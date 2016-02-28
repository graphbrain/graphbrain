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

(ns graphbrain.hg.queries
  (:require [graphbrain.hg.ops :as ops]))

(def exclude-set
  #{"653d14f5d5a58462/noun"
    "e2665974fa3ff255/much"
    "10bb96ba951ac820/more"
    "other"})

(defn- exclude-edge
  [edge]
  (some exclude-set (rest edge)))

(defn- include-edge
  [edge]
  (not (exclude-edge edge)))

(defn- add-neighbours
  [neighbours edge]
  (clojure.set/union neighbours
                     (set (rest edge))))

(defn- vemap-assoc-id-edge
  [vemap id edge]
  (let [vemap (update-in vemap [id :edges] #(conj % edge))
        vemap (update-in vemap [id :neighbours] add-neighbours edge)]
    vemap))

(defn- vemap+edge
  [vemap edge]
  (reduce #(vemap-assoc-id-edge %1 %2 edge)
          vemap
          (rest edge)))

(defn- edges->vemap
  [vemap edges]
  (reduce #(vemap+edge %1 %2) vemap edges))

(defn node->edges
  [hg node]
  (filter include-edge
          (ops/ego hg node 2)))

(defn- interedge
  [edge interverts]
  (every? #(some #{%} interverts) (rest edge)))

(defn walks
  [vemap nodes walk all-walks]
  (if (every? #(some #{%} walk) nodes)
    (conj all-walks walk)
    (let [neighbours (:neighbours (vemap (last walk)))
         next-steps (filter #(not (some #{%} walk)) neighbours)]
     (if (empty? next-steps)
       all-walks
       (reduce #(clojure.set/union %1
                                   (walks vemap nodes
                                          (conj walk %2)
                                          #{}))
               all-walks next-steps)))))

(defn- walk-step->wmap
  [wmap walk-length step]
  (let [clen (wmap step)]
    (if (or (not clen) (< walk-length clen))
      (assoc wmap step walk-length)
      wmap)))

(defn- walk->wmap
  [wmap walk]
  (let [walk-length (count walk)]
    (reduce #(walk-step->wmap %1 walk-length %2)
            wmap walk)))

(defn- walks->wmap
  [walks]
  (reduce walk->wmap {} walks))

(defn- valid-step?
  [wmap nodes walk-length step]
  (or (some #{step} nodes)
      (= walk-length (wmap step))))

(defn- valid-walk?
  [wmap nodes walk]
  (let [walk-length (count walk)]
    (every? #(valid-step? wmap nodes walk-length %) walk)))

(defn intersect
  [hg seeds]
  (let [edgesets (map #(node->edges hg %) seeds)
        vemap (reduce edges->vemap {} edgesets)
        walks (walks vemap seeds [(first seeds)] #{})
        wmap (walks->wmap walks)
        walks (filter #(valid-walk? wmap seeds %) walks)
        interverts (into #{}
                         (flatten
                          (into [] walks)))
        edges (vals vemap)
        edges (mapcat identity (map :edges edges))
        edges (filter #(interedge % interverts) edges)]
    edges))
