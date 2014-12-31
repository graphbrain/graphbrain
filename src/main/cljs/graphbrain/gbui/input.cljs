(ns graphbrain.gbui.input
  (:require-macros [hiccups.core :as hiccups])
  (:require [jayq.core :as jq]
            [hiccups.runtime :as hiccupsrt]
            [graphbrain.gbui.globals :as g]
            [graphbrain.gbui.search :as search])
  (:use [jayq.core :only [$]]))

(defn- url-results-received
  [msg]
  (let [goto-id (:gotoid msg)]
    (if (not (empty? goto-id))
      (set! (.-href js/window.location)
            (str "/n/" (js/encodeURIComponent goto-id))))))

(defn- fact-results-received
  [msg]
  (let [goto-id (:gotoid msg)]
    (if (not (empty? goto-id))
      (set! (.-href js/window.location)
            (str "/n/" goto-id)))))

(defn- def-results-received
  [msg]
  (.log js/console (str msg)))

(defn results-received
  [data]
  (let [msg (cljs.reader/read-string data)]
    (case (:type msg)
      :search (search/results-received msg)
      :url (url-results-received msg)
      :fact (fact-results-received msg)
      :def (def-results-received msg))))

(defn input-request
  [sentence]
  (jq/ajax {:type "POST"
            :url "/input"
            :data (str "sentence=" sentence "&root=" @g/root-id)
            :dataType "text"
            :success results-received}))

(defn query
  [e]
  (.preventDefault e)
  (input-request (jq/val ($ "#main-input-field")))
  false)
