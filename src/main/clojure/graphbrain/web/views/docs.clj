(ns graphbrain.web.views.docs
  (:use hiccup.core)
  (:require [graphbrain.web.views.barpage :as bar]))

(def style "
<style>
  body, html {
    background-image:url('../images/bg.png');
    background-attachment: fixed;
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
     [:div {:class "col-sm-8"
            :style "background-color: rgba(255, 255, 255, 0.90)"}
      html-str]]]

   [:br] [:br]))

(defn docs
  [& {:keys [title css-and-js user ctxt js html]}]
  (bar/barpage :title title
               :css-and-js css-and-js
               :user user
               :ctxt ctxt
               :js js
               :content-fun #(view title html ctxt)))
