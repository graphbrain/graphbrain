(ns graphbrain.eco.eco
  (:use [graphbrain.utils :only [dbg]])
  (:require [graphbrain.db.id :as id]
            [graphbrain.eco.words :as words]
            [graphbrain.eco.word :as word]
            [clojure.math.combinatorics :as combs]))

(defn verb
  [word]
  (word/verb? word))

(defn !verb
  [word]
  (not (verb word)))

(defn ind
  [word]
  (word/indicator? word))

(defn !ind
  [word]
  (not (ind word)))

(defn adverb
  [word]
  (word/adverb? word))

(defn !adverb
  [word]
  (not (adverb word)))

(defn adjective
  [word]
  (word/adjective? word))

(defn !adjective
  [word]
  (not (adjective word)))

(defn det
  [word]
  (word/det? word))

(defn !det
  [word]
  (not (det word)))

(defn w
  [word-str]
  (fn [word] (= (:word word) word-str)))

(defn !w
  [word-str]
  (fn [word] (not (= (:word word) word-str))))

(defn ends-with
  [words1 words2]
  (= (take-last (count words2) words1) words2))

(defn entity
  [words]
  (id/sanitize (clojure.string/join " " (map :word words))))

(defn rel
  [words]
  (str "r/" (entity words)))

(defn edge
  [& parts]
  (let [lparts (map #(if (coll? %) % [%]) parts)]
    (map #(id/ids->id %)
         (apply combs/cartesian-product lparts))))

(defn eid
  [rel name & ids]
  (id/name+ids->eid rel name ids))

(defn words->str
  [& words]
  (clojure.string/join " "
   (map #(clojure.string/join " " (map :word %)) words)))

(defn chunk-def->chunk
  [chunk-def]
  {:var (first chunk-def)
   :word-conds (second chunk-def)})

(defmacro ecoparser
  [name]
  `(def ~name []))

(defmacro pattern
  [rules chunks f]
  `(def ~rules
     (conj ~rules
           {:chunks
            (let [~'chunk-defs
                  ~(apply vector (map
                                  #(vector (keyword (first %)) (second %))
                                  (partition 2 (destructure chunks))))]
              (reduce
               (fn [~'v ~'cd]
                 (conj ~'v (chunk-def->chunk ~'cd)))
               [] ~'chunk-defs))
            :f ~(list 'fn*
                      (apply vector
                             (conj (sort
                               (apply vector
                                      (map
                                       first
                                       (partition 2 (destructure chunks)))))
                                   (symbol 'env)))
                      f)
            :desc ~(clojure.string/join "-"
                   (map #(name (first %)) (partition 2 (destructure chunks))))})))

(defn eval-chunk-word
  [chunk word]
  (if (and chunk word)
    (every? #(% word) (:word-conds chunk))))

(declare eval-rule-r)

(defn- add-var-and-parse-more
  [chunk subsent chunks sentence env]
  (let [parse (eval-rule-r
               sentence
               (rest chunks)
               []
               env)]
    (if parse (map #(assoc % (:var chunk) subsent) parse))))

(defn eval-rule-r
  [sentence chunks subsent env]
  (let [chunk (first chunks)
        word (first sentence)

        cur-word-cur-chunk (eval-chunk-word chunk word)
        
        ;; continue chunk
        continue (if cur-word-cur-chunk
                  (eval-rule-r
                   (rest sentence)
                   chunks
                   (conj subsent word)
                   env))

        ;; end current chunk
        end (if (and (not cur-word-cur-chunk) (not (empty? subsent)))
              (add-var-and-parse-more chunk subsent chunks sentence env))
        
        ;; fork chunk
        fork (if (and cur-word-cur-chunk
                     (not (empty? subsent))
                     (eval-chunk-word (second chunks) word))
               (add-var-and-parse-more chunk subsent chunks sentence env))]
    (if (and (empty? sentence) (empty? chunks))
      [nil]
      (let [res (filter #(not (empty? %)) (into (into continue end) fork))]
        res))))

(defn eval-rule-start
  [rule sentence env]
  (let [chunks (:chunks rule)]
    (eval-rule-r sentence chunks [] env)))

(defn- result->vertex
  [rule result env]
  (let [ks (sort (keys result))
        vals (map #(% result) ks)]
    (apply (:f rule) (conj vals env))))

(defn parse
  [rules words env]
  (loop [rs rules]
    (if (not (empty? rs))
      (let [rule (first rs)
            results (eval-rule-start rule words env)]
        (if (not (empty? results))
          (into [] (map #(result->vertex rule % env)
                          results))
          (recur (rest rs)))))))

(defn parse-str
  [rules s env]
  (parse rules (words/str->words s) env))

(defmacro p
  [rules words]
  `(parse ~rules ~words ~'env))

(defn ecotest
  [rules]
  (let [env {:root "f43806bb591e3b87/berlin", :user "u/telmo"}]
    (parse-str rules "I like Berlin" env)
    (System/exit 0)))
