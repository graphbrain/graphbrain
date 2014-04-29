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
  ([id degree ts]
     {:id id
      :type :edge
      :degree degree
      :ts ts})
  ([id]
     (id->edge id 0 -1)))

(defn ids->edge
  ([ids degree ts]
     (id->edge (id/ids->id ids) degree ts))
  ([ids]
     (ids->edge ids 0 -1)))

(defn negate
  [edge]
  (ids->edge (cons (edgetype/negate (edge-type edge)) (participant-ids edge))))

(defn matches?
  [edge pattern]
  (every? identity
          (map #(or (= %2 "*") (= %1 %2)) (ids edge) pattern)))
