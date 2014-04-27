(ns graphbrain.db.edge
  (:require [graphbrain.db.edgetype :as edgetype]
            [graphbrain.db.id :as id]))

(defn participant-ids
  [edge]
  (rest (:ids edge)))

(defn edge-type
  [edge]
  (first (:ids edge)))

(defn negative?
  [edge]
  (edgetype/negative? (edge-type edge)))

(defn positive?
  [edge]
  (not (negative? edge)))

(defn ids->edge
  ([ids degree ts]
     {:id (id/ids->id ids)
      :ids ids
      :degree degree
      :ts ts})
  ([ids]
     (ids->edge ids 0 -1)))

(defn id->edge
  ([id degree ts]
     {:id id
      :ids (id/id->ids id)
      :degree degree
      :ts ts})
  ([id]
     (id->edge id 0 -1)))

(defn negate
  [edge]
  (ids->edge (cons (str "neg/" (edge-type edge)) (participant-ids edge))))

(defn matches?
  [edge pattern]
  (every? identity
          (map #(or (= %2 "*") (= %1 %2)) (:ids edge) (:ids pattern))))
