(ns graphbrain.web.handlers.brainpage
  (:require [graphbrain.db.gbdb :as gb]
            [graphbrain.db.vertex :as vertex]
            [graphbrain.db.user :as user]
            [graphbrain.web.common :as common]
            [graphbrain.web.contexts :as contexts]
            [graphbrain.web.cssandjs :as css+js]
            [graphbrain.web.views.brainpage :as bp]
            [graphbrain.web.encoder :as enc]))

(defn- braindata
  [ctxt user]
  {:context (contexts/context-data (:id ctxt) (:id user))})

(defn- js
  [ctxt user]
  (str "var ptype='brain';"
       "var data='" (enc/encode (pr-str
                                 (braindata ctxt user))) "';"))

(defn handle
  [request]
  (let
      [user (common/get-user request)
       id (:* (:route-params request))
       ctxts (contexts/active-ctxts id user)
       ctxt (gb/getv common/gbdb
                     id
                     ctxts)
       title (vertex/label ctxt)
       desc (:name ctxt)]
    (bp/brainpage :title title
                  :css-and-js (css+js/css+js)
                  :user user
                  :ctxt (contexts/context-data id (:id user))
                  :js (js ctxt user)
                  :desc desc)))
