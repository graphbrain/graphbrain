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

(ns graphbrain.web.handlers.define
  (:require [graphbrain.hg.ops :as hgops]
            [graphbrain.hg.symbol :as sym]))

(defn reply
  [id]
  id)

(defn process
  [user rel root-id new-id ctxt ctxts]
  #_(let [name (id/last-part root-id)
        new-eid (id/name+ids->eid rel name [new-id])
        new (maps/eid->entity new-eid)
        old (gb/getv common/gbdb root-id)]
    (gb/replace-vertex! common/gbdb old new ctxt ctxts)
    (reply (id/global->local
            (:id new)
            ctxt))))

(defn handle
  [request]
  #_(let [rel ((request :form-params) "rel")
        root-id ((request :form-params) "root-id")
        new-id ((request :form-params) "new-id")
        ctxt ((request :form-params) "ctxt")
        user (common/get-user request)
        ctxts (contexts/active-ctxts ctxt user)]
    (common/log request (str "define new-id: " new-id
                             "; rel: " rel
                             "; root-id: " root-id
                             "; ctxt: " ctxt))
    (process user rel root-id new-id ctxt ctxts)))
