(ns graphbrain.web.handlers.nodeactions
  (:use (graphbrain.web common)
        (ring.util response))
  (:require [graphbrain.graph :as gb]))

(defn- remove-vertex
  [request]
  (let
    [user (get-user request)
     edge-id ((request :form-params) "edge")]
    (. graph remove
      (gb/edge-from-id edge-id)
      (. user id))))

(defn handle-nodeactions
  [request]
  (let
    [vert-id (:* (:route-params request))
     op ((request :form-params) "op")]
    (if (= op "remove")
      (remove-vertex request))
    (redirect (str "/node/" vert-id))))