(ns graphbrain.web.handlers.landing
  (:use (ring.util response)
        (graphbrain.web common)
        (graphbrain.web.views page landing))
  (:require [graphbrain.web.cssandjs :as css+js]))

(defn handle-landing
  [request]
  (let
    [user (get-user request)]
    (if user
      (redirect (str "/v/" (:id user)))
      (page
        :title "Welcome"
        :css-and-js (css+js/css+js)
        :user nil
        :page :home
        :body-fun landing-view
        :js ""))))
