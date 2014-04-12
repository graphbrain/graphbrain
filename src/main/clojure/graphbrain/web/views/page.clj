(ns graphbrain.web.views.page
  (:use hiccup.core
        hiccup.page)
  (:require [graphbrain.web.views.navbar :as navbar]))

(defn page [& {:keys [title user page css-and-js body-fun js]}]
  (html5 {:lang "en"}
    [:head
      [:meta {:charset "utf-8"}]
      [:meta {:viewport "width=device-width, initial-scale=1.0"}]
      [:meta {:description ""}]
      [:meta {:author ""}]
      [:title (str title " - GraphBrain")]
      css-and-js
      [:link {:href "/css/bootstrap.css" :type "text/css" :rel "Stylesheet"}]
      [:link {:href "/css/bootstrap-responsive.css" :rel "stylesheet"}]
      [:script {:src "http://html5shim.googlecode.com/svn/trunk/html5.js"}]]
    [:body
      (navbar/navbar user page)
      (body-fun)
      [:script {:language "javascript"} js]]))
