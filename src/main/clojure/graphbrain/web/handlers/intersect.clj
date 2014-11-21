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
            [graphbrain.web.views.intersect :as i]
            [graphbrain.web.encoder :as enc]
            [clojure.math.combinatorics :as combo]))

(defn- edge->links
  [edge]
  (combo/combinations
   (map id/eid->id
        (maps/participant-ids edge)) 2))

(defn- inters-data
  [ids ctxts]
  (let [edges (q/intersect common/gbdb ids ctxts)
        verts (into #{}
               (flatten
                (map maps/participant-ids edges)))
        verts (map #(vv/id->visual common/gbdb % ctxts) verts)
        links (mapcat identity
                      (map edge->links edges))]
    {:vertices verts
     :links links}))

(defn- data->str
  [data]
  (clojure.string/replace (pr-str data)
                          "'" ""))

(defn- js
  [ids ctxts]
  (str "var ptype='intersect';"
       "var data='" (enc/encode (inters-data ids ctxts)) "';"))

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
