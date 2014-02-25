(ns graphbrain.web.handlers.landing
  (:use (graphbrain.web common)
        (graphbrain.web.views page landing))
  (:import (com.graphbrain.web NavBar CssAndJs)))

(defn handle-landing
  [response]
  (let
    [user (get-user response)]
    (page
      :title "Welcome"
      :css-and-js (. (new CssAndJs) cssAndJs)
      :navbar (. (new NavBar nil "home") html)
      :body-fun landing-view
      :js "")))