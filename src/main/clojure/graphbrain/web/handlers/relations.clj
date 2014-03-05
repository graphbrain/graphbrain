(ns graphbrain.web.handlers.relations
  (:use (graphbrain.web common)
        (graphbrain.web.views page node))
  (:import (com.graphbrain.web VisualGraph)))

(defn handle-relations
  [request]
  (let
    [user (get-user request)
     pos ((request :form-params) "pos")
     root-id ((request :form-params) "rootId")]
    (VisualGraph/generate graph root-id user)))