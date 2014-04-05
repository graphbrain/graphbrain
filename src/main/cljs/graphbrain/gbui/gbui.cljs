(ns graphbrain.gbui.gbui
  (:require [graphbrain.gbui.interface :as intf]
            [graphbrain.gbui.globals :as g]
            [graphbrain.gbui.graph :as graph]
            [graphbrain.gbui.animation :as anim]
            [graphbrain.gbui.relations :as rels]
            [graphbrain.gbui.aichat :as aichat]
            seedrandom
            jquery.cookie
            jquery.mousewheel
            slimscroll
            browsers
            quaternion
            alerts
            search
            disambiguate
            undo
            user))

(defn start
  []
  (Math/seedrandom "GraphBrain GraphBrain")

  (intf/init-interface)

  (js/browserSpecificTweaks)

  (graph/init-graph!)

  (if (not (or (undefined? js/data) (nil? js/data)))
    (do (rels/init-relations!)
        (aichat/init-ai-chat!)))
  
 (if (and (exists? js/data) (not (nil? js/data)))
    (if (:changedSNode @g/graph)
      (anim/add-anim! (anim/anim-lookat (:changedSNode @g/graph)))
      (anim/add-anim! (anim/anim-init-rotation)))))

(set! (.-onload js/window) start)
