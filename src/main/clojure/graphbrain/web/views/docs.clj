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

(ns graphbrain.web.views.docs
  (:use hiccup.core)
  (:require [graphbrain.web.views.barpage :as bar]))

(def style "
<style>
  body, html {
    background-image:url('../images/bg.png');
    background-attachment: fixed;
  }
  #nodeback {
    background-color: transparent;
  }
</style>
")

(defn view
  [title html-str ctxt]
  (html
   style
   [:br] [:br] [:br] [:br] [:br]
   
   [:div {:class "container"}
    [:div {:class "row"}
     [:div {:class "col-sm-8"
            :style "background-color: rgba(255, 255, 255, 0.90)"}
      html-str]]]

   [:br] [:br]))

(defn docs
  [& {:keys [title css-and-js user ctxt js html]}]
  (bar/barpage :title title
               :css-and-js css-and-js
               :user user
               :ctxt ctxt
               :js js
               :content-fun #(view title html ctxt)))
