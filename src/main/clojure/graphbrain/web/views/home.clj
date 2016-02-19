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

(ns graphbrain.web.views.home
  (:use hiccup.core))

(defn view []
  (html
   [:br]
    [:div
     [:div {:class "hero-unit landing" :style "background-color: rgba(255, 255, 255, 0.8)"}
       [:div
         [:div {:style "float:left; height:180px; padding-right:70px"}
           [:img {:src "/images/GB_logo_L.png" :width "209" :height "56" :alt "GraphBrain"}]]
         [:div {:style "color:#555; padding-top:0px; margin-top:0px;"}]]
       [:div {:style "text-align:center; clear:both;"}
         [:h1 "Graphbrain"]]
       [:div {:style "text-align:center"}
         [:h3 "Open Knowledge Hypergraph"]]]]))
