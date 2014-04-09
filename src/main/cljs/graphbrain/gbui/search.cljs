(ns graphbrain.gbui.search
  (:require [jayq.core :as jq])
  (:use [jayq.core :only [$]]))

(defn init-search-dialog!
  []
  (let [html "<div class='modal hide' id='searchResultsModal'>
<div class='modal-header'> <a class='close' data-dismiss='modal'>×</a>
<h3>Search Results</h3>  </div>  <div class='modal-body' id='searchResultsBody' />
<div class='modal-footer'> <a class='btn btn-primary' data-dismiss='modal'>Close</a>
</div>\n</div>"]
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
