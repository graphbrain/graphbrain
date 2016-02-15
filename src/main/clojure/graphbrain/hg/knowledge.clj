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
  (:require [graphbrain.hg.ops :as ops]
            [graphbrain.hg.symbol :as sym]
            [graphbrain.hg.constants :as const]))

(defn degree
  "Degree of a node."
  [hg node]
  (let [deg-edges (ops/pattern->edges hg [const/degree node nil])]
    (if (empty? deg-edges)
      0
      (nth (first deg-edges) 2))))

(defn- set-degree!
  "Sets the degree of a node."
  [hg node degree]
  (ops/remove-by-pattern! hg [const/degree node nil])
  (if (> degree 0)
    (ops/add! hg [const/degree node degree]))
  degree)

(defn- inc-degree!
  "Increments the degree of a node by one."
  [hg node]
  (set-degree! hg node
               (inc (degree hg node))))

(defn- dec-degree!
  "Decrements the degree of a node by one."
  [hg node]
  (set-degree! hg node
               (dec (degree hg node))))

(defn- index-node!
  "Add an index connection to the node (if node is not a root)."
  [hg node]
  (if (not (sym/root? node))
    (ops/add! hg [const/index (sym/root node) node])))

(defn- deindex-node!
  "Remove index connection to the node (if node is not a root)."
  [hg node]
  (if (not (sym/root? node))
    (ops/remove! hg [const/index (sym/root node) node])))

(defn exists?
  "Checks if the given edge exists in the hypergraph."
  [hg edge]
  (ops/exists? hg edge))

(defn add!
  "Adds an edge to the hypergraph if it does not exist yet."
  [hg edge]
  (if (not (ops/exists? hg edge))
    (do (doseq [node edge]
          (if (= (inc-degree! hg node) 1)
            (index-node! hg node)))
        (ops/add! hg edge))))

(defn remove!
  "Removes an edge from the hypergraph if it exists."
  [hg edge]
  (if (ops/exists? hg edge)
    (do (doseq [node edge]
       (if (= (dec-degree! hg node) 0)
         (deindex-node! hg node)))
        (ops/remove! hg edge))))

(defn pattern->edges
  "Return all the edges that match a pattern.
  A pattern is a collection of entity ids and wildcards (nil)."
  [hg pattern]
  (ops/pattern->edges hg pattern))

(defn star
  "Return all the edges that contain a given entity. Entity can be atomic or an edge."
  [hg center]
  (ops/star hg center))

(defn remove-by-pattern!
  "Removes from the hypergraph all edges that match the pattern."
  [hg pattern]
  (ops/remove-by-pattern! hg pattern))

(defn symbols-with-root
  "Find all the symbols with the given root."
  [hg root]
  (set (map #(nth % 2)
            (ops/pattern->edges hg [const/index root nil]))))

(defn add-belief!
  "A belif is a fact with a source. The fact is created as a normal edge
   if it does not exist yet. Another edge is created to assign the fact to
   the source."
  [hg source edge]
  (add! hg edge)
  (add! hg [const/source edge source]))

(defn sources
  "Set of sources (nodes) that support a statement (edge)."
  [hg edge]
  (set (map #(nth % 2)
        (ops/pattern->edges hg [const/source edge nil]))))

(defn remove-belief!
  "A belif is a fact with a source. Two link from the source to the fact
   is removed. If no more sources support the fact, then the fact is also
   removed."
  [hg source edge]
  (remove! hg [const/source edge source])
  (if (empty? (sources hg edge))
    (remove! hg edge)))
