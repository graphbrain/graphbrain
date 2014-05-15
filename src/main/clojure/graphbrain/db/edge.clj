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
      :score score
      :degree -1
      :ts -1})
  ([id]
     (id->edge id 1)))

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
