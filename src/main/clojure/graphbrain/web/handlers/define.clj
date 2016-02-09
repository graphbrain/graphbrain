(ns graphbrain.web.handlers.define
  (:require [graphbrain.web.common :as common]
            [graphbrain.hg.ops :as hgops]
            [graphbrain.hg.id :as id]))

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
