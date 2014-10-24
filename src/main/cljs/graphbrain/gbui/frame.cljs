(ns graphbrain.gbui.frame
  (:require-macros [hiccups.core :as hiccups])
  (:require [jayq.core :as jq]
            [hiccups.runtime :as hiccupsrt]
            [graphbrain.gbui.spherical :as spher]
            [graphbrain.gbui.globals :as g]
            [graphbrain.gbui.item :as item]
            [graphbrain.gbui.mat :as mat]
            [graphbrain.gbui.newedges :as newedges])
  (:use [jayq.core :only [$]]))

(hiccups/defhtml frame-html
  [snode-id rel-text]
  [:div {:id snode-id :class "frame"}
   [:div {:class "frame-label"}
    rel-text ":"]
   [:div {:class "frame-inner"}]])

(defn place!
  [snode-pair ctxts]
  (let [snode-id (first snode-pair)
        snode (second snode-pair)
        relpos (:rpos snode)
        rel-text (:label snode)
        html (frame-html snode-id rel-text)]
    (jq/append ($ "#frames") html)
    (doseq [node (:nodes snode)]
      (item/item-place node snode-id snode ctxts))))
