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

(ns graphbrain.metrics.wiki
  (:require [graphbrain.hg.ops :as ops]
            [graphbrain.hg.beliefs :as beliefs]
            [graphbrain.hg.symbol :as symb]
            [graphbrain.hg.constants :as const]
            [clojure.math.combinatorics :as combs]))

(def in-related
  (memoize
   (fn [hg symbol]
     (ops/pattern->edges hg ["related/1" nil symbol]))))

(def out-related
  (memoize
   (fn [hg symbol]
     (ops/pattern->edges hg ["related/1" symbol nil]))))

(def in-not-related
  (memoize
   (fn [hg symbol]
     (ops/pattern->edges hg ["~related/1" nil symbol]))))

(def out-not-related
  (memoize
   (fn [hg symbol]
     (ops/pattern->edges hg ["~related/1" symbol nil]))))

(def related
  (memoize
   (fn [hg symbol]
     (clojure.set/union
      (in-related hg symbol)
      (out-related hg symbol)))))

(def not-related
  (memoize
   (fn [hg symbol]
     (clojure.set/union
      (in-not-related hg symbol)
      (out-not-related hg symbol)))))

(def neighbors
  (memoize
   (fn [hg symbol]
     (disj
      (into #{}
            (flatten (clojure.set/union
                      (map rest (related hg symbol))
                      (map rest (not-related hg symbol)))))
      symbol))))

(def editors
  (memoize
   (fn [hg symbol]
     (into #{}
           (map second (ops/pattern->edges hg ["editor/1" nil symbol]))))))

(def edited-by
  (memoize
   (fn [hg symbol]
     (into #{}
           (map #(nth % 2) (ops/pattern->edges hg ["editor/1" symbol nil]))))))

(defn semantic-overlap
  [hg symb1 symb2]
  (let [n1 (neighbors hg symb1)
        n2 (neighbors hg symb2)]
    (/
     (float (count
             (clojure.set/intersection n1 n2)))
     (float (count
             (clojure.set/union n1 n2))))))

(defn editor-overlap
  [hg symb1 symb2]
  (let [n1 (editors hg symb1)
        n2 (editors hg symb2)]
    (/
     (float (count
             (clojure.set/intersection n1 n2)))
     (float (count
             (clojure.set/union n1 n2))))))

(def claims
  (memoize
   (fn [hg author]
     (filter
      #(= (first (second %))
          "related/1")
      (ops/pattern->edges hg [const/source nil author])))))

(def refutations
  (memoize
   (fn [hg author]
     (filter
      #(= (first (second %))
          "~related/1")
      (ops/pattern->edges hg [const/source nil author])))))
