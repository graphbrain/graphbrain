(ns graphbrain.web.css.contexts
  (:require [garden.units :refer [px]]))

(def css
  [[:#ctxt-area
    {:position "absolute"
     :bottom (px 10)
     :padding (px 5)
     :left (px 0)
     :z-index 2}]
   [:.ctxt
    {:display "inline"
     :font-size (px 16)
     :padding (px 10)
     :margin-right (px 5)
     :color "rgba(255, 255, 255, 1)"
     :background "rgba(0, 0, 0, 0.95)"}]])
