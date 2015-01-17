(ns graphbrain.web.server
  (:use compojure.core
        ring.adapter.jetty
        (ring.middleware resource file-info cookies params))
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [graphbrain.web.common :as common]
            [graphbrain.web.handlers.landing :as landing]
            [graphbrain.web.handlers.home :as home]
            [graphbrain.web.handlers.nodeactions :as nodeactions]
            [graphbrain.web.handlers.user :as user]
            [graphbrain.web.handlers.raw :as raw]
            [graphbrain.web.handlers.allusers :as allusers]
            [graphbrain.web.handlers.eco :as eco]
            [graphbrain.web.handlers.nodepage :as nodepage]
            [graphbrain.web.handlers.intersect :as intersect]
            [graphbrain.web.handlers.input :as input]
            [graphbrain.web.handlers.search :as search]
            [graphbrain.web.handlers.change :as change]
            [graphbrain.web.handlers.define :as define]
            [graphbrain.web.handlers.createcontext :as cc]
            [graphbrain.web.handlers.contexts :as contexts]
            [graphbrain.web.handlers.grantperm :as gp]
            [graphbrain.web.handlers.followunfollow :as fu]
            [graphbrain.web.handlers.edgedata :as ed]))

(defroutes app-routes
  (GET "/" request (landing/handle request))
  (GET "/demo" request (home/handle request))
  (POST "/n/*" request (nodeactions/handle request))
  (GET "/n/*" request (nodepage/handle request))
  (GET "/x" request (intersect/handle request))
  (GET "/raw/*" request (raw/handle request))
  (POST "/signup" request (user/handle-signup request))
  (POST "/checkusername" request (user/handle-check-username request))
  (POST "/checkemail" request (user/handle-check-email request))
  (POST "/login" request (user/handle-login request))
  (POST "/input" request (input/handle request))
  (POST "/search" request (search/handle request))
  (GET "/allusers" request (allusers/handle request))
  (GET "/eco" request (eco/handle request))
  (POST "/eco" request (eco/handle request))
  (POST "/change" request (change/handle request))
  (POST "/define" request (define/handle request))
  (POST "/create-context" request (cc/handle request))
  (POST "/contexts" request (contexts/handle request))
  (POST "/grant-perm" request (gp/handle request))
  (POST "/follow-unfollow" request (fu/handle request))
  (POST "/edge-data" request (ed/handle request))
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
