(ns graphbrain.web.handlers.bubble
  (:use (graphbrain.web.views page view))
  (:require [graphbrain.db.gbdb :as gb]
            [graphbrain.db.vertex :as vertex]
            [graphbrain.db.user :as user]
            [graphbrain.web.common :as common]
            [graphbrain.web.contexts :as contexts]
            [graphbrain.web.entitydata :as ed]
            [graphbrain.web.cssandjs :as css+js]))

(defn- entity-bubble
  [gbdb id ctxts]
  (let [entity-data (ed/generate gbdb id ctxts)]
    {:id id
     :type :entity
     :pos [0 0]
     :scale 1
     :content entity-data}))

(defn handle-bubble
  [request]
  (let
      [user (common/get-user request)
       ctxts (contexts/active-ctxts request user)
       id ((:query-params request) "id")]
    (pr-str (entity-bubble common/gbdb id ctxts))))
