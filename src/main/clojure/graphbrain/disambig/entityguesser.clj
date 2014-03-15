(ns graphbrain.disambig.entityguesser
  (:use graphbrain.db.graph)
  (:import (com.graphbrain.eco EntityGuesser)))

(defn guess
  [graph name to-hash]
  (vertex-obj->map (EntityGuesser/guess graph name to-hash)))
