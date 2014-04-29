(ns graphbrain.db.utils
  (:require [graphbrain.db.graph :as gb]
            [graphbrain.db.consensus :as consensus]))

(defn init-with-consensus!
  []
  (let [g (gb/graph)]
    (consensus/start-consensus-processor! g) g))
