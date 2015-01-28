(ns graphbrain.web.views.docs
  (:use hiccup.core)
  (:require [graphbrain.web.views.barpage :as bar]))

(def style "
<style>
  body, html {
    background-image:url('../images/bg.png')
    #background-color: rgba(200, 200, 255, 0.85);
    #background-attachment: fixed;
  }
  #nodeback {
    background-color: transparent;    
  }
</style>
")

(defn view
  [title html-str ctxt]
  (html
   style
   [:br] [:br] [:br] [:br] [:br]
   
   [:div {:class "container"}
    [:div {:class "row"}
     [:div {:class "col-sm-8"}
      html-str]]]))

(defn docs
  [& {:keys [title css-and-js user ctxt js html]}]
  (bar/barpage :title title
               :css-and-js css-and-js
               :user user
               :ctxt ctxt
               :js js
               :content-fun #(view title html ctxt)))
