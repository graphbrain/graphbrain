(ns graphbrain.db.utils
  (:require [graphbrain.db.gbdb :as gb]
            [graphbrain.db.consensus :as consensus]))

(defn init-with-consensus!
  []
  (let [g (gb/gbdb)]
    (consensus/start-consensus-processor! g) g))
