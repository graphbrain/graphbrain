(ns graphbrain.gbui.search
  (:require-macros [hiccups.core :as hiccups])
  (:require [jayq.core :as jq]
            [hiccups.runtime :as hiccupsrt]
            [graphbrain.gbui.globals :as g])
  (:use [jayq.core :only [$]]))

(hiccups/defhtml search-dialog-template []
  [:div {:class "modal" :role "dialog" :aria-hidden "true" :id "search-results-modal"}
    [:div {:class "modal-dialog"}
      [:div {:class "modal-content"}
        [:div {:class "modal-header"}
          [:a {:class "close" :data-dismiss "modal"} "×"]
          [:h3 "Search Results"]]
        [:div {:class "modal-body" :id "search-results-body"}
          [:div {:class "modal-footer"}
            [:a {:class "btn btn-primary" :data-dismiss "modal"} "Close"]]]]]])
                 
(defn init-dialog!
  []
  (let [html (search-dialog-template)]
  (.appendTo ($ html) "body")
  (.modal ($ "#search-results-modal") "hide")))

(defn show-dialog
  []
  (.modal ($ "#search-results-modal") "show"))

(defn- search-link
  [result mode]
  (case mode
    :search (str "/v/" (first result))
    :intersect (str "/x?id1=" (first result)
                    "&id2=" @g/root-id)))

(defn rendered-results
  [msg]
  (let [results (:results msg)
        mode (:mode msg)]
    (clojure.string/join
     (map #(str "<p><a href='"
                (search-link % mode) "'>" (second %) "</a></p>")
          results))))

(defn results-received
  [msg]
  (let [results (:results msg)
        html (if (empty? results)
               "<p>Sorry, no results found.</p>"
               (str "<p>" (count results) " results found.</p>"
                    (rendered-results msg)))]
    (.html ($ "#search-results-body") html)
    (show-dialog)))

(defn search-request
  [query]
  (jq/ajax {:type "POST"
            :url "/search"
            :data (str "q=" (.toLowerCase query))
            :dataType "text"
            :success results-received}))
