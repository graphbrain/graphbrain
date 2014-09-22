(ns graphbrain.gbui.nodepage
  (:require [jayq.core :as jq]
            [graphbrain.gbui.globals :as g]
            [graphbrain.gbui.frame :as frame]
            [graphbrain.gbui.input :as input])
  (:use [jayq.core :only [$]]))

(defn- place-frames!
  [snodes ctxts]
  (doseq [snode snodes]
    (frame/place! snode ctxts)))

(defn init-nodepage!
  [view-data-str]
  (let [data (cljs.reader/read-string view-data-str)]
    (reset! g/root-id (:id (:root data)))
    (place-frames! (:snodes data) (:ctxts data)))
  (jq/bind ($ "#top-input-field") "submit" input/query))
