(ns graphbrain.db.searchinterface
  (:require [graphbrain.db.id :as id]
            [graphbrain.db.graph :as gb]
            [graphbrain.db.edge :as edge]))

(defn query
  [graph text]
  (let [id (id/sanitize text)
        can-mean (gb/pattern->edges graph ["r/+can_mean" id "*"])]
    (map #(second (edge/participant-ids %)) can-mean)))
