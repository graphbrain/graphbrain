;   Copyright (c) 2016 CNRS - Centre national de la recherche scientifique.
;   All rights reserved.
;
;   Written by Telmo Menezes <telmo@telmomenezes.com>
;
;   This file is part of GraphBrain.
;
;   GraphBrain is free software: you can redistribute it and/or modify
;   it under the terms of the GNU Affero General Public License as published by
;   the Free Software Foundation, either version 3 of the License, or
;   (at your option) any later version.
;
;   GraphBrain is distributed in the hope that it will be useful,
;   but WITHOUT ANY WARRANTY; without even the implied warranty of
;   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;   GNU Affero General Public License for more details.
;
;   You should have received a copy of the GNU Affero General Public License
;   along with GraphBrain.  If not, see <http://www.gnu.org/licenses/>.

(ns graphbrain.eco.ecofuns
  (:use [graphbrain.utils.utils :only [dbg]])
  (:require [graphbrain.hg.symbol :as sym]
            [graphbrain.eco.word :as word]
            [clojure.math.combinatorics :as combs]))

(defn ?
  [word]
  true)

(defn ??
  [word]
  true)

(defn ???
  [word]
  true)

(defn ????
  [word]
  true)

(defn |
  [& cond-funs]
  (fn [word]
    (some #(% word) cond-funs)))

(defn w
  [word-str]
  (fn [word] (= (:word word) word-str)))

(defn &
  [& cond-funs]
  (fn [word]
    (loop [conds cond-funs]
      (if (empty? conds)
        true
        (let [cond (first conds)
              cond (if (string? cond)
                     (w cond)
                     cond)]
            (if (cond word)
              (recur (rest conds))))))))

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

(defn !w
  [word-str]
  (fn [word] (not (= (:word word) word-str))))

(defn ends-with
  [words1 words2]
  (= (take-last (count words2) words1) words2))

(defn words->id
  [words]
  (sym/str->symbol (clojure.string/join " " (map :word words))))

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

(defn edges
  [& parts]
  (apply edge (conj parts (id->vert "r/*edges"))))

(defn eid
  [rel name & ids]
  #_(let [lparts (map #(if (and (coll? %) (not (map? %))) % [%]) ids)]
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
  #_{:vertex (text/text->pseudo
            (words->str words))})
