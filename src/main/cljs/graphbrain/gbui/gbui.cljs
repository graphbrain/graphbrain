(ns graphbrain.gbui.gbui
  (:require [graphbrain.gbui.interface :as intf]
            [graphbrain.gbui.globals :as g]
            [graphbrain.gbui.graph :as graph]
            [graphbrain.gbui.animation :as anim]
            [graphbrain.gbui.relations :as rels]
            [graphbrain.gbui.aichat :as aichat]
            [graphbrain.gbui.newedges :as newedges]
            [graphbrain.gbui.node :as node]
            [graphbrain.gbui.contexts :as contexts]
            [graphbrain.gbui.inters :as inters]
            [graphbrain.gbui.nodepage :as nodepage]
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
  
  (if (= js/ptype "node3d")
    (graph/init-graph!))

  (intf/init-interface)
  
  (if (= js/ptype "node3d")
    (do (rels/init-relations!)
        (aichat/init-ai-chat!)
        (contexts/init-contexts!)))
 
  (if (= js/ptype "node3d")
    (do (if @g/changed-snode
       (anim/add-anim! (anim/anim-lookat @g/changed-snode))
       (anim/add-anim! (anim/anim-init-rotation)))
     (doseq [node-id (newedges/new-edges)]
       (anim/add-anim! (anim/anim-node-glow (node/node-div-id node-id)))
       (newedges/clean))))

  (if (= js/ptype "intersect")
    (anim/add-anim! (anim/anim-graph-layout))))

($ start)
