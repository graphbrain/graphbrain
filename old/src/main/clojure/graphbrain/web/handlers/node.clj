(ns graphbrain.web.handlers.node
  (:use (graphbrain.web.views page node))
  (:require [graphbrain.db.gbdb :as gb]
            [graphbrain.db.vertex :as vertex]
            [graphbrain.db.user :as user]
            [graphbrain.web.common :as common]
            [graphbrain.web.contexts :as contexts]
            [graphbrain.web.visualgraph :as vg]
            [graphbrain.web.cssandjs :as css+js]))

(defn- js
  [vert user ctxts all-ctxts]
  (str "var ptype='node3d';"
       "var data=" (vg/generate common/gbdb (:id vert) user ctxts all-ctxts) ";"))

(defn handle-node
  [request]
  (let
      [user (common/get-user request)
       ctxts (contexts/active-ctxts request user)
       all-ctxts (user/user->ctxts user)
       vert (gb/getv common/gbdb
                     (:* (:route-params request))
                     ctxts)]
    (page :title (vertex/label vert)
          :css-and-js (css+js/css+js)
          :user user
          :page :node
          :body-fun #(node-view user)
          :js (js vert user ctxts all-ctxts))))
