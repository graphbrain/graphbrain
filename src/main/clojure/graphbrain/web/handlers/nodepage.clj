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
  [vert user ctxts all-ctxts]
  (let [entity-data (ed/generate common/gbdb (:id vert) user ctxts all-ctxts)]
    entity-data))

(defn- js
  [vert user ctxts all-ctxts]
  (str "var ptype='node';"
       "var data='" (enc/encode (pr-str
                      (pagedata vert user ctxts all-ctxts))) "';"))

(defn handle-nodepage
  [request]
  (let
      [user (common/get-user request)
       ctxts (contexts/active-ctxts request user)
       all-ctxts (user/user->ctxts user)
       vert (gb/getv common/gbdb
                     (:* (:route-params request))
                     ctxts)
       title (case (:type vert)
               :url (url/title common/gbdb (:id vert) ctxts)
               (vertex/label vert))
       desc (case (:type vert)
              :entity (entity/subentities vert)
              :url "web page"
              :user "GraphBrain user"
              nil)]
    (np/nodepage :title title
                 :css-and-js (css+js/css+js)
                 :user user
                 :js (js vert user ctxts all-ctxts)
                 :desc desc)))
