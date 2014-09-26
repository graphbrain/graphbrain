(ns graphbrain.utils
  (:import (java.io StringWriter PrintWriter)))

(defmacro dbg[x] `(let [x# ~x] (println "dbg:" '~x "=" x#) x#))

(defn exception->str
  [e]
  (let [sw (StringWriter.)
        pw (PrintWriter. sw)]
    (.printStackTrace e pw)
    (.toString sw)))

(defn map-map-vals
  [f m]
  (into {} (for [[k v] m] [k (f v)])))

(defn iteration->seq
  [iteration]
  (seq
   (reify java.lang.Iterable 
     (iterator [this] 
       (reify java.util.Iterator
         (hasNext [this] (.hasNext iteration))
         (next [this] (.next iteration))
         (remove [this] (.remove iteration)))))))
