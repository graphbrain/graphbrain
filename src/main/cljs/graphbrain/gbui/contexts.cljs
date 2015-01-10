(ns graphbrain.gbui.contexts
  (:require-macros [hiccups.core :as hiccups])
  (:require [jayq.core :as jq]
            [hiccups.runtime :as hiccupsrt])
  (:use [jayq.core :only [$]]))

(hiccups/defhtml create-context-dialog-template
  []
  [:div {:class "modal" :role "dialog" :aria-hidden "true" :id "create-context-modal"}
   [:div {:class "modal-dialog"}
    [:div {:class "modal-content"}
     [:div {:class "modal-header"}
      [:a {:class "close" :data-dismiss "modal"} "×"]
      [:h3 "Create Context"]]
     [:form {:id "create-context-form"
             :action "/create-context"
             :method "post"}
      [:div {:class "modal-body" :id "create-context-body"}
       [:span {:id "create-context-error-message" :class "text-danger"}]
       [:div {:id "name-formgroup" :class "form-group"}
        [:label {:class "control-label"} "Name"]
        [:input {:id "su-name"
                 :name "name"
                 :type "text"
                 :class "form-control input-sm"
                 :placeholder "Short name"}]]
       [:div {:id "desc-formgroup" :class "form-group"}
        [:label {:class "control-label"} "Description"]
        [:input {:id "su-desc"
                 :name "desc"
                 :type "text"
                 :class "form-control input-sm"
                 :placeholder "What is is about?"}]]]
      [:div {:class "modal-footer"}
       [:button
        {:type "submit" :class "btn btn-primary"} "Create"]]]]]])

(defn show-create-context-dialog!
  []
  (.modal ($ "#create-context-modal") "show"))

(defn clear-create-context-errors!
  []
  (.removeClass ($ "#name-formgroup") "has-error")
  (.removeClass ($ "#desc-formgroup") "has-error")
  (.html ($ "#create-content-error-message") ""))

(defn init-dialogs!
  []
  (.appendTo
   ($ (create-context-dialog-template)) "body"))
