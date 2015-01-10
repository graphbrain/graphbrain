(ns graphbrain.gbui.interface
  (:require [jayq.core :as jq]
            [graphbrain.gbui.globals :as g]
            [graphbrain.gbui.animation :as anim]
            [graphbrain.gbui.change :as change]
            [graphbrain.gbui.user :as user]
            [graphbrain.gbui.search :as search]
            [graphbrain.gbui.define :as define]
            [graphbrain.gbui.contexts :as contexts])
  (:use [jayq.core :only [$]]))

(defn init-interface
  []
  (search/init-dialog!)
  (user/init-signup-dialog!)
  (define/init-dialog!)
  (contexts/init-dialogs!)
  (jq/bind ($ ".signupLink") "click" user/show-signup-dialog!)
  (jq/bind ($ "#loginLink") "click" user/show-signup-dialog!)
  (jq/bind ($ "#logoutLink") "click" user/logout!)
  (jq/bind ($ "#create-context-link") "click" contexts/show-create-context-dialog!)
  (if (= js/ptype "node")
    (do
      (change/init-dialog @g/root-id))))
