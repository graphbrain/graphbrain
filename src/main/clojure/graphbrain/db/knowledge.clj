(ns graphbrain.db.knowledge
  (:require [graphbrain.db.gbdb :as gb]
            [graphbrain.db.id :as id]
            [graphbrain.db.maps :as maps]))

(defn addfact!
  [gbdb edge ctxt-id author-id]
  (let [ledge (maps/global->local edge ctxt-id)
        fact-author-id ["r/*author" (:id ledge) author-id]]
    (gb/putv! gbdb edge ctxt-id)
    (gb/putv! gbdb
              (maps/id->edge
               (id/ids->id fact-author-id))
              ctxt-id)))
