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

(ns graphbrain.web.css.frame
  (:require [garden.units :refer [px]]))

(def css
  [[:.frame
   {:font-size (px 16)
    :border-style "none"
    :width "100%"
    :margin-bottom (px 15)
    :display "inline-block"
    :background "#FFF"
    :box-shadow "1px 1px 1px #CCC"}]

  [:.frame-label
   {:padding-left (px 5)
    :padding-right (px 5)
    :margin-bottom (px 10)
    :font-weight "bold"}]

  [:.frame-inner
   {}]])
