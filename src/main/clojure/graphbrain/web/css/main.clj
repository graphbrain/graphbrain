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

(ns graphbrain.web.css.main
  (:require [garden.def :refer [defstylesheet defstyles]]
            [graphbrain.web.css.general :as general]
            [graphbrain.web.css.inters :as inters]
            [graphbrain.web.css.link :as link]
            [graphbrain.web.css.frame :as frame]
            [graphbrain.web.css.nodepage :as np]
            [graphbrain.web.css.bubble :as bubble]
            [graphbrain.web.css.item :as item]
            [graphbrain.web.css.eco :as eco]))

(defstyles main
  (concat general/css
          inters/css
          link/css
          frame/css
          np/css
          bubble/css
          item/css
          eco/css))
