(ns graphbrain.web.handlers.nodeactions
  (:use (graphbrain.web common)
        (ring.util response))
  (:require [graphbrain.db.gbdb :as gb]
            [graphbrain.db.id :as id]
            [graphbrain.db.maps :as maps]))

(defn- remove-vertex!
  [request]
  (let [user (get-user request)
        edge-id ((request :form-params) "edge")]
    (gb/remove! gbdb
                (maps/id->edge edge-id)
                (:id user))))

(defn- new-meaning!
  [request]
  (let [user (get-user request)
        eid ((request :form-params) "eid")
        edge-id ((request :form-params) "edge")
        score ((request :form-params) "score")
        edge (maps/id->edge edge-id score)
        new-eid (id/new-meaning eid (:id user))]
    (gb/replace! gbdb edge eid new-eid (:id user))))

(defn handle
  [request]
  (let [vert-id (:* (:route-params request))
        op ((request :form-params) "op")]
    (case op
      "remove" (remove-vertex! request)
      "new-meaning" (new-meaning! request))
    (redirect (str "/n/" vert-id))))
