(ns graphbrain.web.handlers.node
  (:use (graphbrain.web common)
        (graphbrain.web.views page node))
  (:require [graphbrain.db.graph :as gb]
            [graphbrain.db.vertex :as vertex]
            [graphbrain.web.visualgraph :as vg]
            [graphbrain.web.cssandjs :as css+js]))

(defn- js
  [node user]
  (str "var data = " (vg/generate graph (:id node) user) ";\n"
    "var errorMsg = \"\";\n"))

(defn handle-node
  [request]
  (let
      [user (get-user request)
       vert (gb/getv graph (:* (:route-params request)))]
    (prn (:* (:route-params request)))
    (page :title (vertex/label vert)
          :css-and-js (css+js/css+js)
          :user user
          :page :node
          :body-fun node-view
          :js (js vert user))))
