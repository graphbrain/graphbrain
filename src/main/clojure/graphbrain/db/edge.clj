(ns graphbrain.db.edge
  (:require [graphbrain.db.id :as id]
            [graphbrain.db.maps :as maps]))

(defn matches?
  [edge pattern]
  (every? identity
          (map #(or (= %2 "*") (= %1 %2)) (maps/ids edge) pattern)))

(defn context
  [edge]
  (id/context
   (second
    (maps/ids edge))))
