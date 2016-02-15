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

(ns graphbrain.web.handlers.nodeactions
  (:use (ring.util response))
  (:require [graphbrain.hg.ops :as hgops]
            [graphbrain.hg.symbol :as sym]
            [graphbrain.web.common :as common]))

(defn- remove-vertex!
  [request edge-str]
  (common/log request (str "remove vertex: " edge-str))
  #_(gb/remove! common/gbdb
              (maps/id->edge edge-id)
              targ-ctxt))

(defn- new-meaning!
  [request edge-str]
  (common/log request (str "new meaning: " edge-str))
  #_(let [eid ((request :form-params) "eid")
        score ((request :form-params) "score")
        edge (maps/id->edge edge-id score)
        new-eid (id/new-meaning eid targ-ctxt)]
    (gb/replace! common/gbdb edge eid new-eid targ-ctxt)))

(defn handle
  [request]
  (let [vert-id (:* (:route-params request))
        edge-str ((request :form-params) "edge")
        op ((request :form-params) "op")]
    (case op
      "remove" (remove-vertex! request edge-str)
      "new-meaning" (new-meaning! request edge-str))
    (redirect (str "/n/" vert-id))))
