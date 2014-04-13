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
