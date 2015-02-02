(ns graphbrain.db.knowledge
  (:require [graphbrain.db.gbdb :as gb]
            [graphbrain.db.id :as id]
            [graphbrain.db.maps :as maps]))

(defn addfact!
  [gbdb edge ctxt-id author-id]
  (if (= (maps/edge-type edge) "r/*edges")
    (doseq [e (map maps/id->edge
                   (maps/participant-ids edge))]
      (addfact! gbdb e ctxt-id author-id))
    (if (not (gb/getv gbdb (:id edge) [ctxt-id]))
      (let [fact-author-id ["r/*author" (:id edge) author-id]]
        (gb/putv! gbdb edge ctxt-id)
        (gb/putv! gbdb
                  (maps/id->edge
                   (id/ids->id fact-author-id))
                  ctxt-id)))))

(defn author
  [gbdb edge-id ctxts]
  (let [authors (gb/pattern->edges gbdb ["r/*author" edge-id "*"] ctxts)]
    (if authors
      (second
       (maps/participant-ids
        (first authors))))))
