(ns graphbrain.db.vertex
  (:require [graphbrain.db.id :as id]
            [graphbrain.db.entity :as entity]))

(defn label
  [vertex]
  (case (:type vertex)
    :entity (entity/label vertex)
    :user (:name vertex)
    (:id vertex)))
