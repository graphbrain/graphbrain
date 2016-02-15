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

(ns graphbrain.web.css.eco
  (:require [garden.units :refer [px]]))

(def css
  [[:#eco-results
    {:max-width (px 900)
     :height "100%"
     :padding-top (px 100)
     :margin-left "auto"
     :margin-right "auto"
     :display "block"
     :font-size "14pt"}]

   [:.eco-section
    {:margin (px 20)
     :background "#FFF"}]

   [:.eco-word
    {:margin-right (px 10)
     :display "inline"}]

   [:.eco-pos
    {:color "#00CC00"}]

   [:.eco-lemma
    {:color "#0080FF"}]

   [:.eco-trace-box
    {:margin "5px 5px 5px 30px"
     :padding (px 10)
     :border-style "solid"
     :border-width (px 1)
     :border-color "#AAA"}]

   [:.eco-weight
    {:color "#800000"}]

   [:.eco-rule
    {:color "#0000FF"}]

   [:.eco-vertex
    {:color "#808080"}]])
