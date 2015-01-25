(ns graphbrain.web.handlers.presentation
  (:use (ring.util response)
        (graphbrain.web.views page))
  (:require [graphbrain.web.cssandjs :as css+js]
            [graphbrain.web.views.presentation :as pres]
            [graphbrain.web.common :as common]))

(defn- js
  []
  "var ptype='presentation';")

(defn handle
  [request]
  (common/log request "presentation")
  (page
   :title "Welcome"
   :css-and-js (css+js/css+js)
   :user nil
   :page :presentation
   :body-fun pres/view
   :js (js)))
