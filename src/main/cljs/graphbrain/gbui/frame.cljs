(ns graphbrain.gbui.frame
  (:require [jayq.core :as jq]
            [graphbrain.gbui.spherical :as spher]
            [graphbrain.gbui.globals :as g]
            [graphbrain.gbui.node :as node]
            [graphbrain.gbui.mat :as mat]
            [graphbrain.gbui.newedges :as newedges])
  (:use [jayq.core :only [$]]))

(defn place!
  [snode-pair]
  (let [snode-id (first snode-pair)
        snode (second snode-pair)
        relpos (:rpos snode)
        rel-text (:label snode)
        html (str "<div id='" snode-id "' class='frame'>")
        html (if (= relpos 0)
               (str html
                    "<div class='frame-inner'>"
                    "<div class='viewport' /></div>"
                    "<div class='frame-label'>"
                    rel-text
                    "</div></div>")
               (str html
                    "<div class='frame-label'>"
                    rel-text
                    "</div>"
                    "<div class='frame-inner'>"
                    "<div class='viewport' /></div></div>"))]
    (jq/append ($ "#frames") html)
    (doseq [node (:nodes snode)]
      (node/node-place node snode-id snode nil))))
