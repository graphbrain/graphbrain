(ns graphbrain.gbui.search
  (:require-macros [hiccups.core :as hiccups])
  (:require [jayq.core :as jq]
            [hiccups.runtime :as hiccupsrt])
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
  [msg]
  (.modal ($ "#searchResultsModal") "show"))

(defn results-received
  [json]
  (let [msg (js->clj json :keywordize-keys true)
        results (:results msg)
        html (if (empty? results)
               "<p>Sorry, no results found.</p>"
               (str "<p>" (count (:results msg)) " results found.</p>"
                    (clojure.string/join
                     (map #(str "<p><a href='/node/"
                                (first %) "'>" (second %) "</a></p>") results))))]
    (.html ($ "#searchResultsBody") html)
    (show-search-dialog json)))

(defn search-request
  [query]
  (jq/ajax {:type "POST"
            :url "/search"
            :data (str "q=" (.toLowerCase query))
            :dataType "json"
            :success results-received}))

(defn search-query
  [e]
  (.preventDefault e)
  (search-request (jq/val ($ "#search-input-field")))
  false)
