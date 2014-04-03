(ns graphbrain.web.handlers.node
  (:use (graphbrain.web common)
        (graphbrain.web.views page node))
  (:require [graphbrain.web.visualgraph :as vg])
  (:import (com.graphbrain.web NavBar CssAndJs)))

(defn- js
  [node user]
  (str "var data = " (vg/generate graph (. node id) user) ";\n"
    "var errorMsg = \"\";\n"))

(defn handle-node
  [request]
  (let
    [user (get-user request)
     vert (. graph get (:* (:route-params request)))]
    (page
      :title (. vert label)
      :css-and-js (. (new CssAndJs) cssAndJs)
      :navbar (. (new NavBar user "node") html)
      :body-fun node-view
      :js (js vert user))))
