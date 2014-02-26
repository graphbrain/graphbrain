(ns graphbrain.graph
  (:import (com.graphbrain.db VertexType)))

(defn edge?
  [vertex]
  (and
    vertex
    (= (. vertex type) VertexType/Edge)))