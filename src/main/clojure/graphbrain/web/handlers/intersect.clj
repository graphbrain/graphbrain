(ns graphbrain.web.handlers.intersect
  (:require [graphbrain.db.gbdb :as gb]
            [graphbrain.db.vertex :as vertex]
            [graphbrain.db.user :as user]
            [graphbrain.db.id :as id]
            [graphbrain.db.maps :as maps]
            [graphbrain.db.queries :as q]
            [graphbrain.web.common :as common]
            [graphbrain.web.contexts :as contexts]
            [graphbrain.web.visualvert :as vv]
            [graphbrain.web.cssandjs :as css+js]
            [graphbrain.web.views.intersect :as i]))

(defn- inters-data
  [ids ctxts]
  (let [edges (q/intersect common/gbdb ids ctxts)
        xxx (println (map :id edges))
        verts (into #{}
               (flatten
                (map maps/participant-ids edges)))
        verts (map #(vv/id->visual common/gbdb % ctxts) verts)]
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
