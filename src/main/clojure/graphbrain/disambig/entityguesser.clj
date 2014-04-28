(ns graphbrain.disambig.entityguesser
  (:require [graphbrain.db.graph :as graph]
            [graphbrain.db.id :as id]
            [graphbrain.db.vertex :as vertex]
            [graphbrain.db.edge :as edge]))

(defn guess
  [graph name to-hash]
  (let [base-id (id/sanitize name)
        can-mean (graph/pattern->edges ["r/+can_mean"  base-id "*"])]
    (if (empty? can-mean)
      (vertex/id->vertex (id/build-id [(id/hashed to-hash) base-id]))
      (let [syns (map #(graph/getv graph (nth (edge/ids %) 3)) can-mean)]
        (apply max-key :degree syns)))))
