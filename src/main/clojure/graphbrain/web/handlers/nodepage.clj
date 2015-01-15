(ns graphbrain.web.handlers.nodepage
  (:require [graphbrain.db.gbdb :as gb]
            [graphbrain.db.vertex :as vertex]
            [graphbrain.db.entity :as entity]
            [graphbrain.db.urlnode :as url]
            [graphbrain.db.user :as user]
            [graphbrain.web.common :as common]
            [graphbrain.web.contexts :as contexts]
            [graphbrain.web.entitydata :as ed]
            [graphbrain.web.cssandjs :as css+js]
            [graphbrain.web.views.nodepage :as np]
            [graphbrain.web.encoder :as enc]))

(defn- pagedata
  [id user ctxts]
  (let [entity-data (ed/generate
                     common/gbdb id user ctxts)]
    entity-data))

(defn- js
  [id user ctxts]
  (str "var ptype='node';"
       "var data='" (enc/encode (pr-str
                      (pagedata id user ctxts))) "';"))

(defn handle
  [request]
  (let
      [user (common/get-user request)
       id (:* (:route-params request))
       ctxts (contexts/active-ctxts id user)
       vert (gb/getv common/gbdb
                     id
                     ctxts)
       title (case (:type vert)
               :url (url/title common/gbdb (:id vert) ctxts)
               (vertex/label vert))
       desc (case (:type vert)
              :entity (entity/subentities vert)
              :url "web page"
              :user "GraphBrain user"
              nil)
       ctxt (contexts/context-data id (:id user))]
    (np/nodepage :title title
                 :css-and-js (css+js/css+js)
                 :user user
                 :ctxt ctxt
                 :js (js id user ctxts)
                 :desc desc)))
