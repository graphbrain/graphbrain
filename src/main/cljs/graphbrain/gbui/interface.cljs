(ns graphbrain.gbui.interface
  (:require [jayq.core :as jq]
            [graphbrain.gbui.globals :as g]
            [graphbrain.gbui.animation :as anim]
            [graphbrain.gbui.remove :as rem]
            [graphbrain.gbui.user :as user]
            [graphbrain.gbui.search :as search])
  (:use [jayq.core :only [$]]))

(defn init-interface
  []
  (search/init-search-dialog!)
  (user/init-signup-dialog!)
  (jq/bind ($ ".signupLink") "click" user/show-signup-dialog!)
  (jq/bind ($ "#loginLink") "click" user/show-signup-dialog!)
  (jq/bind ($ "#logoutLink") "click" user/logout!)
  (if (= js/ptype "node")
    (rem/init-remove-dialog @g/root-id)))
