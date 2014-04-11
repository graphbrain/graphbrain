(ns graphbrain.gbui.gbui
  (:require [graphbrain.gbui.interface :as intf]
            [graphbrain.gbui.globals :as g]
            [graphbrain.gbui.graph :as graph]
            [graphbrain.gbui.animation :as anim]
            [graphbrain.gbui.relations :as rels]
            [graphbrain.gbui.aichat :as aichat]
            [graphbrain.gbui.newedges :as newedges]
            [graphbrain.gbui.node :as node]
            seedrandom)
  (:use [jayq.core :only [$]]))

(defn start
  []
  (Math/seedrandom "GraphBrain GraphBrain")

  (if (and (exists? js/data) (not (nil? js/data)))
    (graph/init-graph!))

  (intf/init-interface)
  
  (if (not (or (undefined? js/data) (nil? js/data)))
    (do (rels/init-relations!)
        (aichat/init-ai-chat!)))
 
  (if (and (exists? js/data) (not (nil? js/data)))
    (do (if @g/changed-snode
       (anim/add-anim! (anim/anim-lookat @g/changed-snode))
       (anim/add-anim! (anim/anim-init-rotation)))
     (doseq [node-id (newedges/new-edges)]
       (anim/add-anim! (anim/anim-node-glow (node/node-div-id node-id)))
       (newedges/clean)))))

($ start)
