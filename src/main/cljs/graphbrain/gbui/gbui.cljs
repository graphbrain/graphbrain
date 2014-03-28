(ns graphbrain.gbui.gbui
  (:require [graphbrain.gbui.interface :as intf]
            [graphbrain.gbui.graph :as graph]
            [graphbrain.gbui.animation :as anim]
            seedrandom
            jquery.cookie
            jquery.mousewheel
            slimscroll
            browsers
            vec3mat4x4
            quaternion
            alerts
            layout
            search
            disambiguate
            undo
            user
            relations
            aichat
            state))

(defn start
  []
  (Math/seedrandom "GraphBrain GraphBrain")

  (set! js/state (js/State.))

  (intf/init-interface)

  (if (not (or (undefined? js/data) (nil? js/data)))
    (js/initRelations))

  (js/browserSpecificTweaks)

  (if (not (or (undefined? js/data) (nil? js/data)))
    (if (:changedSNode @graph/graph)
      (anim/add-anim (anim/anim-lookat (:changedSNode @graph/graph)))
      (anim/add-anim (anim/anim-init-rotation)))))

(set! (.-onload js/window) start)
