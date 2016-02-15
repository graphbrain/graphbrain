;   Copyright (c) 2016 CNRS - Centre national de la recherche scientifique.
;   All rights reserved.
;
;   Written by Telmo Menezes <telmo@telmomenezes.com>
;
;   This file is part of GraphBrain.
;
;   GraphBrain is free software: you can redistribute it and/or modify
;   it under the terms of the GNU Affero General Public License as published by
;   the Free Software Foundation, either version 3 of the License, or
;   (at your option) any later version.
;
;   GraphBrain is distributed in the hope that it will be useful,
;   but WITHOUT ANY WARRANTY; without even the implied warranty of
;   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;   GNU Affero General Public License for more details.
;
;   You should have received a copy of the GNU Affero General Public License
;   along with GraphBrain.  If not, see <http://www.gnu.org/licenses/>.

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

(defn- dist
  [p1 p2]
  (let [dx (- (first p1) (first p2))
        dy (- (second p1) (second p2))
        dx2 (* dx dx)
        dy2 (* dy dy)]
    (Math/sqrt
     (+ dx2 dy2))))

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
  (let [bubble (bubbles bid)
        old-pos (:pos bubble)
        new-bubble (assoc bubble
                     :pos pos
                     :delta (dist old-pos pos))]
    (assoc bubbles bid new-bubble)))

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
