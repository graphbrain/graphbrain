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
