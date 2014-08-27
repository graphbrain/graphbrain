(ns graphbrain.web.css.bubble
  (:require [garden.units :refer [px]]))

(def css
  [[:.bubble
    {:max-width (px 200)
     :max-height (px 200)
     :font-size (px 12)
     :border-style "none"
     :padding (px 10)
     :background "rgba(255, 255, 255, 0.8)"
     :overflow "scroll"}]])
