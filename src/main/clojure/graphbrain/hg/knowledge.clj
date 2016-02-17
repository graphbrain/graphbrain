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

(ns graphbrain.hg.knowledge
  "Hypergraph higher-level operations, with degrees and indexing."
  (:require [graphbrain.hg.constants :as const])
  (:use graphbrain.hg.ops))

(defn add-belief!
  "A belif is a fact with a source. The fact is created as a normal edge
   if it does not exist yet. Another edge is created to assign the fact to
   the source."
  [hg source edge]
  (add! hg [edge [const/source edge source]]))

(defn sources
  "Set of sources (nodes) that support a statement (edge)."
  [hg edge]
  (set (map #(nth % 2)
        (pattern->edges hg [const/source edge nil]))))

(defn remove-belief!
  "A belif is a fact with a source. The link from the source to the fact
   is removed. If no more sources support the fact, then the fact is also
   removed."
  [hg source edge]
  (remove! hg [const/source edge source])
  (if (empty? (sources hg edge))
    (remove! hg edge)))
