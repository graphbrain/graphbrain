(ns graphbrain.db.edge
  (:require [graphbrain.db.edgetype :as edgetype]
            [graphbrain.db.id :as id]))

(defn ids
  [edge]
  (id/id->ids (:id edge)))

(defn participant-ids
  [edge]
  (rest (ids edge)))

(defn edge-type
  [edge]
  (first (ids edge)))

(defn negative?
  [edge]
  (edgetype/negative? (edge-type edge)))

(defn positive?
  [edge]
  (not (negative? edge)))

(defn id->edge
  ([id score]
     {:id id
      :type :edge
      :score score})
  ([id]
     (id->edge id 1)))

(defn idedge->entity
  [typerel]
  (if (= (edge-type typerel) "r/+id")
    (let [hsh (id/hashed (:id typerel))
          name (first (participant-ids typerel))
          id (id/build [hsh name])]
      {:id id
       :type :entity
       :typerel (:id typerel)})))

(defn idedge-id->entity
  [idedge-id]
  (if (= (id/id->type idedge-id) :edge)
    (idedge->entity (id->edge idedge-id))))

(defn ids->edge
  ([ids score]
     (id->edge (id/ids->id ids) score))
  ([ids]
     (ids->edge ids 1)))

(defn negate
  [edge]
  (ids->edge (cons (edgetype/negate (edge-type edge)) (participant-ids edge))))

(defn matches?
  [edge pattern]
  (every? identity
          (map #(or (= %2 "*") (= %1 %2)) (ids edge) pattern)))

(defn owner
  [edge]
  (id/owner (second (ids edge))))
