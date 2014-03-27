(ns graphbrain.gbui.gbui
  (:use graphbrain.gbui.interface
        graphbrain.gbui.graph
        graphbrain.gbui.animation)
  (:require seedrandom
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

  (init-interface)

  (if (not (or (undefined? js/data) (nil? js/data)))
    (js/initRelations))

  (js/browserSpecificTweaks)

  (if (not (or (undefined? js/data) (nil? js/data)))
    (if (:changedSNode @graph)
      (add-anim (anim-lookat (LchangedSNode @graph)))
      (add-anim (anim-init-rotation)))))

(set! (.-onload js/window) start)
