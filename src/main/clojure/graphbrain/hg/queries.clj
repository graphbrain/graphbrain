(ns graphbrain.hg.queries
  (:require [graphbrain.hg.ops :as hgops]
            [graphbrain.hg.id :as id]
            [graphbrain.utils :as u]))

(def exclude-set
  #{"653d14f5d5a58462/noun"
    "e2665974fa3ff255/much"
    "10bb96ba951ac820/more"
    "other"})

(defn- exclude-edge
  [edge]
  #_(some exclude-set
        (map id/eid->id (maps/participant-ids edge))))

(defn- include-edge
  [edge]
  (not (exclude-edge edge)))

(defn- add-neighbours
  [neighbours edge]
  #_(clojure.set/union neighbours
                     (into #{}
                           (maps/participant-ids edge))))

(defn- vemap-assoc-id-edge
  [vemap id edge]
  (let [vemap (update-in vemap [id :edges] #(conj % edge))
        vemap (update-in vemap [id :neighbours] add-neighbours edge)]
    vemap))

(defn- vemap+edge
  [vemap edge]
  #_(reduce #(vemap-assoc-id-edge %1 %2 edge)
          vemap
          (maps/participant-ids edge)))

(defn- edges->vemap
  [vemap edges]
  (reduce #(vemap+edge %1 %2) vemap edges))

(defn id->edges
  [gbdb id ctxts]
  #_(filter include-edge
          (db/id->edges gbdb id ctxts 2)))

(defn- interedge
  [edge interverts]
  #_(let [ids (maps/participant-ids edge)]
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

(defn- walk-step->wmap
  [wmap walk-length step]
  (let [clen (wmap step)]
    (if (or (not clen) (< walk-length clen))
      (assoc wmap step walk-length)
      wmap)))

(defn- walk->wmap
  [wmap walk]
  (let [walk-length (count walk)]
    (reduce #(walk-step->wmap %1 walk-length %2)
            wmap walk)))

(defn- walks->wmap
  [walks]
  (reduce walk->wmap {} walks))

(defn- valid-step?
  [wmap ids walk-length step]
  (or (some #{step} ids)
      (= walk-length (wmap step))))

(defn- valid-walk?
  [wmap ids walk]
  (let [walk-length (count walk)]
    (every? #(valid-step? wmap ids walk-length %) walk)))

(defn intersect
  [gbdb ids ctxts]
  #_(let [ids (map id/local->global ids)
        edgesets (map #(id->edges gbdb % ctxts) ids)
        vemap (reduce edges->vemap {} edgesets)
        eids (map #(db/id->eid gbdb %) ids)
        walks (walks vemap eids [(first eids)] #{})
        wmap (walks->wmap walks)
        walks (filter #(valid-walk? wmap eids %) walks)
        interverts (into #{}
                         (flatten
                          (into [] walks)))
        edges (vals vemap)
        edges (mapcat identity (map :edges edges))
        edges (filter #(interedge % interverts) edges)]
    edges))
