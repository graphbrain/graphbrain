(ns graphbrain.web.handlers.grantperm
  (:use (ring.util response))
  (:require [graphbrain.db.gbdb :as gb]
            [graphbrain.db.maps :as maps]
            [graphbrain.db.perms :as perms]
            [graphbrain.web.common :as common]))

(defn handle
  [request]
  (let [ctxt ((request :form-params) "ctxt")
        email-username ((request :form-params) "email-username")
        role ((request :form-params) "role")
        user (gb/find-user common/gbdb email-username)]
    (if user
      (case role
        "Administrator" (perms/grant-admin! common/gbdb (:id user) ctxt)
        "Editor" (perms/grant-editor! common/gbdb (:id user) ctxt)))
    (redirect (str "/n/" ctxt))))
