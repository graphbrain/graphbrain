(ns graphbrain.gbui.input
  (:require-macros [hiccups.core :as hiccups])
  (:require [jayq.core :as jq]
            [hiccups.runtime :as hiccupsrt]
            [graphbrain.gbui.globals :as g]
            [graphbrain.gbui.search :as search]
            [graphbrain.gbui.define :as define]
            [graphbrain.gbui.contexts :as contexts]
            [graphbrain.gbui.messages :as msg])
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
            (str "/n/" @g/context "/" goto-id)))))

(defn- def-results-received
  [msg]
  (define/show! msg))

(defn- error-results-received
  [msg]
  (msg/error (:msg msg)))

(defn results-received
  [data]
  (let [msg (cljs.reader/read-string data)]
    (msg/hide)
    (case (:type msg)
      :search (search/results-received msg)
      :url (url-results-received msg)
      :fact (fact-results-received msg)
      :def (def-results-received msg)
      :error (error-results-received msg))))

(defn input-request
  [sentence]
  (jq/ajax {:type "POST"
            :url "/input"
            :data (str "sentence=" sentence
                       "&root=" @g/root-id
                       "&targ-ctxt=" (contexts/targ-ctxt))
            :dataType "text"
            :success results-received}))

(defn query
  [e]
  (if (= js/ptype "eco")
    (.submit ($ "#top-input-field"))
    (do
      (.preventDefault e)
      (input-request (jq/val ($ "#main-input-field")))
      false)))
