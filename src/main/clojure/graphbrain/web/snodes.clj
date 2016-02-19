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

(ns graphbrain.web.snodes
  (:require [graphbrain.web.visualvert :as vv]
            [graphbrain.hg.ops :as ops]
            [graphbrain.hg.symbol :as symb]))

(defn- snode-id
  [rel-pos]
  (str (-> (first rel-pos)
           (clojure.string/replace #"\W" "_"))
       "_" (second rel-pos)))

(defn- edge->visual
  [edge root-id]
  (let [c (count edge)]
    {:edge-type (first edge)
     :id1 (nth edge 1)
     :id2 (nth edge 2)
     :parent edge}))

(defn- add-to-edge-node-map
  [en-map key e root-id]
  (if (or (and (= (second key) 0) (= (:id1 e) root-id))
          (and (= (second key) 1) (= (:id2 e) root-id)))
    en-map
    (assoc en-map key (conj (en-map key) e))))

(defn- add-edge-to-edge-node-map
  [en-map edge root-id]
  (let [enm (add-to-edge-node-map en-map [(:edge-type edge) 0] edge root-id)
        enm (add-to-edge-node-map enm [(:edge-type edge) 1] edge root-id)]
    enm))

(defn- edge-node-map
  [edges root-id]
  (reduce #(add-edge-to-edge-node-map %1 %2 root-id) {} edges))

(defn- node-label
  [node]
  (case (:type node)
    :concept (:text node)
    :url "web page"
    (name (:type node))))

(defn- link-label
  [edge-type rpos root-node]
  (if (empty? edge-type)
    ""
    (let [rel-label (symb/symbol->str edge-type)
          text (node-label root-node)]
      (if (= rpos 0)
        (str rel-label " " text)
        (str text " " rel-label)))))

(defn- node->map
  [hg node-id edge root-id]
  (let [vv (vv/sym->visual node-id)
        node-edge (:parent edge)]
    (assoc vv
      :edge (str node-edge))))

(defn- se->node-id
  [se rp]
  (if (= (second rp) 0) (:id1 se) (:id2 se)))

(defn- snode
  [hg rp sedges root-node]
  (let [etype (first rp)
        rpos (second rp)
        label (link-label etype rpos root-node)
        nodes (map #(node->map hg
                               (se->node-id % rp)
                               %
                               (:id root-node)) sedges)]
    {:nodes nodes
     :etype etype
     :rpos rpos
     :label label}))

(defn- snode-map
  [hg en-map root-node]
  (loop [enm en-map
         count 0
         snode-map {}]
    (if (empty? enm)
      snode-map
      (let [en (first enm)
            rp (first en)
            snid (snode-id rp)]
        (recur (rest enm) (inc count)
               (assoc snode-map
                 snid
                 (snode hg rp (second en) root-node)))))))

(defn- edges->visual
  [root-id edges]
  (filter (complement nil?)
          (map #(edge->visual % root-id)
               edges)))

(defn generate
  [hg root-id]
  (let [edges (ops/star hg root-id)
        visual-edges (edges->visual root-id edges)
        en-map (edge-node-map visual-edges root-id)
        root-node (vv/sym->visual root-id)]
    (snode-map hg en-map root-node)))
