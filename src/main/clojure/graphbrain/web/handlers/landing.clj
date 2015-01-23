(ns graphbrain.web.handlers.landing
  (:use (ring.util response)
        (graphbrain.web common))
  (:require [graphbrain.web.views.launchpage :as lp]))

(defn- js
  []
  "var ptype='landing';")

(defn handle
  [request]
  (log request "home")
  (let [user (get-user request)]
    (if user
      (redirect (str "/n/" (:id user)))
      (if (= (:server-name request) "graphbrain.com")
        (lp/page)
        (redirect "/demo")))))
