(ns graphbrain.utils)

(defn map-map-vals
  [f m]
  (into {} (for [[k v] m] [k (f v)])))
