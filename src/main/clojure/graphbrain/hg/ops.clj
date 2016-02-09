(ns graphbrain.hg.ops
  (:require [graphbrain.hg.mysql :as mysql]))

(defn hg
  ([] (hg "gbnode"))
  ([name] mysql/db-connection))

(defn exists?
  [hg edge]
  (mysql/exists? hg edge))

(defn add!
  [hg edge]
  (mysql/add! hg edge))

(defn remove!
  [hg edge]
  (mysql/remove! hg edge))

(defn pattern->edges
  [hg pattern]
  (mysql/pattern->edges hg pattern))

(defn neighbors
  [hg center]
  (mysql/neighbors hg center))
