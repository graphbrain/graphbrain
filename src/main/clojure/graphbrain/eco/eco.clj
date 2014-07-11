(ns graphbrain.eco.eco
  (:require [graphbrain.db.id :as id]
            [graphbrain.eco.words :as words]
            [graphbrain.eco.word :as word]))

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

(defn chunk-def->chunk
  [chunk-def]
  {:var (first chunk-def)
   :word-conds (second chunk-def)})

(defmacro ecoparser
  [name]
  `(def ~name []))

(defmacro eco-wv
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
  (every? #(% word) (:word-conds chunk)))

(defn eval-chunk
  [chunk sentence]
  (loop [s sentence
         part []]
    (if (empty? s)
      part
      (if (eval-chunk-word chunk (first s))
        (recur (rest s) (conj part (first s)))
        part))))

(defn eval-rule
  [rule sentence env]
  (loop [chunks (:chunks rule)
         results nil
         rest-of-sentence sentence]
    (if (and (empty? chunks) (empty? rest-of-sentence))
      results
      (if (and (not (empty? chunks)) (not (empty? rest-of-sentence)))
       (let [chunk (first chunks)
             subsent (eval-chunk chunk rest-of-sentence)]
         (if (not (empty? subsent))
           (recur (rest chunks)
                  (assoc results (:var chunk) subsent)
                  (drop (count subsent) rest-of-sentence))))))))

(defn parse
  [rules words env]
  (loop [rs rules]
    (if (not (empty? rs))
      (let [rule (first rs)
            result (eval-rule rule words env)]
        (if result
          (let [ks (sort (keys result))]
            (apply (:f rule) (conj (map #(% result) ks) env)))
          (recur (rest rs)))))))

(defn parse-str
  [rules s env]
  (parse rules (words/str->words s) env))

(defmacro p
  [rules words]
  `(parse ~rules ~words ~'env))
