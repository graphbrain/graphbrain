(ns graphbrain.db.searchinterface
  (:require [graphbrain.db.id :as id]
            [graphbrain.db.gbdb :as gb]
            [graphbrain.db.edge :as edge]))

(defn query
  [gbdb text]
  (let [id (id/sanitize text)
        can-mean (gb/pattern->edges gbdb ["r/+can_mean" id "*"])]
    (map #(second (edge/participant-ids %)) can-mean)))
