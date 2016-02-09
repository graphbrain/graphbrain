(ns graphbrain.web.handlers.landing
  (:use (ring.util response)
        (graphbrain.web common))
  (:require [graphbrain.web.views.launchpage :as lp]))

(defn- js
  []
  "var ptype='landing';")

(defn handle
  [request]
  (log request "landing")
  (redirect "/presentation"))
