(ns graphbrain.hg.ops
  (:require [graphbrain.hg.mysql :as mysql]))

(defn hg
  ([] (hg :mysql "gbnode"))
  ([storage-type name]
     (case storage-type
       :mysql (mysql/mysql-hg name)
       (throw (Exception. (str "Unknown storage type: " storage-type))))))

(defn exists?
  [hg edge]
  ((:exists? hg) hg edge))

(defn add!
  [hg edge]
  ((:add! hg) hg edge))

(defn remove!
  [hg edge]
  ((:remove! hg) hg edge))

(defn pattern->edges
  [hg pattern]
  ((:pattern->edges hg) hg pattern))

(defn neighbors
  [hg center]
  ((:neighbors hg) hg center))
