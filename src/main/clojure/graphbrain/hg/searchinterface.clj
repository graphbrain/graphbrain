(ns graphbrain.hg.searchinterface
  (:require [graphbrain.hg.id :as id]
            [graphbrain.hg.ops :as hgops]))

(defn query
  [hg text ctxts]
  #_(let [id (id/sanitize text)
        can-mean (gb/pattern->edges gbdb ["r/*can_mean" id "*"] ctxts)]
    (map #(second (maps/participant-ids %)) can-mean)))
