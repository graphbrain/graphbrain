(ns graphbrain.eco.eco
  (:use [graphbrain.utils :only [dbg]])
  (:require [graphbrain.db.id :as id]
            [graphbrain.eco.words :as words]
            [graphbrain.eco.word :as word]
            [clojure.math.combinatorics :as combs]))

(defn ?
  [word]
  true)

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


(defn- words->id
  [words]
  (id/sanitize (clojure.string/join " " (map :word words))))

(defn entity
  [words]
  {:vertex (words->id words)})

(defn user
  [env]
  {:vertex (:user env)})

(defn root
  [env]
  {:vertex (:root env)})

(defn rel
  [words]
  {:vertex (str "r/" (words->id words))})

(defn edge
  [& parts]
  (let [lparts (map #(if (and (coll? %) (not (map? %))) % [%]) parts)]
    (map #(hash-map :vertex %) (apply combs/cartesian-product lparts))))


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

(defn funexpand
  [funs]
  (map #(if (coll? %) % %) (dbg funs)))

(defn funvec
  [x]
  (if (coll? x) x [x]))

(defmacro pattern
  [rules chunks f]
  `(def ~rules
     (conj ~rules
           {:chunks
            (let [~'chunk-defs
                  ~(apply vector (map
                                  #(vector (keyword (first %))
                                           (funvec (second %)))
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
                                   (symbol 'env)
                                   (symbol 'rules)))
                      f)
            :desc ~(clojure.string/join "-"
                   (map #(name (first %)) (partition 2 (destructure chunks))))})))

(defn eval-cond-word
  [cond word]
  (if (string? cond)
    (= (:word word) cond)
    (cond word)))

(defn eval-chunk-word
  [chunk word]
  (if (and chunk word)
    (every? #(eval-cond-word % word) (:word-conds chunk))))

(declare eval-rule)

(defn- add-var-and-parse-more
  [chunk subsent chunks sentence env]
  (let [parse (eval-rule
               sentence
               (rest chunks)
               []
               env)]
    (if parse (map #(assoc % (:var chunk) subsent) parse))))

(defn eval-rule
  [sentence chunks subsent env]
  (let [chunk (first chunks)
        word (first sentence)

        cur-word-cur-chunk (eval-chunk-word chunk word)
        
        ;; continue chunk
        continue (if cur-word-cur-chunk
                  (eval-rule
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

(defn- result->vertex
  [rules rule result env]
  (let [ks (sort (keys result))
        vals (map #(% result) ks)
        verts (apply (:f rule) (conj vals env rules))]
    (if (map? verts)
      (assoc verts :rule rule)
      (map #(assoc % :rule rule) verts))))

(defn parse
  [rules words env]
  (loop [rs rules]
    (if (not (empty? rs))
      (let [rule (first rs)
            results (eval-rule words (:chunks rule) [] env)]
        (if (not (empty? results))
          (flatten (into [] (map #(result->vertex rules rule % env)
                                 results)))
          (recur (rest rs)))))))

(defn parse->vertex
  [par]
  (let [vert (:vertex par)]
    (if (coll? vert)
      (id/ids->id (map parse->vertex vert))
      vert)))

(defn parse-str
  [rules s env]
  (let [par (parse rules (words/str->words s) env)]
    (map parse->vertex par)))

(defmacro !
  [words]
  `(parse ~'rules ~words ~'env))
