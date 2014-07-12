(ns graphbrain.db.searchinterface
  (:require [graphbrain.db.id :as id]
            [graphbrain.db.gbdb :as gb]
            [graphbrain.db.maps :as maps]))

(defn query
  [gbdb text ctxts]
  (let [id (id/sanitize text)
        can-mean (gb/pattern->edges gbdb ["r/*can_mean" id "*"] ctxts)]
    (map #(second (maps/participant-ids %)) can-mean)))
