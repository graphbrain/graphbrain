(ns graphbrain.gbui.view
  (:require [jayq.core :as jq]
            [graphbrain.gbui.bubble :as bubble]
            [graphbrain.gbui.globals :as g])
  (:use [jayq.core :only [$]]))

(def scale 1)

(defn- view-size
  []
  (let [view-view ($ "#view-view")
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
        view-div ($ "#view-view")]
    (jq/css view-div {:width (str (first g/world-size) "px")
                      :height (str (second g/world-size) "px")
                      :transform transform-str})))

(defn on-scroll-world
  [event]
  (let [delta (.-wheelDelta (.-originalEvent event))]
    (def scale (* scale (Math/pow 0.9995 delta))))
  (move-world! [0 0] scale)
  false)

(defn init-view!
  [view-data-str]
  (reset! g/view-size (view-size))
  (move-world! [0 0] 1)
  (jq/bind ($ "#data-view") "mousewheel" on-scroll-world)
  (def data (cljs.reader/read-string view-data-str))
  (place-bubbles! (:bubbles data)))
