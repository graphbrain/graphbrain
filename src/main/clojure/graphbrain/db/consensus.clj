(ns graphbrain.db.consensus
  (:require [graphbrain.db.id :as id]
            [graphbrain.db.graph :as gb]
            [graphbrain.db.vertex :as vertex]
            [graphbrain.db.edge :as edge]))

(defn eval-edge!
  [graph edge]
  (if (id/global-space? (:id edge))
    (let [score 0
          neg-edge (edge/negate edge)
          pids (edge/participant-ids edge)
          alt-vertices (gb/global-alts (first pids))
          score (loop [altvs alt-vertices
                       score 0]
                  (if (empty? altvs) score
                      (let [altv (first altvs)
                            user-id (id/owner-id altv)
                            user-edge (vertex/global->user edge user-id)
                            neg-user-edge (vertex/global->user neg-edge user-id)
                            s (if (gb/exists? graph user-edge) (inc score) score)
                            s (if (gb/exists? graph neg-user-edge) (dec s) s)]
                        (recur (rest altvs) s))))]
      (if (> score 0) (gb/putv! graph edge) (gb/remove! graph edge)))))
