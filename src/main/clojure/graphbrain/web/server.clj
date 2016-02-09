(ns graphbrain.web.server
  (:use compojure.core
        ring.adapter.jetty
        (ring.middleware resource file-info cookies params))
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [graphbrain.web.common :as common]
            [graphbrain.web.handlers.landing :as landing]
            [graphbrain.web.handlers.home :as home]
            [graphbrain.web.handlers.presentation :as pres]
            [graphbrain.web.handlers.nodeactions :as nodeactions]
            [graphbrain.web.handlers.raw :as raw]
            [graphbrain.web.handlers.eco :as eco]
            [graphbrain.web.handlers.nodepage :as nodepage]
            [graphbrain.web.handlers.intersect :as intersect]
            [graphbrain.web.handlers.input :as input]
            [graphbrain.web.handlers.search :as search]
            [graphbrain.web.handlers.change :as change]
            [graphbrain.web.handlers.define :as define]
            [graphbrain.web.handlers.edgedata :as ed]
            [graphbrain.web.handlers.docs :as docs]))

(defroutes app-routes
  (GET "/" request (landing/handle request))
  (GET "/demo" request (pres/handle request))
  (POST "/n/*" request (nodeactions/handle request))
  (GET "/n/*" request (nodepage/handle request))
  (GET "/x" request (intersect/handle request))
  (GET "/raw/*" request (raw/handle request))
  (POST "/input" request (input/handle request))
  (POST "/search" request (search/handle request))
  (GET "/eco" request (eco/handle request))
  (POST "/eco" request (eco/handle request))
  (POST "/change" request (change/handle request))
  (POST "/define" request (define/handle request))
  (POST "/edge-data" request (ed/handle request))
  (GET "/presentation" request (pres/handle request))
  (GET "/docs/*" request (docs/handle request))
  (route/not-found "<h1>Page not found</h1>"))

(defn init-server!
  []
  (common/init-graph!))

(def app
  (-> app-routes
      (wrap-resource "")
      wrap-file-info
      wrap-params
      wrap-cookies))

(def handler
  (handler/site app))

(defn start!
  []
  (init-server!)
  (let [port (if common/production? 80 3000)]
    (run-jetty app {:port port})))
