(ns graphbrain.web.handlers.intersect
  (:require [graphbrain.db.gbdb :as gb]
            [graphbrain.db.vertex :as vertex]
            [graphbrain.db.user :as user]
            [graphbrain.db.queries :as q]
            [graphbrain.web.common :as common]
            [graphbrain.web.contexts :as contexts]
            [graphbrain.web.entitydata :as ed]
            [graphbrain.web.cssandjs :as css+js]
            [graphbrain.web.views.intersect :as i]))

(defn- entity-bubble
  [gbdb id user ctxts all-ctxts]
  (let [entity-data (ed/generate gbdb id user ctxts all-ctxts)]
    {:id id
     :type :entity
     :pos [0 0]
     :scale 1
     :content entity-data}))

(defn- view-data
  [user ctxts all-ctxts]
  (let [bubble (entity-bubble common/gbdb
                              "f43806bb591e3b87/berlin"
                              user
                              ctxts
                              all-ctxts)]
    {:bubbles [bubble]}))

(defn- js
  [vert user ctxts all-ctxts]
  (str "var ptype='intersect';"
       "var data='" (pr-str (view-data user ctxts all-ctxts)) "';"))

(defn handle-intersect
  [request]
  (let
      [user (common/get-user request)
       ctxts (contexts/active-ctxts request user)
       all-ctxts (user/user->ctxts user)
       ids (vals (:query-params request))
       edges (q/intersect common/gbdb ids ctxts)]
    (i/intersect :title "intersect"
                 :css-and-js (css+js/css+js)
                 :user user
                 :js (js nil user ctxts all-ctxts)
                 :text edges)))
