(ns graphbrain.web.views.help
  (:use hiccup.core)
  (:require [graphbrain.web.views.barpage :as bar]))

(defn view
  [title desc ctxt]
  (html
   [:br] [:br] [:br] [:br] [:br]
   
   [:div {:class "container"}
    [:div {:class "row"}
     [:div {:class "col-sm-8"}
      [:h1 "What are Knowledge Graphs?"]
      [:p "Knowledge Graphs are ..."]]]]))

(defn help
  [& {:keys [title css-and-js user ctxt js desc]}]
  (bar/barpage :title title
               :css-and-js css-and-js
               :user user
               :ctxt ctxt
               :js js
               :content-fun #(view title desc ctxt)))
