;   Copyright (c) 2016 CNRS - Centre national de la recherche scientifique.
;   All rights reserved.
;
;   Written by Telmo Menezes <telmo@telmomenezes.com>
;
;   This file is part of GraphBrain.
;
;   GraphBrain is free software: you can redistribute it and/or modify
;   it under the terms of the GNU Affero General Public License as published by
;   the Free Software Foundation, either version 3 of the License, or
;   (at your option) any later version.
;
;   GraphBrain is distributed in the hope that it will be useful,
;   but WITHOUT ANY WARRANTY; without even the implied warranty of
;   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;   GNU Affero General Public License for more details.
;
;   You should have received a copy of the GNU Affero General Public License
;   along with GraphBrain.  If not, see <http://www.gnu.org/licenses/>.

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
