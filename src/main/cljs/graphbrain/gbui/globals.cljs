(ns graphbrain.gbui.globals)

(defonce graph (atom nil))

(defonce graph-vis (atom nil))

(defonce changed-snode (atom nil))

(defonce rng (atom nil))

(defonce view-size (atom nil))

(def world-size [5000 5000])

(def origin (atom nil))

(def last-pos (atom nil))
