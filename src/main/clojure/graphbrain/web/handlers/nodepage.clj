(ns graphbrain.web.handlers.nodepage
  (:require [graphbrain.hg.ops :as hgops]
            [graphbrain.web.common :as common]
            [graphbrain.web.snodes :as snodes]
            [graphbrain.web.cssandjs :as css+js]
            [graphbrain.web.views.nodepage :as np]
            [graphbrain.web.encoder :as enc]))

(defn- data
  [id user ctxt ctxts]
  (let [snodes (snodes/generate common/hg id ctxt ctxts)]
    {:root-id id
     :snodes snodes}))

(defn- js
  [id user ctxt ctxts]
  (str "var ptype='node';"
       "var data='" (enc/encode (pr-str
                      (data id user ctxt ctxts))) "';"))

(defn handle
  [request]
  #_(let
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
    (common/log request (str "nodepage: " id))
    (np/nodepage :title title
                 :css-and-js (css+js/css+js)
                 :user user
                 :ctxt ctxt
                 :js (js id user ctxt ctxts)
                 :desc desc)))
