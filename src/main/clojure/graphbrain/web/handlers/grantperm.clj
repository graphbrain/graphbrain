(ns graphbrain.web.handlers.grantperm
  (:use (ring.util response))
  (:require [graphbrain.db.gbdb :as gb]
            [graphbrain.db.maps :as maps]
            [graphbrain.db.perms :as perms]
            [graphbrain.web.common :as common]))

(defn handle
  [request]
  (let [user (common/get-user request)
        ctxt ((request :form-params) "ctxt")
        role ((request :form-params) "role")
        email-username ((request :form-params) "email-username")
        targ-user (gb/find-user common/gbdb email-username)]
    (if (and targ-user
             (perms/is-admin? common/gbdb (:id user) ctxt))
      (case role
        "Administrator" (perms/grant-admin! common/gbdb (:id targ-user) ctxt)
        "Editor" (perms/grant-editor! common/gbdb (:id targ-user) ctxt)))
    (redirect (str "/n/" ctxt))))
