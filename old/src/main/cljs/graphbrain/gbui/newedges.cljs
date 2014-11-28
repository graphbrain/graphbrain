(ns graphbrain.gbui.newedges
  (:require [alandipert.storage-atom :refer [local-storage]]))

(def newedges (local-storage (atom []) :newedges))

(defn set-new-edges!
  [edges]
  (reset! newedges edges))

(defn new-edges
  []
  @newedges)

(defn clean
  []
  (reset! newedges nil))
