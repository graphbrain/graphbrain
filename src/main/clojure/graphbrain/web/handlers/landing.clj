(ns graphbrain.web.handlers.landing
  (:use (ring.util response)
        (graphbrain.web common))
  (:require [graphbrain.web.views.launchpage :as lp]))

(defn- js
  []
  "var ptype='landing';")

(defn handle
  [request]
  (let
    [user (get-user request)]
    (if user
      (redirect (str "/v/" (:id user)))
      (if (= (:server request) "graphbrain.com")
        (lp/page)
        (redirect "/demo")))))
