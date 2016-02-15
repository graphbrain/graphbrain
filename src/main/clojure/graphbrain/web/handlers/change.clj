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

(ns graphbrain.web.handlers.change
  (:require [graphbrain.web.common :as common]
            [graphbrain.hg.ops :as hgops]
            [graphbrain.hg.symbol :as sym]))

(defn reply
  []
  (pr-str {:type :change}))

(defn process
  [edge-id old-id new-id targ-ctxt]
  #_(let [edge (maps/id->edge edge-id)
        old-eid (gb/id->eid common/gbdb old-id)
        new-eid (gb/id->eid common/gbdb new-id)
        ids (id/id->ids edge-id)
        new-ids (map #(if (= % old-eid) new-eid %)
                     ids)
        new-edge (maps/ids->edge new-ids)]
    (gb/replace! common/gbdb edge old-eid new-eid targ-ctxt))
  (reply))

(defn handle
  [request]
  #_(let [user (common/get-user request)
        edge ((request :form-params) "edge")
        old-id ((request :form-params) "old-id")
        new-id ((request :form-params) "new-id")
        targ-ctxt ((request :form-params) "targ-ctxt")]
    (if (perms/can-edit? common/gbdb (:id user) targ-ctxt)
      (do
        (common/log request (str "change old-id: " old-id
                                 "; new-id: " new-id
                                 "; ctxt: " targ-ctxt))
        (process edge old-id new-id targ-ctxt))
      (do
        (common/log request (str "CHANGE FAILED (no perms) old-id: " old-id
                                 "; new-id: " new-id
                                 "; ctxt: " targ-ctxt))
        (reply-no-perms)))))
