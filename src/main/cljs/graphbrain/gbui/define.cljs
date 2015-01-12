(ns graphbrain.gbui.define
  (:require-macros [hiccups.core :as hiccups])
  (:require [jayq.core :as jq]
            [hiccups.runtime :as hiccupsrt]
            [graphbrain.gbui.search :as search])
  (:use [jayq.core :only [$]]))

(def initialised (atom false))

(hiccups/defhtml dialog-template
  []
  [:div {:class "modal" :role "dialog" :aria-hidden "true" :id "define-modal"}
   [:div {:class "modal-dialog"}
    [:div {:class "modal-content"}
     [:div {:class "modal-header"}
      [:a {:class "close" :data-dismiss "modal"} "×"]
      [:h3 "Define"]
      [:div {:class "modal-body"}
       [:div {:id "alt-entities-define"}]]
      [:div {:class "modal-footer"}
       [:a {:class "btn" :data-dismiss "modal"} "Close"]]]]]])

(defn init-dialog!
  []
  (jq/append ($ "body") (dialog-template)))

(defn- on-defined
  [goto-id]
  (set! (.-href js/window.location)
            (str "/n/" goto-id)))

(defn- define-request!
  [msg new-id]
  (jq/ajax {:type "POST"
            :url "/define"
            :data (str "rel=" (js/encodeURIComponent (:rel msg))
                       "&root-id=" (js/encodeURIComponent (:root-id msg))
                       "&new-id=" (js/encodeURIComponent new-id))
            :dataType "text"
            :success on-defined}))

(defn show-dialog!
  [results msg]
  (if (not @initialised)
    (do
      (init-dialog!)
      (reset! initialised true)))
  (jq/html ($ "#alt-entities-define")
           (search/rendered-results results))
  (let [results (:results results)]
    (doseq [result results]
      (jq/bind
       ($ (str "#" (search/link-id (first result))))
       "click"
       #(define-request! msg (first result)))))
  (.modal ($ "#define-modal") "show"))

(defn- results-received
  [results msg]
  (show-dialog! (cljs.reader/read-string results) msg))

(defn show!
  [msg]
  (search/request! (:param msg)
                   :define
                   #(results-received % msg)))
