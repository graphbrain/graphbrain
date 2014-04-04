(ns graphbrain.gbui.gbui
  (:require [graphbrain.gbui.interface :as intf]
            [graphbrain.gbui.globals :as g]
            [graphbrain.gbui.graph :as graph]
            [graphbrain.gbui.animation :as anim]
            [graphbrain.gbui.relations :as rels]
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
            user
            aichat
            state))

(defn start
  []
  (Math/seedrandom "GraphBrain GraphBrain")

  (set! js/state (js/State.))

  (intf/init-interface)

  (js/browserSpecificTweaks)

  (graph/init-graph!)

  (if (not (or (undefined? js/data) (nil? js/data)))
    (rels/init-relations!))
  
 (if (and (exists? js/data) (not (nil? js/data)))
    (if (:changedSNode @g/graph)
      (anim/add-anim! (anim/anim-lookat (:changedSNode @g/graph)))
      (anim/add-anim! (anim/anim-init-rotation)))))

(set! (.-onload js/window) start)
