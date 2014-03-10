(ns graphbrain.eco.word
  (:import (com.graphbrain.eco Word)))

(defn noun?
  [word]
  (let [pos (. word getPos)]
    (= (clojure.string/lower-case (first pos)) "n")))

(defn adjective?
  [word]
  (let [pos (. word getPos)]
    (= (clojure.string/lower-case (first pos)) "j")))
