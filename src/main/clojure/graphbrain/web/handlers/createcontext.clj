(ns graphbrain.web.handlers.createcontext
  (:use (ring.util response))
  (:require [graphbrain.db.gbdb :as gb]
            [graphbrain.db.maps :as maps]
            [graphbrain.web.common :as common]))

(defn handle
  [request]
  (let [name ((request :form-params) "name")
        desc ((request :form-params) "desc")
        user (common/get-user request)
        ctxt (maps/new-context name desc "public")]
    (gb/putv! common/gbdb ctxt)
    (gb/putrel! common/gbdb ["r/*admin" (:id user) (:id ctxt)] (:id ctxt))
    (gb/putrel! common/gbdb ["r/*follower" (:id user) (:id ctxt)] (:id user))
    (redirect (str "/b/" (:id ctxt)))))
