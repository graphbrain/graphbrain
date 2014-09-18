(ns graphbrain.web.css.nodepage
  (:require [garden.units :refer [px]]))

(def css
  [[:body
    {:background "#EEE"}]

   [:#nodeback
    {:width "100%"
     :height "100%"
     :background "#EEE"}]
   
   [:#nodepage
    {:max-width (px 900)
     :height "100%"
     :padding-top (px 100)
     :margin-left "auto"
     :margin-right "auto"
     :display "block"}]

   [:#nodepage-title
    {:margin-bottom (px 25)}]
   
   [:#frames
    {:column-count 3
     :-webkit-column-count 3
     :-mozcolumn-count 3}]

   [:.np-title
    {:font-size (px 36)}]

   [:.np-desc
    {:font-size (px 20)
     :margin-bottom (px 10)}]

   [:#topbar
    {:position "fixed"
     :top (px 0)
     :left (px 0)
     :width "100%"
     :height (px 60)
     :background "#FFF"}]

   [:.topbar-element
    {:float "left"
     :width (px 100)}]

   [:.topbar-menu
    {:float "right"
     :width (px 100)}]

   [:.topbar-input-area
    {:margin "0 100px"}]

   [:.topbar-center
    {:position "relative"
     :top "50%"
     :transform "translateY(-50%)"
     :text-align "center"}]
   
   [:.top-input
    {}]

   [:.top-input-field
    {:width "100%"
     :padding "10px 10px"
     :line-height (px 30)
     :font-size (px 24)
     :border "none"
     ;;:-webkit-appearance "none"
     :outline "none"}]])
