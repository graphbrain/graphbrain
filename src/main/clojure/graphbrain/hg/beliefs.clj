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

(ns graphbrain.hg.beliefs
  "Belief-level operations -- facts supported by sources."
  (:require [clojure.set :refer [union]]
            [graphbrain.hg.constants :as const]
            [graphbrain.hg.ops :as ops]))

(defn add!
  "A belif is a fact with a source. The fact is created as a normal edge
   if it does not exist yet. Another edge is created to assign the fact to
   the source.

   Multiple edges can be provided, in which case all the beliefs will be
   inserted at once. This may be faster."
  [hg source edges & {:keys [timestamp] :or {timestamp -1}}]
  (if (coll? (first edges))
    (let [sources (map #(vector const/source % source) edges)
          all-edges (into [] (union edges sources))]
      (ops/add! hg all-edges :timestamp timestamp))
    (ops/add! hg [edges [const/source edges source]] :timestamp timestamp)))

(defn sources
  "Set of sources (nodes) that support a statement (edge)."
  [hg edge]
  (set (map #(nth % 2)
        (ops/pattern->edges hg [const/source edge nil]))))

(defn remove!
  "A belif is a fact with a source. The link from the source to the fact
   is removed. If no more sources support the fact, then the fact is also
   removed."
  [hg source edge]
  (ops/remove! hg [const/source edge source])
  (if (empty? (sources hg edge))
    (ops/remove! hg edge)))
