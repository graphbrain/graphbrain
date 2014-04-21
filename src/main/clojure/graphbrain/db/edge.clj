(ns graphbrain.db.edge
  (:require [graphbrain.db.graph :as gb])
  (:import (com.graphbrain.db Edge)))

(defn ids->edge-id
  [ids]
  (str "(" (clojure.string/join " " ids) ")"))

(defn participant-ids
  [edge]
  (rest (:ids edge)))

(defn edge-type
  [edge]
  (first (:ids edge)))

(defn positive?
  [edge]
  (.isPositive (gb/map->edge-obj edge)))

(defn id->edge
  [id]
  (gb/edge-obj->map (Edge/fromId id)))

(defn ids->edge
  [ids]
  (id->edge (ids->edge-id ids)))

(defn matches?
  [edge pattern]
  (every? identity
          (map #(or (= %2 "*") (= %1 %2)) (:ids edge) (:ids pattern))))
