(ns graphbrain.web.views.brainpage
  (:use hiccup.core)
  (:require [graphbrain.web.views.barpage :as bar]))

(defn view
  [title desc]
  (html
   [:div {:id "nodepage"}
    [:div {:id "nodepage-title"}
     [:div {:class "np-title"} title]
     [:div {:class "np-desc"} desc]]
    [:div {:id "frames"}]]))

(defn brainpage
  [& {:keys [title css-and-js user ctxt js desc]}]
  (bar/barpage :title title
               :css-and-js css-and-js
               :user user
               :ctxt ctxt
               :js js
               :content-fun #(view title desc)))
