(ns graphbrain.web.views.presentation
  (:use hiccup.core))

(def style "
<style>
  html,body {
    background-image: url('../images/bg.png');
    //background-repeat: repeat;
    background-attachment: fixed;
  }
</style>
")

(defn view []
  (html
   style
   [:br]
   [:div {:class "jumbotron landing"
          :style "background-color: rgba(255, 255, 255, 0.8)"}
    [:div
      [:img {:src "/images/GB_logo_L.png" :width "209" :height "56" :alt "GraphBrain"}]
      [:div {:style "text-align:center; clear:both;"}
       [:h1 "A Map of Ideas"]]
      [:div {:style "text-align:center"}
       [:h3 "Knowledge Graphs for Online Teams, Communities and Organizations."]]
      [:div {:style "text-align:center"}]
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
                  :id "loginLink"
                  :data-toggle "modal"
                  :data-target "#signup-modal"
                  :style "font-size:150%"} "Register or Login"]]]]

     
     [:div {:class "container"}
      [:div {:class "row"}
       [:div {:class "col-md-12"}
        [:div {:style "text-align:center"}
         [:iframe {:width "420" :height "315"
                   :src "//www.youtube.com/embed/NCzFcuWHeI0"
                   :frameborder "0"
                   :allowfullscreen true}]]
        [:br] [:br] [:br] [:br]
        [:p {:style "text-align:center"}
         "GraphBrain is a new type of tool to organize and explore knowledge."]
        [:br] [:br]
        ]]]

     
     [:div {:class "container"}
      [:div {:class "row"}
       [:div {:class "col-md-4"}
        [:h2 "Network of Knowledge"]
        [:p "GraphBrain lets you build a networks of knowledge. It associates concepts by relationships, much like we store information in our own brains."]]
       [:div {:class "col-md-4"}
        [:h2 "It Understands You"]
        [:p "Add little chunks of information in a straightforward way, simply by typing sentences. The AI understands you and adds the correct connections."]]
       [:div {:class "col-md-4"}
        [:h2 "Explore and Visualize"]
        [:p "Find connections that were not apparent before, drill down into details, find relevant facts quickly across different domains."]]]

      [:div {:class "row"}
       [:div {:class "col-md-4"}
        [:h2 "Collaborate"]
        [:p "Invite people from your team, community or organization to help you enrich your knowledge graph."]]
       [:div {:class "col-md-4"}
        [:h2 "Web Resources"]
        [:p "Simply give GraphBrain a URL and it will reade the page, extract the topics and connect everything to your graph."]]
       [:div {:class "col-md-4"}
        [:h2 "Free"]
        [:p "Public graphs are free. GraphBrain is ideal for online communities."]]]]

    [:div {:class "container"}
     [:div {:class "row"}
      [:div {:class "col-md-12"}
       [:br] [:br] [:br] [:br]
       [:p {:style "text-align:center"}
        "GraphBrain was created under the "
        [:a {:href "http://algopol.fr"} "Algopol"]
        " research project."]
       [:br] [:br]
       ]]]
     ]))
