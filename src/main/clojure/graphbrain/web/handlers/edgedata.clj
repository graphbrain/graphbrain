(ns graphbrain.web.handlers.edgedata
  (:require [graphbrain.web.common :as common]
            [graphbrain.web.handlers.search :as search]
            [graphbrain.db.gbdb :as gb]
            [graphbrain.db.knowledge :as k]))

(defn- author
  [edge-id ctxts]
  #_(let [author-id (k/author common/gbdb edge-id ctxts)]
    (if author-id
      (let [auth (gb/getv common/gbdb author-id)]
        {:id (:id auth)
         :username (:username auth)}))))

(defn reply
  [id edge-id user-id ctxt ctxts]
  #_(pr-str {:results (search/results
                     (entity/text id)
                     ctxts)
           :author (author edge-id ctxts)
           :can-edit (perms/can-edit? common/gbdb edge-id user-id ctxt)}))

(defn handle
  [request]
  #_(let [id ((request :form-params) "id")
        edge-id ((request :form-params) "edge")
        ctxt ((request :form-params) "ctxt")
        user (common/get-user request)
        ctxts (contexts/active-ctxts ctxt user)]
    (reply id edge-id (:id user) ctxt ctxts)))
