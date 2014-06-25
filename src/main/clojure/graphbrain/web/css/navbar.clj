(ns graphbrain.web.css.navbar
  (:require [garden.units :refer [px]]))

(def css
  [[:.navbar-container
   {:position "absolute"
    :top (px 0)
    :left (px 0)
    :heigth (px 45)
    :background "rgba(0, 0, 0, 0.5)"
    :z-index 10
    :padding-left (px 15)
    :padding-right (px 15)}]

  [:.navbar-element
   {:display "inline"
    :line-height (px 45)}]

  [:.menu-caret
   {:display "inline-block"
    :width (px 0)
    :height (px 0)
    :vertical-align "middle"
    :border-top "5px solid #aaa"
    :border-right "5px solid transparent"
    :border-left "5px solid transparent"
    :color "#aaa"}]

  [:.nav-search
   {:display "inline"
    :margin-left (px 15)
    :margin-right (px 15)
    :background "#f00"}]

  [:.nav-search
   ["input[type=text]"
    {:padding (px 5)
     :margin (px 0)
     :font-size (px 12)
     :line-height "100%"
     :border "1px solid #000" 
     :-webkit-border-radius (px 0)
     :border-radius (px 0)}]]])