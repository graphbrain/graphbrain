(ns graphbrain.eco.eco
  (:use [graphbrain.utils :only [dbg]]
        graphbrain.eco.ecofuns)
  (:require [graphbrain.db.id :as id]
            [graphbrain.eco.words :as words]
            [clojure.set :as set]))

(defn chunk-def->chunk
  [chunk-def]
  {:var (first chunk-def)
   :word-conds (second chunk-def)})

(defmacro ecoparser
  [name]
  `(def ~name []))

(defn funexpand
  [funs]
  (map #(if (string? %) (w %) %) funs))

(defn funvec
  [x]
  (funexpand (if (coll? x) x [x])))

(defn- cond-weight
  [cond]
  (if (= cond ?) 0 1))

(defn- rule-priority
  [rule]
  (reduce +
          (map
           #(+ (reduce + (map cond-weight (funvec (:word-conds %)))) 1)
           (:chunks rule))))

(defn sorted-rules
  [rules]
  (sort #(> (:priority %1) (:priority %2))
        (map #(assoc % :priority (rule-priority %)) rules)))

(defmacro pattern
  [rules chunks f]
  `(def ~rules
     (sorted-rules (conj ~rules
            {:chunks
             (let [~'chunk-defs
                   ~(apply vector (map
                                   #(vector (keyword (first %))
                                            (second %))
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
                                         (map #(name (first %)) (partition 2 (destructure chunks))))}))))

(defn eval-chunk-word
  [chunk word]
  (if (and chunk word)
    (every? #(% word) (funvec (:word-conds chunk)))))

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
  (loop [rs rules
         res []]
    (if (empty? rs)
      res
      (let [rule (first rs)
            results (eval-rule words (:chunks rule) [] env)
            results (flatten (into [] (map #(result->vertex rules rule % env)
                                           results)))]
        (recur (rest rs)
               (set/union res results))))))

(defn parse->vertex
  [par]
  (let [vert (:vertex par)]
    (if (coll? vert)
      (id/ids->id (map parse->vertex vert))
      vert)))

(defn vert+weight
  [par]
  (let [vert (:vertex par)
        rule (:rule par)
        weight (if rule
                 (:priority rule) 0)]
    (if (coll? vert)
      (let [vws (map vert+weight vert)
            id (id/ids->id (map :vert vws))
            w (reduce + (map :weight vws))]
        (assoc par
          :vert id
          :weight (+ w weight)))
      (assoc par
        :vert vert
        :weight weight))))

(defn verts+weights->vertex
  [rules vws env]
  (let [best (apply max (map :weight vws))
        vws (filter #(= (:weight %) best) vws)]
    (:vert (first vws))))

(defn parse-words
  [rules words env]
  (let [par (parse rules words env)
        vws (map vert+weight par)]
    (verts+weights->vertex rules vws env)))

(defn parse-str
  [rules s env]
  (parse-words rules
               (words/str->words s)
               env))

(defmacro !
  [words]
  `(parse ~'rules ~words ~'env))
