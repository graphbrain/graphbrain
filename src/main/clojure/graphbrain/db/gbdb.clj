(ns graphbrain.db.gbdb
  (:require [graphbrain.db.mysql :as mysql]))

(defn gbdb
  ([] (gbdb "gbnode"))
  ([name] mysql/db-connection))

(defn exists?
  [gbdb edge]
  (mysql/exists? gbdb edge))

(declare remove!)

(defn add!
  [gbdb edge]
  (mysql/add! gbdb edge))

(defn remove!
  [gbdb edge]
  (mysql/remove! gbdb edge))

(defn pattern->edges
  [gbdb pattern]
  (mysql/pattern->edges gbdb pattern))

(defn neighbors
  [gbdb center]
  (mysql/neighbors gbdb center))
