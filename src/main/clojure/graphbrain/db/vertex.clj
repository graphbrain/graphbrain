(ns graphbrain.db.vertex
  (:require [graphbrain.db.graph :as gb])
  (:import (com.graphbrain.db Vertex)))

(defn id->vertex
  [id]
  (gb/vertex-obj->map (Vertex/fromId id)))
