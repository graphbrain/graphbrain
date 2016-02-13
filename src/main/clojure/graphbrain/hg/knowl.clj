(ns graphbrain.hg.knowl
  "Hypergraph higher-level operations, with degrees and indexing."
  (:require [graphbrain.hg.ops :as ops]))

(defn degree
  "Degree of a node."
  [hg node]
  (let [deg-edges (ops/pattern->edges hg ["deg" node "*"])]
    (if (empty? deg-edges)
      0
      (nth (first deg-edges) 2))))

(defn- set-degree!
  "Sets the degree of a node."
  [hg node degree]
  (ops/remove-by-pattern! hg ["deg" node "*"])
  (if (> degree 0)
    (ops/add! hg ["deg" node degree]))
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
  ""
  [hg node])

(defn- deindex-node!
  ""
  [hg node])

(defn exists?
  "Checks if the given edge exists in the hypergraph."
  [hg edge]
  (ops/exists? hg edge))

(defn add!
  "Adds an edge to the hypergraph if it does not exist yet."
  [hg edge]
  (doseq [node edge]
    (if (= (inc-degree! hg node) 1)
      (index-node! node)))
  (ops/add! hg edge))

(defn remove!
  "Removes an edge from the hypergraph."
  [hg edge]
  (doseq [node edge]
    (if (= (dec-degree! hg node) 0)
      (deindex-node! node)))
  (ops/remove! hg edge))

(defn pattern->edges
  "Return all the edges that match a pattern. A pattern is a collection of entity ids and wildcards ('*')."
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
