(ns graphbrain.eco.ecofuns
  (:use [graphbrain.utils :only [dbg]])
  (:require [graphbrain.db.id :as id]
            [graphbrain.db.text :as text]
            [graphbrain.eco.word :as word]
            [clojure.math.combinatorics :as combs]))

(defn ?
  [word]
  true)

(defn |
  [& cond-funs]
  (fn [word]
    (some #(% word) cond-funs)))

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

(defn adv
  [word]
  (word/adverb? word))

(defn !adv
  [word]
  (not (adv word)))

(defn adj
  [word]
  (word/adjective? word))

(defn compar
  [word]
  (word/comparative? word))

(defn !adj
  [word]
  (not (adj word)))

(defn det
  [word]
  (word/det? word))

(defn !det
  [word]
  (not (det word)))

(defn to
  [word]
  (word/to? word))

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

(defn id->vert
  [id]
  {:vertex id})

(defn edge
  [& parts]
  (let [lparts (map #(if (and (coll? %) (not (map? %))) % [%]) parts)]
    (map #(hash-map :vertex %) (apply combs/cartesian-product lparts))))

(defn eid
  [rel name & ids]
  (let [lparts (map #(if (and (coll? %) (not (map? %))) % [%]) ids)]
    (map #(hash-map :vertex
                    (apply id/name+ids->eid
                           (conj [rel name] (map :vertex %))))
         (apply combs/cartesian-product lparts))))

(defn words->str
  [& words]
  (clojure.string/join " "
   (map #(clojure.string/join " " (map :word %)) words)))

(defn text
  [words]
  {:vertex (text/text->pseudo
            (words->str words))})
