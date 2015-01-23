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
      (do
        (common/log request (str "grant permissions ctxt: " ctxt
                                 "; role: " role
                                 "; email-username: " email-username
                                 "; targ-user: " (:id targ-user)))
        (case role
          "Administrator" (perms/grant-admin! common/gbdb (:id targ-user) ctxt)
          "Editor" (perms/grant-editor! common/gbdb (:id targ-user) ctxt)))
      (do
        (common/log request (str "FAILED TO GRANT PERMISSIONS ctxt: " ctxt
                                 "; role: " role
                                 "; email-username: " email-username
                                 "; targ-user: " (:id targ-user)))))
    (redirect (str "/n/" ctxt))))
