(ns graphbrain.gbui.bubble
  (:require-macros [hiccups.core :as hiccups])
  (:require [jayq.core :as jq]
            [hiccups.runtime :as hiccupsrt]
            [graphbrain.gbui.globals :as g]
            [graphbrain.gbui.item :as item])
  (:use [jayq.core :only [$]]))

(defn bubble-id
  [id]
  (let [bid (clojure.string/replace id "/" "_")]
    (str "bub_" bid)))

(hiccups/defhtml bubble-template
  [id html]
  [:div {:id (bubble-id id)
         :class "bubble"} html])

(hiccups/defhtml bubble-title-template
  [node]
  [:div {:class "bubble-title"} (:text node)])

(hiccups/defhtml bubble-body-template
  [html]
  [:div {:class "bubble-body"} html])

(defn bubble-size
  [bid]
  (let [bub-div ($ (str "#" bid))
        width (jq/width bub-div)
        height (jq/height bub-div)]
    [width height]))

(defn move-bubble!
  [bid pos]
  (let [bsize (bubble-size bid)
        half-size (map #(/ % 2) g/world-size)
        half-bsize (map #(/ % 2) bsize)
        trans (map #(- (+ %1 %2) %3) pos half-size half-bsize)
        transform-str (str "translate(" (first trans) "px," (second trans) "px)")
        bub-div ($ (str "#" bid))]
    (jq/css bub-div {:transform transform-str})))

(defn place-bubble!
  [bubble]
  (let [bid (bubble-id (:id bubble))]
    (jq/append ($ "#inters-view")
               (bubble-template (:id bubble)
                (item/item-html bubble "xxx")))
    (move-bubble! bid [0 0])))
