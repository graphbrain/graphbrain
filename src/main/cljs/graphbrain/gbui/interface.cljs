(ns graphbrain.gbui.interface
  (:require [jayq.core :as jq]
            [graphbrain.gbui.globals :as g]
            [graphbrain.gbui.animation :as anim]
            [graphbrain.gbui.change :as change]
            [graphbrain.gbui.user :as user]
            [graphbrain.gbui.contexts :as contexts]
            [graphbrain.gbui.input :as input]
            [graphbrain.gbui.switchcontext :as sc]
            [graphbrain.gbui.managecontext :as mc])
  (:use [jayq.core :only [$]]))

(defn init-interface
  []
  (jq/bind ($ "#top-input-field") "submit" input/query)
  (jq/bind ($ ".signupLink") "click" user/show-signup-dialog!)
  (jq/bind ($ "#loginLink") "click" user/show-signup-dialog!)
  (jq/bind ($ "#logoutLink") "click" user/logout!)
  (jq/bind ($ "#create-context-link") "click" contexts/show-create-context-dialog!)
  (jq/bind ($ "#switch-context-link") "click" sc/request!)
  (jq/bind ($ "#manage-context-link") "click" mc/request!)
  (if (= js/ptype "node")
    (do
      (change/init-dialog @g/root-id))))
