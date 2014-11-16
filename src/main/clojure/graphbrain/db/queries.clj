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

(defn- add-neighbours
  [neighbours edge]
  (clojure.set/union neighbours
                     (into #{}
                           (maps/participant-ids edge))))

(defn- vemap-assoc-id-edge
  [vemap id edge]
  (let [vemap (update-in vemap [id :edges] #(conj % edge))
        vemap (update-in vemap [id :neighbours] add-neighbours edge)]
    vemap))

(defn- vemap+edge
  [vemap edge]
  (reduce #(vemap-assoc-id-edge %1 %2 edge)
          vemap
          (maps/participant-ids edge)))

(defn- edges->vemap
  [vemap edges]
  (reduce #(vemap+edge %1 %2) vemap edges))

(defn id->edges
  [gbdb id ctxts]
  (filter include-edge
          (db/id->edges gbdb id ctxts 2)))

(defn- interedge
  [edge interverts]
  (let [ids (maps/participant-ids edge)]
    (every? #(some #{%} interverts) ids)))

(defn walks
  [vemap ids walk all-walks]
  (if (every? #(some #{%} walk) ids)
    (conj all-walks walk)
    (let [neighbours (:neighbours (vemap (last walk)))
         next-steps (filter #(not (some #{%} walk)) neighbours)]
     (if (empty? next-steps)
       all-walks
       (reduce #(clojure.set/union %1
                                   (walks vemap ids
                                          (conj walk %2)
                                          #{}))
               all-walks next-steps)))))

(defn intersect
  [gbdb ids ctxts]
  (let [edgesets (map #(id->edges gbdb % ctxts) ids)
        vemap (reduce edges->vemap {} edgesets)
        eids (map #(db/id->eid gbdb %) ids)
        walks (walks vemap eids [(first eids)] #{})
        interverts (into #{}
                         (flatten
                          (into [] walks)))
        edges (vals vemap)
        edges (mapcat identity (map :edges edges))
        edges (filter #(interedge % interverts) edges)]
    edges))
