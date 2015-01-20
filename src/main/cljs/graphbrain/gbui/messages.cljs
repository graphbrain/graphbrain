(ns graphbrain.gbui.messages
  (:require [jayq.core :as jq]
            [hiccups.runtime :as hiccupsrt])
  (:use [jayq.core :only [$]]))

(defn error
  [msg]
  (let [em ($ "#msgbar")]
    (jq/html em msg)
    (jq/show em)))

(defn hide
  []
  (let [em ($ "#msgbar")]
    (jq/hide em)))
