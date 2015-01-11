(ns graphbrain.web.handlers.nodeactions
  (:use (graphbrain.web common)
        (ring.util response))
  (:require [graphbrain.db.gbdb :as gb]
            [graphbrain.db.id :as id]
            [graphbrain.db.maps :as maps]))

(defn- remove-vertex!
  [request targ-ctxt]
  (let [edge-id ((request :form-params) "edge")]
    (gb/remove! gbdb
                (maps/id->edge edge-id)
                targ-ctxt)))

(defn- new-meaning!
  [request targ-ctxt]
  (let [eid ((request :form-params) "eid")
        edge-id ((request :form-params) "edge")
        score ((request :form-params) "score")
        edge (maps/id->edge edge-id score)
        new-eid (id/new-meaning eid targ-ctxt)]
    (gb/replace! gbdb edge eid new-eid targ-ctxt)))

(defn handle
  [request]
  (let [vert-id (:* (:route-params request))
        op ((request :form-params) "op")
        targ-ctxt ((request :form-params) "targ-ctxt")]
    (case op
      "remove" (remove-vertex! request targ-ctxt)
      "new-meaning" (new-meaning! request targ-ctxt))
    (redirect (str "/n/" targ-ctxt "/" vert-id))))
