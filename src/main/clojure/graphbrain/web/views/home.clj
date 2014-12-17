(ns graphbrain.web.views.home
  (:use hiccup.core))

(def style "
<style>
  body {
    background-color: transparent;
    background-image:url('../images/bg.png')
  }
</style>
")

(def cyberspace "Cyberspace. A consensual hallucination experienced daily by
billions of legitimate operators, in every nation, by children being taught
mathematical concepts... A graphic representation of data abstracted from banks
of every computer in the human system. Unthinkable complexity. Lines of light
ranged in the nonspace of the mind, clusters and constellations of data. Like
city lights, receding...")

(defn view []
  (html
    style
    [:br]
    [:div
      [:div {:class "hero-unit landing" :style "background-color: rgba(255, 255, 255, 0.8)"}
        [:div
          [:div {:style "float:left; height:180px; padding-right:70px"}
            [:img {:src "/images/GB_logo_L.png" :width "209" :height "56" :alt "GraphBrain"}]]
            [:div {:style "color:#555; padding-top:0px; margin-top:0px;"}
              cyberspace
              [:br]
              [:i "-- William Gibson, Neuromancer"]]]
        [:div {:style "text-align:center; clear:both;"}
          [:h1 "A map of your ideas"]]
        [:div {:style "text-align:center"}
          [:h3 "Navigate it visually and store knowledge using an artificial intelligence interface."]]
        [:div {:style "text-align:center"}
          [:h4
            [:a {:href "http://graphbrain.com"} "About"]
            " | "
            [:a {:href "http://algopol.fr"} "Algopol Project"]]]
        [:br] [:br] [:br] [:br]
        [:div {:style "margin-bottom:70px;"}
          #_[:div {:style "text-align:center; float: left; padding-left:150px;"}
            [:form {:class "form-inline" :role "form" :id "search-field"}
              [:div {:class "form-group"}
                [:input {:type "text"
                         :class "form-control input-lg search-query"
                         :id "search-input-field"
                         :placeholder "Search"}]]
              [:div {:class "form-group"} [:button {:type "submit" :class "btn btn-primary btn-lg"} "Search"]]]]
          [:div {:style "text-align:center; xfloat: right; xpadding-right:150px;"}
            [:button {:type "button"
                      :class "btn btn-success btn-large"
                      :data-toggle "modal"
                      :data-target "#signup-modal"
                      :style "font-size:150%"} "Register or Login"]]]]]))
