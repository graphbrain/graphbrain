(ns graphbrain.web.css.bubble
  (:require [garden.units :refer [px]]))

(def css
  [[:.bubble
    {:position "absolute"
     :max-width (px 200)
     :max-height (px 200)
     :font-size (px 12)
     :padding (px 0)
     :color "rgb(91, 214, 185)"
     :background "rgb(20, 20, 20)"
     :overflow "scroll"}]

   [:.bubble-title
    {:color "rgb(20, 20, 20)"
     :background "rgb(91, 214, 185)"
     :padding (px 10)
     :text-align "center"
     :font-size "150%"
     :font-weight "bolder"
     :text-transform "uppercase"}]

   [:.bubble-body
    {:padding (px 10)
     :font-size "80%"}]])
