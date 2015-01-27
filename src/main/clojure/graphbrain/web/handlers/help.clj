(ns graphbrain.web.handlers.help
  (:require [graphbrain.db.gbdb :as gb]
            [graphbrain.db.user :as user]
            [graphbrain.web.common :as common]
            [graphbrain.web.contexts :as contexts]
            [graphbrain.web.cssandjs :as css+js]
            [graphbrain.web.views.help :as help]
            [graphbrain.web.encoder :as enc]))

(defn- data
  [user ctxt ctxts]
  {:ctxts (contexts/contexts-map ctxts (:id user))
   :context (contexts/context-data (:id user) (:id user))})

(defn- js
  [user ctxt ctxts]
  (str "var ptype='help';"
       "var data='" (enc/encode (pr-str
                      (data user ctxt ctxts))) "';"))

(defn handle
  [request]
  (let
      [user (common/get-user request)
       ctxts (contexts/active-ctxts (:id user) user)
       ctxt (contexts/context-data (:id user) (:id user))]
    (common/log request "help")
    (help/help :title "Help"
               :css-and-js (css+js/css+js)
               :user user
               :ctxt ctxt
               :js (js user ctxt ctxts))))
