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

(ns graphbrain.web.handlers.intersect
  (:require [graphbrain.hg.symbol :as sym]
            [graphbrain.hg.queries :as q]
            [graphbrain.web.visualvert :as vv]
            [graphbrain.web.views.intersect :as i]
            [graphbrain.web.encoder :as enc]
            [clojure.math.combinatorics :as combo]))

(defn- edge->links
  [edge]
  (combo/combinations (rest edge) 2))

(defn- inters-data
  [hg ids]
  (let [edges (q/intersect hg ids)
        verts (into #{}
               (flatten
                (map rest edges)))
        verts (map #(vv/sym->visual hg %) verts)
        links (mapcat identity
                      (map edge->links edges))]
    {:vertices verts
     :links links
     :seeds ids}))

(defn- data->str
  [data]
  (clojure.string/replace (pr-str data)
                          "'" ""))

(defn- js
  [hg ids]
  (str "var ptype='intersect';"
       "var data='" (enc/encode
                     (inters-data hg ids))
       "';"))

(defn handle
  [request hg]
  (let [ids (vals (:query-params request))]
    (i/intersect :title "intersect"
                 :js (js hg ids))))
