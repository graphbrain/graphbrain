(ns graphbrain.web.css.link
  (:require [garden.units :refer [px]]))

(def css
  [[:.link
    {:position "absolute"
     :background "#FF0000"
     :width (px 5)
     :height (px 5)
     :z-index -1}]])
