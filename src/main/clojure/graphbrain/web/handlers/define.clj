(ns graphbrain.web.handlers.define
  (:require [graphbrain.web.common :as common]
            [graphbrain.web.contexts :as contexts]
            [graphbrain.db.gbdb :as gb]
            [graphbrain.db.id :as id]
            [graphbrain.db.maps :as maps]
            [graphbrain.db.entity :as entity]))

(defn reply
  [id]
  id)

(defn process
  [user rel root-id new-id ctxts]
  (let [user-id (:id user)
        name (id/last-part root-id)
        new-eid (id/name+ids->eid rel name [new-id])
        new (maps/eid->entity new-eid)
        old (gb/getv common/gbdb root-id)]
    (gb/replace-vertex! common/gbdb old new user-id ctxts)
    (reply (:id new))))

(defn handle
  [request]
  (let [rel ((request :form-params) "rel")
        root-id ((request :form-params) "root-id")
        new-id ((request :form-params) "new-id")
        ctxt ((request :form-params) "ctxt")
        user (common/get-user request)
        ctxts (contexts/active-ctxts ctxt user)]
    (process user rel root-id new-id ctxts)))
