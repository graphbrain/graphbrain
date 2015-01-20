(ns graphbrain.web.handlers.nodeactions
  (:use (ring.util response))
  (:require [graphbrain.db.gbdb :as gb]
            [graphbrain.db.id :as id]
            [graphbrain.db.maps :as maps]
            [graphbrain.db.perms :as perms]
            [graphbrain.web.common :as common]))

(defn- remove-vertex!
  [request targ-ctxt]
  (let [edge-id ((request :form-params) "edge")]
    (gb/remove! common/gbdb
                (maps/id->edge edge-id)
                targ-ctxt)))

(defn- new-meaning!
  [request targ-ctxt]
  (let [eid ((request :form-params) "eid")
        edge-id ((request :form-params) "edge")
        score ((request :form-params) "score")
        edge (maps/id->edge edge-id score)
        new-eid (id/new-meaning eid targ-ctxt)]
    (gb/replace! common/gbdb edge eid new-eid targ-ctxt)))

(defn handle
  [request]
  (let [user (common/get-user request)
        vert-id (:* (:route-params request))
        op ((request :form-params) "op")
        targ-ctxt ((request :form-params) "targ-ctxt")]
    (if (perms/can-edit? common/gbdb (:id user) targ-ctxt)
      (case op
        "remove" (remove-vertex! request targ-ctxt)
        "new-meaning" (new-meaning! request targ-ctxt)))
    (redirect (str "/n/" targ-ctxt
                   "/" (id/local->global vert-id)))))
