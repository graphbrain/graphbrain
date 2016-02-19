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

(ns graphbrain.web.handlers.raw
  (:use [clojure.string :only [join]]
        (graphbrain.web.views page raw))
  (:require [graphbrain.hg.ops :as ops]
            [graphbrain.hg.edgestr :as es]
            [graphbrain.web.cssandjs :as css+js]
            [graphbrain.web.views.barpage :as bar]))

(defn- js
  []
  "var ptype='raw';")

(defn- raw-html
  [request hg vertex-id]
  (str "<h2>Vertex: " vertex-id "</h2><br/><br/>"
       (let [edges (ops/star hg vertex-id)]
         (join (map #(str (es/edge->str %) "<br />") edges)))))

(defn handle
  [request hg]
  (let [vertex-id (:* (:route-params request))]
    (bar/barpage :title vertex-id
                 :css-and-js (css+js/css+js)
                 :content-fun #(raw-view (raw-html request hg vertex-id))
                 :js (js))))
