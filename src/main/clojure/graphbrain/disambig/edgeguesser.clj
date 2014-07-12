(ns graphbrain.disambig.edgeguesser
  (:require [graphbrain.db.id :as id]
            [graphbrain.disambig.entityguesser :as eg]))

(defn guess
  [gbdb id text ctxts]
  (case (id/id->type id)
    :entity (eg/guess-eid gbdb id text nil ctxts)
    :edge (id/ids->id (map #(guess gbdb % text ctxts) (id/id->ids id)))
    id))
