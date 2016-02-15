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

(ns graphbrain.web.css.nodepage
  (:require [garden.units :refer [px]]))

(def css
  [[:body
    {:background "#EEE"}]

   [:#nodeback
    {:width "100%"
     :height "100%"
     :background "#EEE"}]
   
   [:#nodepage
    {:max-width (px 900)
     :height "100%"
     :padding-top (px 100)
     :margin-left "auto"
     :margin-right "auto"
     :display "block"}]

   [:#nodepage-title
    {:margin-bottom (px 25)}]
   
   [:#frames
    {:column-count 3
     :-webkit-column-count 3
     :-moz-column-count 3}]

   [:.np-title
    {:font-size (px 36)}]

   [:.np-desc
    {:font-size (px 20)
     :margin-bottom (px 10)}]

   [:#topbar
    {:position "fixed"
     :top (px 0)
     :left (px 0)
     :width "100%"
     :height (px 60)
     :text-color "#000"
     :background "#FFF"
     :z-index 1000}]

   [:#msgbar
    {:position "fixed"
     :top (px 60)
     :left (px 0)
     :width "100%"
     :height (px 40)
     :z-index 1000
     :display "none"}]

   [:.topbar-element
    {:float "left"
     :width (px 100)}]

   [:.topbar-menu
    {:margin-right (px 15)
     ;;:z-index 9999
     :max-width (px 200)
     :font-size (px 14)}]

   [:.topbar-input-area
    {:margin "0px 440px 0px 100px"}]

   [:.topbar-center
    {:position "relative"
     :text-align "center"}]

   [:.topbar-vcenter
    {:position "relative"
     :top "50%"
     :transform "translateY(-50%)"}]
   
   [:.top-input
    {}]

   [:.top-input-field
    {:width "100%"
     :padding "10px 10px"
     :line-height (px 30)
     :font-size (px 24)
     :border "none"
     :outline "none"}]

   [:.after-title-frame
    {:margin-top (px 15)
     :margin-bottom (px 25)}]

   [:.after-title-form-elem
    {:margin-right (px 5)}]
   
   ])
