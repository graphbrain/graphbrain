(ns graphbrain.gbui.nodepage
  (:require [jayq.core :as jq]
            [graphbrain.gbui.globals :as g]
            [graphbrain.gbui.frame :as frame])
  (:use [jayq.core :only [$]]))

(defn- place-frames!
  [snodes ctxts]
  (doseq [snode snodes]
    (frame/place! snode ctxts)))

(defn init-nodepage!
  []
  (reset! g/root-id (:id (:root @g/data)))
  (place-frames! (:snodes @g/data) (:ctxts @g/data)))
