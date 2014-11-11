(ns graphbrain.web.handlers.intersect
  (:require [graphbrain.db.gbdb :as gb]
            [graphbrain.db.vertex :as vertex]
            [graphbrain.db.user :as user]
            [graphbrain.db.id :as id]
            [graphbrain.db.maps :as maps]
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

(defn- inters-data
  [ids ctxts]
  (let [edges (q/intersect common/gbdb ids ctxts)
        verts (into #{}
               (flatten
                (map maps/participant-ids edges)))
        verts (map #(maps/id->vertex (id/eid->id %)) verts)]
    {:vertices verts}))

(defn- js
  [ids ctxts]
  (str "var ptype='intersect';"
       "var data='" (pr-str (inters-data ids ctxts)) "';"))

(defn handle-intersect
  [request]
  (let
      [user (common/get-user request)
       ctxts (contexts/active-ctxts request user)
       ids (vals (:query-params request))]
    (i/intersect :title "intersect"
                 :css-and-js (css+js/css+js)
                 :user user
                 :js (js ids ctxts))))
