(ns graphbrain.web.views.page
  (:use hiccup.core
        hiccup.page))

(defn page [& {:keys [title css-and-js body-fun js]}]
  (html5 {:lang "en"}
    [:head
      [:meta {:charset "utf-8"}]
      [:meta {:viewport "width=device-width, initial-scale=1.0"}]
      [:meta {:description ""}]
      [:meta {:author ""}]
      [:title (str title " - GraphBrain")]
      [:link {:href "/css/bootstrap.min.css" :type "text/css" :rel "Stylesheet"}]
      css-and-js
      [:script {:src "http://html5shim.googlecode.com/svn/trunk/html5.js"}]]
    [:body
      (body-fun)
      [:script {:language "javascript"} js]]))
