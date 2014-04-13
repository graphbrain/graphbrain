(ns graphbrain.db.entity
  (:require [graphbrain.db.graph :as gb])
  (:import (com.graphbrain.db EntityNode)))

(defn create
  [id]
  (gb/entity-obj->map (EntityNode. id)))
