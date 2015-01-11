(ns graphbrain.db.context
  (:require [graphbrain.db.entity :as entity]))

(defn text
  [id-or-context]
  (entity/text id-or-context))

(defn label
  [id-or-context]
  (entity/label id-or-context))
