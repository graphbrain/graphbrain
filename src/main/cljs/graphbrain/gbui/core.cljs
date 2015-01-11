(ns graphbrain.gbui.gbui
  (:require [graphbrain.gbui.interface :as intf]
            [graphbrain.gbui.globals :as g]
            [graphbrain.gbui.animation :as anim]
            [graphbrain.gbui.inters :as inters]
            [graphbrain.gbui.nodepage :as nodepage]
            [graphbrain.gbui.eco :as eco]
            [graphbrain.gbui.encoder :as enc]
            [cemerick.pprng :as rng])
  (:use [jayq.core :only [$]]))

(defn- init-data
  [data-str]
  (let [data (cljs.reader/read-string
              (enc/decode data-str))]
    (reset! g/data data)
    (reset! g/context (:id (:context data)))))

(defn start
  []
  (reset! g/rng (rng/rng "GraphBrain GraphBrain"))

  (if (some #{js/ptype} ["node" "intersect" "brain"])
    (init-data js/data))
  
  (case js/ptype
    "node" (nodepage/init-nodepage!)
    "intersect" (do (inters/init-view!)
                    (anim/add-anim! (anim/anim-graph-layout)))
    "eco" (eco/init-eco!)
    nil)
  
  (intf/init-interface))

($ start)
