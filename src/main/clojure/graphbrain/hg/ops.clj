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

(ns graphbrain.hg.ops
  "Hypergraph low-level operations."
  (:require [clojure.set :as set]))

(defprotocol Ops
  ;; Checks if the given edge exists in the hypergraph.
  (exists? [hg vertex])

  ;; Adds one or multiple edges to the hypergraph if it does not exist yet.
  ;; Adding multiple edges at the same time might be faster.
  (add! [hg edges])

  ;; Removes one or multiple edges from the hypergraph.
  ;; Removing multiple edges at the same time might be faster.
  (remove! [hg edges])

  ;; Return all the edges that match a pattern.
  ;; A pattern is a collection of entity ids and wildcards (nil).
  (pattern->edges [hg pattern])

  ;; Return all the edges that contain a given entity.
  ;; Entity can be atomic or an edge.
  (star [hg center])

  ;; Find all symbols with the given root.
  (symbols-with-root [hg root])
  
  ;; Erase the hypergraph.
  (destroy! [hg])

  ;; Returns the degree of a vertex
  (degree [hg vertex]))

(defn remove-by-pattern!
  "Removes from the hypergraph all edges that match the pattern."
  [hg pattern]
  (doseq [edge (pattern->edges hg pattern)]
    (remove! hg edge)))

;; TODO: this can be optimized
(defn ego
  [hg center depth]
  (if (> depth 0)
    (let [edges (star hg center)
          ids (set (flatten (map rest edges)))
          ;;ids (filter #(< (degree hg %) 9999) ids)
          next-edges (map #(ego hg % (dec depth)) ids)]
      (apply set/union (conj next-edges edges)))))
