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

(defn start
  []
  (reset! g/rng (rng/rng "GraphBrain GraphBrain"))

  (if (= js/ptype "node")
    (nodepage/init-nodepage! (enc/decode js/data)))
  
  (if (= js/ptype "intersect")
    (inters/init-view! (enc/decode js/data)))

  (if (= js/ptype "eco")
    (eco/init-eco!))
  
  (intf/init-interface)
  
  (if (= js/ptype "intersect")
    (anim/add-anim! (anim/anim-graph-layout))))

($ start)
