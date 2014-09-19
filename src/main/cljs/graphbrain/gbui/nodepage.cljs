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
  [view-data-str]
  (def data (cljs.reader/read-string view-data-str))
  (place-frames! (:snodes data) (:ctxts data)))
