(ns graphbrain.gbui.interface
  (:require [jayq.core :as jq]
            [graphbrain.gbui.globals :as g]
            [graphbrain.gbui.animation :as anim]
            [graphbrain.gbui.change :as change]
            [graphbrain.gbui.remove :as rem]
            [graphbrain.gbui.user :as user]
            [graphbrain.gbui.search :as search])
  (:use [jayq.core :only [$]]))

(defn init-interface
  []
  (search/init-dialog!)
  (user/init-signup-dialog!)
  (jq/bind ($ ".signupLink") "click" user/show-signup-dialog!)
  (jq/bind ($ "#loginLink") "click" user/show-signup-dialog!)
  (jq/bind ($ "#logoutLink") "click" user/logout!)
  (if (= js/ptype "node")
    (do
      (change/init-dialog @g/root-id)
      (rem/init-remove-dialog @g/root-id))))
