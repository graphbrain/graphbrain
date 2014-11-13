(ns graphbrain.db.queries
  (:require [graphbrain.db.gbdb :as db]
            [graphbrain.db.id :as id]
            [graphbrain.db.maps :as maps]
            [graphbrain.utils :as u]))

(def exclude-set
  #{"653d14f5d5a58462/noun"})

(defn- exclude-edge
  [edge]
  (some exclude-set
        (map id/eid->id (maps/participant-ids edge))))

(defn- include-edge
  [edge]
  (not (exclude-edge edge)))

(defn- vemap-assoc-id-edge
  [vemap id edge]
  (update-in vemap [id :edges] #(conj % edge)))

(defn- vemap+edge
  [vemap edge]
  (reduce #(vemap-assoc-id-edge %1 %2 edge)
          vemap
          (maps/participant-ids edge)))

(defn- edges->vemap
  [vemap edges]
  (reduce #(vemap+edge %1 %2) vemap edges))

(defn- propagate
  [vemap ids pos label]
  (let [val (vemap pos)
        labels (:labels val)]
    (if (some #{label} labels)
      vemap
      (let [vemap (assoc-in vemap [pos :labels] (conj labels label))
            edges (:edges val)
            ids (flatten (map maps/participant-ids edges))
            ids (into #{} ids)]
        (if (and (some #{pos} ids) (not= pos label))
          vemap
          (reduce #(propagate %1 ids %2 label) vemap ids))))))

(defn id->edges
  [gbdb id ctxts]
  (filter include-edge
   (db/id->edges gbdb id ctxts)))

(defn intersect
  [gbdb ids ctxts]
  (let [edgesets (map #(id->edges gbdb % ctxts) ids)
        vemap (reduce edges->vemap {} edgesets)
        eids (map #(db/id->eid gbdb %) ids)
        vemap (reduce #(propagate %1 eids %2 %2) vemap eids)
        edges (vals vemap)
        edges (filter #(= (count (:labels %)) (count ids)) edges)
        edges (into #{}
                    (flatten
                     (map :edges edges)))]
    edges))
