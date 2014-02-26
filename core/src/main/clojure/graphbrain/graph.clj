(ns graphbrain.graph
  (:import (com.graphbrain.db Edge VertexType)))

(defn edge?
  [vertex]
  (and
    vertex
    (= (. vertex type) VertexType/Edge)))

(defn edge-from-id
  [edge-id]
  (Edge/fromId edge-id))