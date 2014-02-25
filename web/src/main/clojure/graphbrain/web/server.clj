(ns graphbrain.web.server
  (:use compojure.core
        ring.adapter.jetty
        (ring.middleware resource file-info cookies params)
        (graphbrain.web.handlers landing node user))
  (:require [compojure.handler :as handler]
            [compojure.route :as route]))

(defroutes app-routes
  (GET "/" response (handle-landing response))
  (GET "/node/*" response (handle-node response))
  (POST "/signup" response (handle-signup response))
  (POST "/checkusername" response (handle-check-username response))
  (POST "/checkemail" response (handle-check-email response))
  (POST "/login" response (handle-login response))
  (route/not-found "<h1>Page not found</h1>"))

(def app
  (-> app-routes
    (wrap-resource "")
    wrap-file-info
    wrap-params
    wrap-cookies))

(run-jetty app {:port 4567})