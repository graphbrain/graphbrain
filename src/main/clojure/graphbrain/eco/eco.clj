(ns graphbrain.eco.eco
  (:require [graphbrain.eco.words :as words]
            [graphbrain.eco.word :as word]))

(defn verb
  [word]
  (word/verb? word))

(defn not-verb
  [word]
  (not (verb word)))

(defn- chunk-def->chunk
  [chunk-def]
  {:var (first chunk-def)
   :word-conds (second chunk-def)})

(defmacro ecorules
  [name]
  `(def ~name []))

(defmacro ecorule
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
                             (sort (apply vector
                                    (map first (partition 2 (destructure chunks))))))
                      f)})))

(ecorules testrules)

(ecorule testrules
         [a [not-verb]
          b [verb]
          c [not-verb]]
         (str "verb: " b))

(defn eval-chunk-word
  [chunk word]
  (every? #(% word) (:word-conds chunk)))

(defn eval-chunk
  [chunk sentence]
  (loop [s sentence
         part []]
    (if (eval-chunk-word chunk (first s))
      (if (empty? s)
        part
        (recur (rest s) (conj part (first s))))
      part)))

(defn eval-rule
  [rule sentence]
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

(defn eval-rules
  [rules sentence]
  (let [words (words/str->words sentence)]
    (loop [rs rules]
      (if (not (empty? rs))
        (let [rule (first rs)
              result (eval-rule rule words)]
          (if result
            (let [ks (sort (keys result))]
              (apply (:f rule) (map #(% result) ks)))
            (recur (rest rs))))))))
