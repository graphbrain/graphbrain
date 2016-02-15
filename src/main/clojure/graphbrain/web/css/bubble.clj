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

(ns graphbrain.web.css.bubble
  (:require [garden.units :refer [px]]))

(def css
  [[:.bubble
    {:position "absolute"
     :max-width (px 200)
     :max-height (px 200)
     :font-size (px 12)
     :padding (px 0)
     :color "#000"
     :background "#FFF"
     :overflow "scroll"}]

   [:.seed-bubble
    {:background "#FFFF00"}]

   [:.bubble-title
    {:color "rgb(20, 20, 20)"
     :background "rgb(91, 214, 185)"
     :padding (px 10)
     :text-align "center"
     :font-size "150%"
     :font-weight "bolder"
     :text-transform "uppercase"}]

   [:.bubble-body
    {:padding (px 10)
     :font-size "80%"}]])
