(ns graphbrain.web.handlers.landing
  (:use (ring.util response)
        (graphbrain.web common)
        (graphbrain.web.views page landing))
  (:import (com.graphbrain.web NavBar CssAndJs)))

(defn handle-landing
  [request]
  (let
    [user (get-user request)]
    (if user
      (redirect (str "/node/" (. user id)))
      (page
        :title "Welcome"
        :css-and-js (. (new CssAndJs) cssAndJs)
        :navbar (. (new NavBar nil "home") html)
        :body-fun landing-view
        :js ""))))