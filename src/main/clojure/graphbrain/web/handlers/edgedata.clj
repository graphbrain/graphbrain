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

(ns graphbrain.web.handlers.edgedata
  (:require [graphbrain.web.handlers.search :as search]
            [graphbrain.hg.ops :as hgops]))

(defn- author
  [edge-id ctxts]
  #_(let [author-id (k/author common/gbdb edge-id ctxts)]
    (if author-id
      (let [auth (gb/getv common/gbdb author-id)]
        {:id (:id auth)
         :username (:username auth)}))))

(defn reply
  [id edge-id user-id ctxt ctxts]
  #_(pr-str {:results (search/results
                     (entity/text id)
                     ctxts)
           :author (author edge-id ctxts)
           :can-edit (perms/can-edit? common/gbdb edge-id user-id ctxt)}))

(defn handle
  [request]
  #_(let [id ((request :form-params) "id")
        edge-id ((request :form-params) "edge")
        ctxt ((request :form-params) "ctxt")
        user (common/get-user request)
        ctxts (contexts/active-ctxts ctxt user)]
    (reply id edge-id (:id user) ctxt ctxts)))
