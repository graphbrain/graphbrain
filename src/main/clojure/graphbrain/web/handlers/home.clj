(ns graphbrain.web.handlers.home
  (:use (ring.util response)
        (graphbrain.web.views page))
  (:require [graphbrain.web.cssandjs :as css+js]
            [graphbrain.web.views.home :as home]
            [graphbrain.web.common :as common]))

(defn- js
  []
  "var ptype='landing';")

(defn handle
  [request]
  (common/log request "home")
  (let
    [user (common/get-user request)]
    (if user
      (redirect (str "/n/" (:id user)))
      (page
       :title "Welcome"
       :css-and-js (css+js/css+js)
       :user nil
       :page :home
       :body-fun home/view
       :js (js)))))
