(ns graphbrain.gbui.gbui
  (:require seedrandom
            jquery.cookie
            jquery.mousewheel
            slimscroll
            browsers
            vec3mat4x4
            quaternion
            alerts
            animation
            interf
            node
            sphericalcoords
            snode
            layout
            graph
            search
            disambiguate
            undo
            user
            relations
            aichat
            remove
            state))

(defn start
  []
  (Math/seedrandom "GraphBrain GraphBrain")

  (set! js/state (js/State.))

  (set! js/g (if (or (undefined? js/data) (nil? js/data))
           nil
           (. js/Graph initGraph (. js/state getNewEdges))))
  
  (js/initInterface)

  (if (not (or (undefined? js/data) (nil? js/data)))
    (js/initRelations))

  (js/browserSpecificTweaks)

  (if (not (or (undefined? js/data) (nil? js/data)))
    (if (.-changedSNode js/g)
      (js/addAnim (js/AnimLookAt. (.-changedSNode js/g)))
      (js/addAnim (js/AnimInitRotation.)))))

(set! (.-onload js/window) start)
