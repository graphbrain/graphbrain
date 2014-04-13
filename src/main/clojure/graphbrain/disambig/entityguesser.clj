(ns graphbrain.disambig.entityguesser
  (:require [graphbrain.db.graph :as graph])
  (:import (com.graphbrain.eco EntityGuesser)))

(defn guess
  [g name to-hash]
  (graph/vertex-obj->map (EntityGuesser/guess g name to-hash)))
