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
         "Graphbrain is a new type of tool to organize and explore knowledge. It aims at providing a novel way to analyze and explore large text and hyper-text corpora, by relying extensively on the notion of viewpoints -- making it possible, for instance, to assess the consensual nature of claims in a given community by keeping track of the social support of the extracted propositions."]
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
       [:h1 "Team"]]]
     [:div {:class "row"}
      [:div {:class "col-md-6"}
       [:h2 "Telmo Menezes"]
       [:a {:href "http://telmomenezes.com"} "http://telmomenezes.com"]
       [:p "Telmo has a PhD in Computer Science with a specialisation in Artificial Intelligence. He has diverse industry and research experience, having worked in a system to test NASA space probes, in computer game AI, and in a search engine startup. Currently he works as a researcher for the French CNRS, the largest fundamental research agency in Europe, and as an invited researcher in Berlin, having recently published a work about artificial network scientists on Nature’s Journal Scientific Reports."]]
      [:div {:class "col-md-6"}
   [:h2 "Camille Roth"]
       [:a {:href "http://camille.roth.free.fr"} "http://camille.roth.free.fr"]
       [:p "Camille has a strong interdisciplinary profile as he holds a PhD in Social Sciences from the École Polytechnique and is a graduate in general engineering (maths/physics) of the École des Ponts. He is currently Tenured Full Researcher in Computer Science at the French CNRS, after having been Associate Professor of Sociology at the University of Toulouse. Author of about 50 peer-reviewed publications, he coordinated several national and international research projects focusing on socio-semantic systems, especially scientific and online communities."]]]

     [:div {:class "row"}
      [:div {:class "col-md-6"}
       [:h2 "Chih-Chun Chen"]
       [:a {:href "http://abmcet.net"} "http://abmcet.net"]
       [:p "Chih-Chun has a PhD in Computer Science from University College London with a specialisation in Complex Systems modelling, and a BA in Psychology with Philosophy from the University of Oxford. She has worked at Microsoft Research, Yahoo! and Deutsche Bank and is currently a research associate at the University of Cambridge."]]
      [:div {:class "col-md-6"}
       [:h2 "Miguel Miraldo"]
       [:a {:href "http://blackbox.pt"} "http://blackbox.pt"]
       [:p "Miguel Miraldo is an Architect and Digital Media expert. He founded a successful visual communication company that has produced work for high-profile clients, namely Philippe Starck."]]]]

    
    [:div {:class "container"}
     [:div {:class "row"}
      [:div {:class "col-md-12"}
       [:br] [:br] [:br] [:br]
       [:p {:style "text-align:center"}
        "GraphBrain was partially created under the  "
        [:a {:href "http://algopol.fr"} "Algopol"]
        " research project at the "
        [:a {:href "http://www.cmb.hu-berlin.de/en/"} "Centre Marc Bloch"]
        " 'Digital Humanities' section."]
       [:br] [:br]
       ]]

     [:div {:class "row"}
      [:div {:class "col-md-12"}
       [:a {:href "http://cmb.huma-num.fr"}
        [:img {:src "images/1-logo-v4-noline-md.png" :height 85}]]
       [:img {:src "images/2-logosCMB-soutiens.png" :height 85}]
       [:a {:href "http://algopol.fr"}
        [:img {:src "images/3-logo-algopol-square-cmbdh.png" :height 85}]]
       [:a {:href "http://huma-num.fr"}
        [:img {:src "images/4-logo-huma-num-fr.jpeg" :height 85}]]]]]
     ]))
