(ns graphbrain.gbui.interface
  (:require [jayq.core :as jq]
            [graphbrain.gbui.globals :as g]
            [graphbrain.gbui.animation :as anim]
            [graphbrain.gbui.user :as user]
            [graphbrain.gbui.contexts :as contexts]
            [graphbrain.gbui.input :as input]
            [graphbrain.gbui.switchcontext :as sc])
  (:use [jayq.core :only [$]]))

(defn init-interface
  []
  (jq/bind ($ "#top-input-field") "submit" input/query)
  (jq/bind ($ ".signupLink") "click" user/show-signup-dialog!)
  (jq/bind ($ "#loginLink") "click" user/show-signup-dialog!)
  (jq/bind ($ "#logoutLink") "click" user/logout!)
  (jq/bind ($ "#create-context-link") "click" contexts/show-create-context-dialog!)
  (jq/bind ($ "#switch-context-link") "click" sc/request!))
