(ns graphbrain.web.server
  (:use compojure.core
        ring.adapter.jetty
        (ring.middleware resource file-info cookies params)
        (graphbrain.web.handlers landing nodeactions user raw
                                         allusers eco nodepage intersect))
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [graphbrain.web.common :as common]
            [graphbrain.web.handlers.input :as input]))

(defroutes app-routes
  (GET "/" request (handle-landing request))
  (POST "/node/*" request (handle-nodeactions request))
  (GET "/v/*" request (handle-nodepage request))
  (GET "/x" request (handle-intersect request))
  (GET "/raw/*" request (handle-raw request))
  (POST "/signup" request (handle-signup request))
  (POST "/checkusername" request (handle-check-username request))
  (POST "/checkemail" request (handle-check-email request))
  (POST "/login" request (handle-login request))
  (POST "/input" request (input/handle request))
  (GET "/allusers" request (handle-allusers request))
  (GET "/eco" request (handle-eco request))
  (POST "/eco" request (handle-eco request))
  (route/not-found "<h1>Page not found</h1>"))

(def app
  (-> app-routes
    (wrap-resource "")
    wrap-file-info
    wrap-params
    wrap-cookies))

(def handler
  (do
    (common/init-graph!)
    (handler/site (-> app-routes
                      (wrap-resource "")
                      wrap-file-info
                      wrap-params
                      wrap-cookies))))

(defn run!
  []
  (common/init-graph!)
  (let [port (if common/production? 80 3000)]
    (run-jetty app {:port port})))
