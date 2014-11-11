(ns graphbrain.gbui.inters
  (:require [jayq.core :as jq]
            [graphbrain.gbui.bubble :as bubble]
            [graphbrain.gbui.globals :as g])
  (:use [jayq.core :only [$]]))

(def scale 1)

(defn- view-size
  []
  (let [view-view ($ "#inters-view")
        width (jq/width view-view)
        height (jq/height view-view)]
    [width height]))

(defn- place-bubbles!
  [bubbles]
  (doseq [bubble bubbles]
    (bubble/place-bubble! bubble)))

(defn move-world!
  [pos scale]
  (let [half-size (map #(/ % 2) @g/view-size)
        half-wsize (map #(/ % 2) g/world-size)
        trans (map - half-size half-wsize)
        transform-str (str "translate(" (first trans) "px," (second trans) "px) "
                           "scale(" scale ")")
        view-div ($ "#inters-view")]
    (jq/css view-div {:width (str (first g/world-size) "px")
                      :height (str (second g/world-size) "px")
                      :transform transform-str})))

(defn on-scroll-world
  [event]
  (let [delta (.-wheelDelta (.-originalEvent event))]
    (def scale (* scale (Math/pow 0.9995 delta))))
  (move-world! [0 0] scale)
  false)

(defn on-reply
  [reply]
  (.log js/console (pr-str @g/last-pos))
  (let [bubble (cljs.reader/read-string reply)
        bubble (assoc bubble :pos @g/last-pos)]
    (bubble/place-bubble! bubble)))

(defn on-mouse-up
  [event]
  (let [oev (.-originalEvent event)
        page [(.-pageX oev) (.-pageY oev)]
        xxx (.log js/console (str "page: " (pr-str page)))
        view ($ "#inters-view")
        parent-offset (.offset view)
        offset [(.-left parent-offset) (.-top parent-offset)]
        xxx (.log js/console (str "offset: " (pr-str offset)))
        pos (map - page offset)
        xxx (.log js/console (str "pos1: " (pr-str pos)))
        half-size (map #(/ % 2) g/world-size)
        pos (map - pos half-size)
        xxx (.log js/console (str "pos2: " (pr-str pos)))]
    (reset! g/last-pos (into [] pos)))
  (jq/ajax {:url "/bubble"
            :success on-reply
            :contentType "text"
            :data @g/origin}))

(defn bind-events!
  []
  (jq/bind ($ "#data-view") "mousewheel" on-scroll-world)
  (jq/bind ($ "#inters-view") "mouseup" on-mouse-up))

(defn init-view!
  [view-data-str]
  (reset! g/view-size (view-size))
  (move-world! [0 0] 1)
  (bind-events!)
  (def data (cljs.reader/read-string view-data-str))
  (place-bubbles! (:vertices data)))
