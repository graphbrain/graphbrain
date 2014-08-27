(ns graphbrain.gbui.bubble
  (:require-macros [hiccups.core :as hiccups])
  (:require [jayq.core :as jq]
            [hiccups.runtime :as hiccupsrt]
            [graphbrain.gbui.globals :as g])
  (:use [jayq.core :only [$]]))

(defn bubble-id
  [id]
  (let [bid (clojure.string/replace id "/" "_")]
    (str "bub_" bid)))

(hiccups/defhtml bubble-template
  [id title]
  [:div {:id (bubble-id id)
         :class "bubble"} title])

(defn bubble-size
  [bid]
  (let [bub-div ($ (str "#" bid))
        width (jq/width bub-div)
        height (jq/height bub-div)]
    [width height]))

(defn move-bubble!
  [bid pos]
  (let [bsize (bubble-size bid)
        half-width (/ (first g/world-size) 2)
        half-height (/ (second g/world-size) 2)
        half-bwidth (/ (first bsize) 2)
        half-bheight (/ (second bsize) 2)
        x (- (+ (first pos) half-width) half-bwidth)
        y (- (+ (second pos) half-height) half-bheight)
        transform-str (str "translate(" x "px," y "px)")
        bub-div ($ (str "#" bid))]
    (jq/css bub-div {:transform transform-str})))

(defn place-bubble!
  [bubble]
  (jq/append ($ "#view-view") (bubble-template (:id bubble)
                                               (pr-str bubble)))
  (move-bubble! (bubble-id (:id bubble)) [0 0]))
