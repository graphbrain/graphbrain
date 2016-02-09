(ns graphbrain.web.handlers.nodeactions
  (:use (ring.util response))
  (:require [graphbrain.hg.ops :as hgops]
            [graphbrain.hg.id :as id]
            [graphbrain.web.common :as common]))

(defn- remove-vertex!
  [request edge-str]
  (common/log request (str "remove vertex: " edge-str))
  #_(gb/remove! common/gbdb
              (maps/id->edge edge-id)
              targ-ctxt))

(defn- new-meaning!
  [request edge-str]
  (common/log request (str "new meaning: " edge-str))
  #_(let [eid ((request :form-params) "eid")
        score ((request :form-params) "score")
        edge (maps/id->edge edge-id score)
        new-eid (id/new-meaning eid targ-ctxt)]
    (gb/replace! common/gbdb edge eid new-eid targ-ctxt)))

(defn handle
  [request]
  (let [vert-id (:* (:route-params request))
        edge-str ((request :form-params) "edge")
        op ((request :form-params) "op")]
    (case op
      "remove" (remove-vertex! request edge-str)
      "new-meaning" (new-meaning! request edge-str))
    (redirect (str "/n/" vert-id))))
