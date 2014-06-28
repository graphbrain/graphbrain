(ns graphbrain.web.handlers.node
  (:use (graphbrain.web.views page node))
  (:require [graphbrain.db.gbdb :as gb]
            [graphbrain.db.vertex :as vertex]
            [graphbrain.web.common :as common]
            [graphbrain.web.contexts :as contexts]
            [graphbrain.web.visualgraph :as vg]
            [graphbrain.web.cssandjs :as css+js]))

(defn- js
  [vert user ctxts]
  (str "var data = " (vg/generate common/gbdb (:id vert) user ctxts) ";\n"
    "var errorMsg = \"\";\n"))

(defn handle-node
  [request]
  (let
      [user (common/get-user request)
       ctxts (contexts/active-ctxts request user)
       vert (gb/getv common/gbdb
                     (:* (:route-params request))
                     ctxts)]
    (page :title (vertex/label vert)
          :css-and-js (css+js/css+js)
          :user user
          :page :node
          :body-fun #(node-view user)
          :js (js vert user ctxts))))
