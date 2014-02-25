(ns com.graphbrain.web
  (:use compojure.core
        ring.adapter.jetty
        (ring.middleware resource file-info cookies)
        (com.graphbrain.web.views page landing node))
  (:require [compojure.handler :as handler]
            [compojure.route :as route])
  (:import (com.graphbrain.web NavBar CssAndJs VisualGraph)
           (com.graphbrain.db Graph)))

(def graph (new Graph))

(defn get-user
  [response]
  (let
    [username (:username (response :cookies))
     session (:session (response :cookies))]
    (if (or (not username) (not session))
      nil
      (let
        [user-node (. graph getUserNodeByUsername username)]
        (if user-node
          (if (. user-node checkSession session)
            user-node))))))

(defn- js
  [node user]
  (str "var data = " (VisualGraph/generate (. node id) user)  ";\n"
    "var errorMsg = \"\";\n"))

(defn- handle-node
  [response]
  (let
    [user (get-user response)
     vert (. graph get (:* (:route-params response)))]
    (page
      :title (. vert label)
      :css-and-js (. (new CssAndJs) cssAndJs)
      :navbar (. (new NavBar user "node") html)
      :body-fun node
      :js (js vert user))))

(defroutes app-routes
  (GET "/" []
    (page
      :title "Welcome"
      :css-and-js (. (new CssAndJs) cssAndJs)
      :navbar (. (new NavBar nil "home") html)
      :body-fun landing
      :js ""))
  (GET "/node/*" response (handle-node response))
  (route/not-found "<h1>Page not found</h1>"))

(def app
  (-> app-routes
    (wrap-resource "")
    wrap-file-info
    wrap-cookies))

(run-jetty app {:port 4567})