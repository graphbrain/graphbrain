(ns graphbrain.web.handlers.createcontext
  (:use (ring.util response))
  (:require [graphbrain.db.gbdb :as gb]
            [graphbrain.db.maps :as maps]
            [graphbrain.db.perms :as perms]
            [graphbrain.web.common :as common]))

(defn handle
  [request]
  (let [name ((request :form-params) "name")
        desc ((request :form-params) "desc")
        user (common/get-user request)
        ctxt (maps/new-context name desc "public")]
    (common/log request (str "create context: " name
                             "; desc: " desc
                             "; ctxt" (:id ctxt)))
    (gb/putv! common/gbdb ctxt)
    (perms/grant-admin! common/gbdb (:id user) (:id ctxt))
    (perms/follow! common/gbdb (:id user) (:id ctxt))
    (redirect (str "/n/" (:id ctxt)))))
