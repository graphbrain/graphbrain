(ns graphbrain.web.views.navbar)

(defn- user-id
  [user]
  (if (nil? user) "" (:id user)))

(defn- tools
  [page]
  (if (= page :node)
    [:li [:button {:class "btn", :id "ai-chat-button", :data-toggle="button"}
          [:i {:class "icon-asterisk icon-black"}]
          "Talk to AI"]]
    ""))

(defn- user-stuff
  [user page]
  (if (nil? user)
    [:div {:class "pull-right"}
     [:ul {:class "nav"}
      [:li [:a {:class "signupLink" :href "#"} "Sign Up"]]
      [:li [:a {:class "loginLink" :href "#"} "Login"]]]]

    [:div {:class "pull-right"}
     [:ul {:class "nav"}
      (tools page)
      [:li {:class "dropdown"}
       [:a {:href "#"
            :class "dropdown-toggle"
            :data-toggle "dropdown"}
        [:i {:class "icon-user icon-black"}]
        (:name user)
        [:b {:class "caret"}]]
       [:ul {:class "dropdown-menu"}
        [:li [:a {:href "http://graphbrain.com"} "About GraphBrain"]]
        [:li [:a {:href (str "/node/user/" (:username user))} "Home"]]
        [:li [:a {:href "#" :id "logoutLink"} "Logout"]]]]]]))

(defn navbar
  [user page]
  (if (or (= page :comingsoon) (= page :home)) ""
      [:div {:class "navbar navbar-fixed-top"}
       [:div {:class "navbar-inner"}
        [:div {:class "container-fluid"}
         [:a {:class "btn btn-navbar"
              :data-toggle "collapse"
              :data-target ".nav-collapse"}
          [:span {:class "icon-bar"}]
          [:span {:class "icon-bar"}]
          [:span {:class "icon-bar"}]]
         [:a {:href "/"}
          [:img {:src "/images/GB_logo_XS.png"
                 :class "brand"
                 :alt "graphbrain"}]]
         [:div {:class "nav-collapse"}
          [:ul {:class "nav"}
           [:li [:form {:class "navbar-search" :id "search-field"}
                 [:input {:type "text"
                          :id "search-input-field"
                          :placeholder "Search"}]]]]]
         (user-stuff user page)]]
       [:div {:id "alert" :class "alert" :style "visibility:hidden; margin:0px"}
        [:div {:id "alertMsg"}]]]))
