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

(ns graphbrain.web.views.barpage
  (:use hiccup.core
        (graphbrain.web.views page)))

(defn view
  [content-fun]
  (html
   [:div {:id "nodeback"}
    [:div {:id "topbar"}
     [:div {:class "topbar-element topbar-center topbar-vcenter"}
      [:a {:href "/"}
       [:img {:src "/images/GB_logo_XS.png"
              :alt "graphbrain"}]]]

     [:div {:class "topbar-input-area topbar-center topbar-vcenter"}
      [:form {:class "top-input"
              :id "top-input-field"
              :autocomplete "off"
              :action "/eco"
              :method "post"}
       [:input {:type "text"
                :id "main-input-field"
                :class "top-input-field"
                :placeholder "Search or tell me something"
                :name "input-field"
                :autofocus ""}]]]]

    [:div {:class "alert alert-danger"
           :id "msgbar"
           :role "alert"}]
    
    (content-fun)]))

(defn barpage
  [& {:keys [title css-and-js js content-fun]}]
  (page :title title
        :css-and-js css-and-js
        :body-fun #(view content-fun)
        :js js))
