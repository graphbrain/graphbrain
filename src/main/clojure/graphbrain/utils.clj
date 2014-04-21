(ns graphbrain.utils
  (:import (java.io StringWriter PrintWriter)))

(defn exception->str
  [e]
  (let [sw (StringWriter.)
        pw (PrintWriter. sw)]
    (.printStackTrace e pw)
    (.toString sw)))

(defn map-map-vals
  [f m]
  (into {} (for [[k v] m] [k (f v)])))
