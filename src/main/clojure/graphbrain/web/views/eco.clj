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

(ns graphbrain.web.views.eco
  (:use hiccup.core)
  (:require [graphbrain.web.views.barpage :as bar]))

(defn sentence
  [words]
  (interpose " "
             (map #(vector :span {:class "eco-word"}
                           (:word %)
                           "/"
                           [:span {:class "eco-pos"} (:pos %)]
                           "/"
                           [:span {:class "eco-lemma"} (:lemma %)]) words)))

(defn trace
  [rs]
  (map #(vector :div
                [:span {:class "eco-weight"} (:weight %)]
                " "
                [:span {:class "eco-rule"} (:desc (:rule %))]
                " "
                [:span {:class "eco-vertex"} (:vert %)]
                [:div {:class "eco-trace-box"}
                 (let [v (:vertex %)]
                   (if (coll? v)
                     (trace v) v))])
       (sort-by #(if (:weight %)
                   (:weight %) 0) > rs)))

(defn view
  [report]
  (html
   [:div {:id "eco-results"}
    [:div {:class "eco-section"} (sentence (:words report))]
    [:div {:class "eco-section"} (:res report)]
    [:div {:class "eco-section"} (:id (:edge report))]
    [:div {:class "eco-section"} (trace (:vws report))]]))

(defn page
  [& {:keys [title css-and-js user ctxt js report]}]
  (bar/barpage :title title
               :css-and-js css-and-js
               :user user
               :ctxt ctxt
               :js js
               :content-fun #(view report)))
