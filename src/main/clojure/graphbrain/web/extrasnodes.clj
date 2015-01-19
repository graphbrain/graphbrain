(ns graphbrain.web.extrasnodes
  (:require [graphbrain.db.gbdb :as gb]
            [graphbrain.web.common :as common]
            [graphbrain.web.visualvert :as vv]))

(defn- edge->map
  [edge ctxt]
  (let [vv (vv/id->visual nil (:id edge) ctxt nil)]
    (assoc vv
      :edge (:id edge)
      :edge-text (:text vv))))

(defn- recent-edges
  [ctxt]
  (let [edges (gb/recent-n-edges common/gbdb (:id ctxt) 10)]
    {:nodes (map #(edge->map % ctxt) edges)
     :label "Recent additions"
     :static true}))

(defn extrasnodes
  [gbdb root-id ctxt ctxts snodes]
  (if (= root-id (:id ctxt))
    (assoc snodes
      "recent" (recent-edges ctxt))
    snodes))
