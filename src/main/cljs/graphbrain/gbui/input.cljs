(ns graphbrain.gbui.input
  (:require-macros [hiccups.core :as hiccups])
  (:require [jayq.core :as jq]
            [hiccups.runtime :as hiccupsrt]
            [graphbrain.gbui.globals :as g])
  (:use [jayq.core :only [$]]))

(hiccups/defhtml search-dialog-template []
  [:div {:class "modal" :role "dialog" :aria-hidden "true" :id "searchResultsModal"}
    [:div {:class "modal-dialog"}
      [:div {:class "modal-content"}
        [:div {:class "modal-header"}
          [:a {:class "close" :data-dismiss "modal"} "×"]
          [:h3 "Search Results"]]
        [:div {:class "modal-body" :id "searchResultsBody"}
          [:div {:class "modal-footer"}
            [:a {:class "btn btn-primary" :data-dismiss "modal"} "Close"]]]]]])
                 
(defn init-search-dialog!
  []
  (let [html (search-dialog-template)]
  (.appendTo ($ html) "body")
  (.modal ($ "#searchResultsModal") "hide")))

(defn show-search-dialog
  []
  (.modal ($ "#searchResultsModal") "show"))

(defn- search-results-received
  [msg]
  (let [results (:results msg)
        html (if (empty? results)
               "<p>Sorry, no results found.</p>"
               (str "<p>" (count (:results msg)) " results found.</p>"
                    (clojure.string/join
                     (map #(str "<p><a href='/x/"
                                (first %) "'>" (second %) "</a></p>") results))))]
    (.html ($ "#searchResultsBody") html)
    (show-search-dialog)))

(defn- url-results-received
  [msg]
  (let [goto-id (:gotoid msg)]
    (if (not (empty? goto-id))
      (set! (.-href js/window.location)
            (str "/x/" (js/encodeURIComponent goto-id))))))

(defn- fact-results-received
  [msg]
  (let [goto-id (:gotoid msg)]
    (if (not (empty? goto-id))
      (set! (.-href js/window.location)
            (str "/x/" (js/encodeURIComponent goto-id))))))

(defn results-received
  [data]
  (let [msg (cljs.reader/read-string data)]
    (case (:type msg)
      :search (search-results-received msg)
      :url (url-results-received msg)
      :fact (fact-results-received msg))))

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
