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

(ns graphbrain.web.handlers.search
  (:require [graphbrain.web.common :as common]
            [graphbrain.hg.ops :as hgops]
            [graphbrain.hg.symbol :as sym]))

(defn results
  [q ctxts]
  #_(map #(list (id/eid->id %) (entity/description %))
       (si/query common/gbdb q ctxts)))

(defn reply
  [results mode]
  (pr-str {:type :search
           :mode mode
           :results results}))

(defn process
  [q ctxts mode]
  (reply
   (results q ctxts)
   mode))

(defn handle
  [request]
  #_(let [q ((request :form-params) "q")
        mode (keyword ((request :form-params) "mode"))
        ctxt ((request :form-params) "ctxt")
        user (common/get-user request)
        ctxts (contexts/active-ctxts ctxt user)]
    (process q ctxts mode)))
