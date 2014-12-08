(ns graphbrain.gbui.bubble
  (:require-macros [hiccups.core :as hiccups])
  (:require [jayq.core :as jq]
            [hiccups.runtime :as hiccupsrt]
            [graphbrain.gbui.globals :as g]
            [graphbrain.gbui.id :as id]
            [graphbrain.gbui.item :as item])
  (:use [jayq.core :only [$]]))

(defn bubble-id
  [id]
  (str "bub_" (id/clean id)))

(hiccups/defhtml bubble-template
  [id html seed]
  (let [classes (if seed
                  "bubble seed-bubble"
                  "bubble")]
    [:div {:id (bubble-id id)
           :class classes} html]))

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
  [bubbles bid pos visual]
  (if visual
    (let [bsize (bubble-size bid)
          half-size (map #(/ % 2) g/world-size)
          half-bsize (map #(/ % 2) bsize)
          trans (map #(- (+ %1 %2) %3) pos half-size half-bsize)
          transform-str (str "translate(" (first trans) "px," (second trans) "px)")
          bub-div ($ (str "#" bid))]
      (jq/css bub-div {:transform transform-str})))
  (assoc-in bubbles [bid :pos] pos))

(defn- new-bubble
  []
  {:pos [0 0]
   :v [0 0]})

(defn random-pos!
  [bubbles bid]
  (move-bubble! bubbles bid [(+ -60 (rand-int 120))
                             (+ -20 (rand-int 40))]
                false))

(defn place-bubble!
  [bubbles bubble seeds]
  (let [bid (bubble-id (:id bubble))
        bubbs (assoc bubbles bid (new-bubble))]
    (jq/append ($ "#inters-view")
               (bubble-template (:id bubble)
                                (item/item-html bubble "xxx")
                                (some #{(:id bubble)} seeds)))
    (random-pos! bubbs bid)))

(defn layout-step!
  [bubbles bid visual]
  (let [bubble (bubbles bid)
        pos (:pos bubble)
        v (:v bubble)
        pos (map + pos v)]
    (move-bubble! bubbles bid pos visual)))
