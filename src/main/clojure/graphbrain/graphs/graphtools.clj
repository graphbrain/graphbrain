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

(ns graphbrain.graphs.graphtools
  (:use graphbrain.utils.utils))

(defn node-in-arcs
  [node]
  (let [arcs (:in node)]
    (if arcs arcs {})))

(defn node-out-arcs
  [node]
  (let [arcs (:out node)]
    (if arcs arcs {})))

(defn- add-to-arc-map
  [arc-map node]
  (if (contains? arc-map node)
    (let [arc (arc-map node)
          weight (:weight arc)]
      (assoc arc-map node
             (assoc arc :weight (inc weight))))
    (assoc arc-map node {:weight 1})))

(defn add-arc
  [graph pair]
  (let [orig (first pair)
        targ (second pair)
        orig-node (graph orig)
        targ-node (graph targ)
        orig-out (node-out-arcs orig-node)
        targ-in (node-in-arcs targ-node)
        g (assoc graph orig (assoc orig-node :out (add-to-arc-map orig-out targ)))
        g (assoc g targ (assoc targ-node :in (add-to-arc-map targ-in orig)))]
    g))

(defn add-edge
  [graph pair]
  (let [g (add-arc graph pair)
        g (add-arc g (reverse pair))] g))

(defn add-field-all-nodes
  [graph field value]
  (map-map-vals #(assoc % field value) graph))

(defn inc-field
  [graph node-key field]
  (let [node (graph node-key)
        node (if node node {})
        val (node field)
        newval (if val (inc val) 1)]
    (assoc graph node-key (assoc node field newval))))
