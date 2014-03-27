(ns graphbrain.gbui.interface
  (:use graphbrain.gbui.animation))

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
  (stop-anims)
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
      (rotate-x (* (- delta-x) 0.0015))
      (rotate-y (* (- delta-y) 0.0015))
      (update-view)))
  false)

(defn touch-start
  [e]
  (stop-anims)
  (let [touches (.-touches e)]
    (if (= (count touches) 1)
      (let [touch = (nth touches 0)]
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
        (rotate-x (* (- delta-x) * 0.0015))
        (rotate-y (* (- delta-y) * 0.0015))
        (update-view)
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
                y (/ (+ page-y0 page-y1) 2)]
            (reset! delta-scale (* (- scale @last-scale) 0.025))
            (zoom @delta-scale x y)))
        (reset! last-scale scale)
        false)
      :else true)))

(defn mouse-wheel
  [e delta delta-x delta-y]
  (if (not @scroll)
    (zoom delta-y (.-pageX e) (.-pageY e)))
  true)

(defn full-bind
  [event-name f]
  (jq/bind ($ "#graph-view") event-name f)
  (jq/bind ($ ".snode1") event-name f)
  (jq/bind ($ ".snodeN") event-name f)
  (jq/bind ($ ".link") event-name f))

(defn init-interface
  []
  (jq/submit ($ "#search-field") js/searchQuery)
  (js/initSearchDialog)
  (js/initSignUpDialog)
  (jq/bind ($ ".signupLink") "click" js/showSignUpDialog)
  (jq/bind ($ "#loginLink") "click" js/showSignUpDialog)
  (jq/bind ($ "#logoutLink") "click" js/logout)
  (full-bind "mouseup" mouse-up)
  (full-bind "mousedown" mouse-down)
  (full-bind "mousemove" mouse-move)
  (full-bind "mousewheel" mouse-wheel)
  (.addEventListener js/document "touchstart" touch-start)
  (.addEventListener js/document "touchend" touch-end)
  (.addEventListener js/document "touchmove" touch-move)
  (js/initAlert)
  (if (not (nil? js/data))
    (js/initAiChat)
    (js/initRemoveDialog)
    (js/initDisambiguateDialog)
    (js/bind ($ "#ai-chat-button") "click" js/aiChatButtonPressed))
  (if (not (or (nil? js/errorMsg) (empty? js/errorMsg)))
    (js/setErrorAlert js/errorMsg)))
