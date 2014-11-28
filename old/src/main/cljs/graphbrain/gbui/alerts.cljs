(ns graphbrain.gbui.alerts
  (:require [jayq.core :as jq])
  (:use [jayq.core :only [$]]))

(defn init-alert!
  []
  (.css ($ "#alert") "display" "none")
  (.css ($ "#alert") "visibility" "visible"))

(defn set-info-alert!
  [msg]
  (.css ($ "#alert") "display" "block")
  (.removeClass ($ "#alert") "alert-error")
  (.addClass ($ "#alert") "alert-info")
  (.html ($ "#alertMsg") msg))

(defn set-error-alert!
  [msg]
  (.css ($ "#alert") "display" "block")
  (.removeClass ($ "#alert") "alert-info")
  (.addClass ($ "#alert") "alert-error")
  (.html ($ "#alertMsg") msg))

(defn hide-alert!
  []
  (.css ($ "#alert") "display" "none"))
