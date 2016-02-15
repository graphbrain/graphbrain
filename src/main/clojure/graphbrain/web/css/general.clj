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

(ns graphbrain.web.css.general
  (:require [garden.units :refer [px]]))

(def css
  [[:html :body
   {:width "100%"
    :height "100%"
    :margin (px 0)
    :font-family "Helvetica, sans-serif"
    :font-size (px 12)
    :background "#EEE"}]

  [:.landing
   {:padding (px 50)
    :margin (px 20)
    :border-radius (px 10)}]

  [:#main-view
   {:width "100%"
    :height "100%"
    :background "rgb(10, 10, 10)"
    ;;:background-image "url('http://www.theloftberlin.com/wp-content/uploads/2013/09/2013-berlin.jpg')"
    :-webkit-background-size "cover"
    :z-index 1}]

  [:#data-view
   {:position "relative"
    :width "100%"
    :height "100%"
    :z-index 1}]

  [:#graph-view
   {:float "left"
    :width "75%"
    :height "100%"
    :-webkit-perspective (px 1000)
    :-moz-perspective (px 1000)
    :-webkit-transform-style "preserve-3d"
    :-moz-transform-style "preserve-3d"}]])
