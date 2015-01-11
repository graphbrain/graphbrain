(ns graphbrain.db.vertex
  (:require [graphbrain.db.id :as id]
            [graphbrain.db.entity :as entity]
            [graphbrain.db.context :as context]))

(defn label
  [vertex]
  (case (:type vertex)
    :entity (entity/label vertex)
    :context (context/label vertex)
    :user (:name vertex)
    (:id vertex)))
