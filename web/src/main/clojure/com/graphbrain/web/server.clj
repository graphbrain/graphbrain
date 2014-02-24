(ns com.graphbrain.web
  (:use compojure.core
        ring.adapter.jetty
        (ring.middleware resource file-info)
        (com.graphbrain.web.views page landing))
  (:require [compojure.handler :as handler]
            [compojure.route :as route])
  (:import (com.graphbrain.web NavBar CssAndJs)))

(defroutes app-routes
  (GET "/" []
    (page
      "Welcome"
      (. (new CssAndJs) cssAndJs)
      (. (new NavBar nil "home") html)
      landing
      ""))
  (route/not-found "<h1>Page not found</h1>"))

(def app
  (-> app-routes
    (wrap-resource "")
    (wrap-file-info)))

(run-jetty app {:port 4567})