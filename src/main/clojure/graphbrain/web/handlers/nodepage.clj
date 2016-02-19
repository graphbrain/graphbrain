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

(ns graphbrain.web.handlers.nodepage
  (:require [graphbrain.hg.ops :as ops]
            [graphbrain.web.snodes :as snodes]
            [graphbrain.web.cssandjs :as css+js]
            [graphbrain.web.views.nodepage :as np]
            [graphbrain.web.encoder :as enc]))

(defn- data
  [hg id]
  (let [snodes (snodes/generate hg id)]
    {:root-id id
     :snodes snodes}))

(defn- js
  [hg id]
  (str "var ptype='node';"
       "var data='" (enc/encode (pr-str
                      (data hg id))) "';"))

(defn handle
  [request hg]
  (let [id (:* (:route-params request))
        title id
        desc "desc"]
    (np/nodepage :title title
                 :css-and-js (css+js/css+js)
                 :js (js hg id)
                 :desc desc)))
