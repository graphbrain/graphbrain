(ns graphbrain.db.edgetype
  (:import (com.graphbrain.db EdgeType)))

(defn label
  [id]
  (EdgeType/label id))
