(ns graphbrain.web.handlers.nodepage
  (:use (graphbrain.web.views page))
  (:require [graphbrain.db.gbdb :as gb]
            [graphbrain.db.vertex :as vertex]
            [graphbrain.db.user :as user]
            [graphbrain.web.common :as common]
            [graphbrain.web.contexts :as contexts]
            [graphbrain.web.entitydata :as ed]
            [graphbrain.web.cssandjs :as css+js]
            [graphbrain.web.views.nodepage :as npview]))

(defn- pagedata
  [vert ctxts]
  (let [entity-data (ed/generate common/gbdb (:id vert) ctxts)]
    entity-data))

(defn- js
  [vert user ctxts all-ctxts]
  (str "var pagedata = '" (pr-str (pagedata vert ctxts)) "';"))

(defn handle-nodepage
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
          :body-fun #(npview/view vert user)
          :js (js vert user ctxts all-ctxts))))
