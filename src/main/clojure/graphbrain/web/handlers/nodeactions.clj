(ns graphbrain.web.handlers.nodeactions
  (:use (ring.util response))
  (:require [graphbrain.db.gbdb :as gb]
            [graphbrain.db.id :as id]
            [graphbrain.db.maps :as maps]
            [graphbrain.db.perms :as perms]
            [graphbrain.web.common :as common]))

(defn- remove-vertex!
  [edge-id targ-ctxt]
  (gb/remove! common/gbdb
              (maps/id->edge edge-id)
              targ-ctxt))

(defn- new-meaning!
  [request edge-id targ-ctxt]
  (let [eid ((request :form-params) "eid")
        score ((request :form-params) "score")
        edge (maps/id->edge edge-id score)
        new-eid (id/new-meaning eid targ-ctxt)]
    (gb/replace! common/gbdb edge eid new-eid targ-ctxt)))

(defn handle
  [request]
  (let [user (common/get-user request)
        vert-id (:* (:route-params request))
        edge-id ((request :form-params) "edge")
        op ((request :form-params) "op")
        targ-ctxt ((request :form-params) "targ-ctxt")]
    (if (perms/can-edit? common/gbdb edge-id (:id user) targ-ctxt)
      (case op
        "remove" (remove-vertex! edge-id targ-ctxt)
        "new-meaning" (new-meaning! request edge-id targ-ctxt)))
    (redirect (str "/n/" (if (= targ-ctxt vert-id)
                           ""
                           (str targ-ctxt "/"))
                   (id/local->global vert-id)))))
