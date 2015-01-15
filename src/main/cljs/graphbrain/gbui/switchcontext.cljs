(ns graphbrain.gbui.switchcontext
  (:require-macros [hiccups.core :as hiccups])
  (:require [jayq.core :as jq]
            [hiccups.runtime :as hiccupsrt]
            [graphbrain.gbui.globals :as g]
            [graphbrain.gbui.id :as id])
  (:use [jayq.core :only [$]]))

(def initialised (atom false))

(hiccups/defhtml switch-context-dialog-template []
  [:div {:class "modal" :role "dialog" :aria-hidden "true" :id "switch-context-modal"}
    [:div {:class "modal-dialog"}
      [:div {:class "modal-content"}
        [:div {:class "modal-header"}
          [:a {:class "close" :data-dismiss "modal"} "×"]
          [:h3 "Switch to another GraphBrain"]]
        [:div {:class "modal-body" :id "switch-context-body"}
          [:div {:class "modal-footer"}
            [:a {:class "btn btn-primary" :data-dismiss "modal"} "Close"]]]]]])
                 
(defn init-dialog!
  []
  (let [html (switch-context-dialog-template)]
  (.appendTo ($ html) "body")
  (.modal ($ "#switch-context-modal") "hide")))

(defn show-dialog!
  []
  (if (not @initialised)
    (do
      (init-dialog!)
      (reset! initialised true)))
  (.modal ($ "#switch-context-modal") "show"))

(defn- link
  [result]
  (str "/n/" (first result)))

(defn rendered-results
  [msg]
  (let [results (:results msg)]
    (clojure.string/join
     (map #(str "<p><a href='"
                (link %)
                "'>"
                (second %)
                "</a></p>")
          results))))

(defn results-received
  [msg]
  (let [html (rendered-results
              (cljs.reader/read-string msg))]
    (show-dialog!)
    (.html ($ "#switch-context-body") html)))

(defn request!
  []
  (jq/ajax {:type "POST"
            :url "/contexts"
            :dataType "text"
            :success results-received}))
