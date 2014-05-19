(ns graphbrain.disambig.entityguesser
  (:require [graphbrain.db.gbdb :as gbdb]
            [graphbrain.db.id :as id]
            [graphbrain.db.vertex :as vertex]
            [graphbrain.db.edge :as edge]))

(defn guess
  [gbdb name to-hash]
  (let [base-id (id/sanitize name)
        can-mean (gbdb/pattern->edges gbdb ["r/+can_mean" base-id "*"])]
    (if (empty? can-mean)
      (vertex/id->vertex (id/build [(id/hashed to-hash) base-id]))
      (let [syns (map #(gbdb/getv gbdb (nth (edge/ids %) 2)) can-mean)]
        (apply max-key :degree syns)))))
