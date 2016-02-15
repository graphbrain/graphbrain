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
  (:require [graphbrain.hg.mysql :as mysql]))

(defn hg
  "Obtain a reference to a specific hypergraph."
  ([] (hg :mysql "gbnode"))
  ([storage-type name]
     (case storage-type
       :mysql (mysql/mysql-hg name)
       (throw (Exception. (str "Unknown storage type: " storage-type))))))

(defn exists?
  "Checks if the given edge exists in the hypergraph."
  [hg edge]
  ((:exists? hg) hg edge))

(defn add!
  "Adds an edge to the hypergraph if it does not exist yet."
  [hg edge]
  ((:add! hg) hg edge))

(defn remove!
  "Removes an edge from the hypergraph."
  [hg edge]
  ((:remove! hg) hg edge))

(defn pattern->edges
  "Return all the edges that match a pattern.
   A pattern is a collection of entity ids and wildcards (nil)."
  [hg pattern]
  ((:pattern->edges hg) hg pattern))

(defn star
  "Return all the edges that contain a given entity. Entity can be atomic or an edge."
  [hg center]
  ((:star hg) hg center))

(defn remove-by-pattern!
  "Removes from the hypergraph all edges that match the pattern."
  [hg pattern]
  (doseq [edge (pattern->edges hg pattern)]
    (remove! hg edge)))
