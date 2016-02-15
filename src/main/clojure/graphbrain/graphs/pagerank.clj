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

(ns graphbrain.graphs.pagerank
  (:use graphbrain.utils.utils
        graphbrain.graphs.graphtools))

(defn init-pr
  [graph]
  (add-field-all-nodes graph :pr 1.0))

(defn- pr-contrib
  [graph node]
  (let [v (graph node)
        pr (:pr v)
        l (double (count (:in v)))]
    (/ pr l)))

(defn- new-node-pr
  [graph node damp]
  (let [links (keys (:in node))
        prc (reduce + (map #(pr-contrib graph %) links))]
    (assoc node :newpr (+ (/ (- 1.0 damp) (double (count graph))) (* damp prc)))))

(defn- compute-new-pr
  [graph damp]
  (map-map-vals #(new-node-pr graph % damp) graph))

(defn- update-node-pr
  [node]
  (assoc node :pr (:newpr node)))

(defn- update-pr
  [graph]
  (map-map-vals update-node-pr graph))

(defn sorted-by-pr
  [graph]
  (into
   (sorted-map-by (fn [key1 key2]
                    (compare (:pr (graph key2)) (:pr (graph key1))))) graph))
(defn- pr-node-error
  [node]
  (let [nodev (second node)]
    (Math/abs (- (:pr nodev) (:newpr nodev)))))

(defn- pr-error
  [graph]
  (reduce max (map pr-node-error graph)))

(defn compute-pr
  [graph damp]
  (loop [g graph
         error 1]
    (if (< error 0.001)
      (sorted-by-pr g)
      (let [g1 (compute-new-pr g damp)
            err (pr-error g1)]
        (prn err)
        (recur (update-pr g1) err)))))
