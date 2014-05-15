(ns graphbrain.db.consensus
  (:require [graphbrain.db.id :as id]
            [graphbrain.db.graph :as gb]
            [graphbrain.db.vertex :as vertex]
            [graphbrain.db.edge :as edge]
            [graphbrain.db.queues :as queues]))

(defn eval-edge!
  [graph edge-id]
  (if (id/global-space? edge-id)
    (let [edge (edge/id->edge edge-id)
          neg-edge (edge/negate edge)
          pids (edge/participant-ids edge)
          alt-vertices (gb/global-alts (first pids))
          score (loop [altvs alt-vertices
                       score 0]
                  (if (empty? altvs) score
                      (let [altv (first altvs)
                            owner-id (id/owner altv)
                            local-edge (vertex/global->local edge owner-id)
                            neg-local-edge (vertex/global->local neg-edge owner-id)
                            s (if (gb/exists? graph local-edge) (inc score) score)
                            s (if (gb/exists? graph neg-local-edge) (dec s) s)]
                        (recur (rest altvs) s))))]
      (if (> score 0) (gb/putv! graph edge) (gb/remove! graph edge)))))

(defn start-consensus-processor!
  [graph]
  (compare-and-set! queues/consensus-active false true)
  (def consensus-processor (future
     (while @queues/consensus-active
       (eval-edge! (.take queues/consensus-queue))))))

(defn stop-consensus-processor!
  []
  (compare-and-set! queues/consensus-active true false))
