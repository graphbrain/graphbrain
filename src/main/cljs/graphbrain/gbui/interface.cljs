(ns graphbrain.gbui.interface
  (:require [jayq.core :as jq]
            [graphbrain.gbui.globals :as g]
            [graphbrain.gbui.graph :as graph]
            [graphbrain.gbui.animation :as anim]
            [graphbrain.gbui.remove :as rem]
            [graphbrain.gbui.aichat :as aichat]
            [graphbrain.gbui.search :as search]
            [graphbrain.gbui.user :as user]
            [graphbrain.gbui.alerts :as alerts])
  (:use [jayq.core :only [$]]))

(def dragging (atom false))

(def last-x (atom 0))

(def last-y (atom 0))

(def last-scale (atom -1))

(def scroll (atom false))

(defn scroll-on!
  [e]
  (reset! scroll true))

(defn scroll-off!
  [e]
  (reset! scroll false))

(defn mouse-up
  [e]
  (reset! dragging false)
  false)

(defn mouse-down
  [e]
  (reset! dragging true)
  (reset! last-x (.-pageX e))
  (reset! last-y (.-pageY e))
  (anim/stop-anims!)
  false)

(defn mouse-move
  [e]
  (if @dragging
    (let [page-x (.-pageX e)
          page-y (.-pageY e)
          delta-x (- page-x @last-x)
          delta-y (- page-y @last-y)]
      (reset! last-x page-x)
      (reset! last-y page-y)
      (graph/rotate-x (* (- delta-x) 0.0015))
      (graph/rotate-y (* delta-y 0.0015))
      (graph/update-view)))
  false)

(defn touch-start
  [e]
  (anim/stop-anims)
  (let [touches (.-touches e)]
    (if (= (count touches) 1)
      (let [touch (nth touches 0)]
        (reset! last-x (.-pageX touch))
        (reset! last-y (.-pageY touch))))
    true))

(defn touch-end
  [e]
  (reset! last-scale -1)
  true)

(defn touch-move
  [e]
  (let [touches (.-touches e)
        ntouches (count touches)]
    (cond (= ntouches 1)
      (let [touch (nth touches 0)
            page-x (.-pageX touch)
            page-y (.-pageY touch)
            delta-x (- page-x @last-x)
            delta-y (- page-y @last-y)]
        (.preventDefault e)
        (reset! last-x page-x)
        (reset! last-y page-y)
        (graph/rotate-x (* (- delta-x) * 0.0015))
        (graph/rotate-y (* (- delta-y) * 0.0015))
        (graph/update-view)
        false)
      (= ntouches 2)
      (let [touch0 (nth touches 0)
            touch1 (nth touches 1)
            page-x0 (.-pageX touch0)
            page-x1 (.-pageX touch1)
            page-y0 (.-pageY touch0)
            page-y1 (.-pageY touch1)
            dx (- page-x0 page-x1)
            dy (- page-y0 page-y1)
            scale (Math/sqrt (+ (* dx dx) (* dy dy)))]
        (.preventDefault e)
        (if (>= @last-scale 0)
          (let [x (/ (+ page-x0 page-x1) 2)
                y (/ (+ page-y0 page-y1) 2)
                delta-scale (* (- scale @last-scale) 0.025)]
            (graph/zoom delta-scale x y)))
        (reset! last-scale scale)
        false)
      :else true)))

(defn mouse-wheel
  [e delta delta-x delta-y]
  (if (not @scroll)
    (graph/zoom delta-y (.-pageX e) (.-pageY e)))
  true)

(defn full-bind
  [event-name f]
  (jq/bind ($ "#graph-view") event-name f)
  (jq/bind ($ ".snode1") event-name f)
  (jq/bind ($ ".snodeN") event-name f)
  (jq/bind ($ ".link") event-name f))

(defn init-interface
  []
  (jq/bind ($ "#search-field") "submit" search/search-query)
  (search/init-search-dialog!)
  (user/init-signup-dialog!)
  (jq/bind ($ ".signupLink") "click" user/show-signup-dialog!)
  (jq/bind ($ "#loginLink") "click" user/show-signup-dialog!)
  (jq/bind ($ "#logoutLink") "click" user/logout!)
  (full-bind "mouseup" mouse-up)
  (full-bind "mousedown" mouse-down)
  (full-bind "mousemove" mouse-move)
  (full-bind "mousewheel" mouse-wheel)
  (.addEventListener js/document "touchstart" touch-start)
  (.addEventListener js/document "touchend" touch-end)
  (.addEventListener js/document "touchmove" touch-move)
  (alerts/init-alert!)
  (if (exists? js/data)
    (rem/init-remove-dialog
     (:id (first (get-in @g/graph [:snodes "root" :nodes]))))
    (js/bind ($ "#ai-chat-button") "click" aichat/ai-chat-button-pressed))
  (if (and (exists? js/errorMsg) (not (empty? js/errorMsg)))
    (alerts/set-error-alert! js/errorMsg)))
