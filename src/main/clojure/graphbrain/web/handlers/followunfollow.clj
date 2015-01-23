(ns graphbrain.web.handlers.followunfollow
  (:use (ring.util response))
  (:require [graphbrain.db.gbdb :as gb]
            [graphbrain.db.maps :as maps]
            [graphbrain.db.perms :as perms]
            [graphbrain.web.common :as common]))

(defn handle
  [request]
  (let [ctxt ((request :form-params) "ctxt")
        user (common/get-user request)]
    (common/log request (str "followunfollow: " ctxt))
    (perms/toggle-follow! common/gbdb (:id user) ctxt)
    (redirect (str "/n/" ctxt))))
