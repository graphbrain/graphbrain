(ns graphbrain.web.handlers.relations
  (:use (graphbrain.web common)
        (graphbrain.web.views page node))
  (:require [graphbrain.web.visualgraph :as vg]))

(defn handle-relations
  [request]
  (let
    [user (get-user request)
     pos ((request :form-params) "pos")
     root-id ((request :form-params) "rootId")]
    (vg/generate gbdb root-id user)))
