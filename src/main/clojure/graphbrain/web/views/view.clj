(ns graphbrain.web.views.view
  (:use hiccup.core))

(def ^:private style "
<style type=\"text/css\">
  body {
    background:#FFF;
    #overflow:hidden;
  }
</style>")

(defn- user-menu
  [user]
  (if (nil? user)
    [:span
     [:a {:class "signupLink" :href "#"} "Sign Up"]
     "&nbsp; | &nbsp"
     [:a {:id "loginLink" :href "#"} "Login"]]

    [:span {:class "dropdown"}
     [:a {:href "#"
          :class "dropdown-toggle"
          :data-toggle "dropdown"}
      [:i {:class "icon-user icon-white"}]
      (:name user)
      [:div {:class "menu-caret"}]]
     [:ul {:class "dropdown-menu"}
      [:li [:a {:href "http://graphbrain.com"} "About GraphBrain"]]
      [:li [:a {:href (str "/node/u/" (:username user))} "Home"]]
      [:li [:a {:href "#" :id "logoutLink"} "Logout"]]]]))

(defn view-view
  [user]
  (html
   style
   ;; navbar
   (html
    [:div {:class "navbar-container"}
     [:div {:class "navbar-element" :style "float:left;"}
      [:a {:href "/"}
       [:img {:src "/images/GB_logo_XS.png"
              :alt "graphbrain"}]]]
     [:div {:class "navbar-element"}
      [:form {:class "nav-search" :id "search-field"}
       [:input {:type "text"
                :id "search-input-field"
                :placeholder "Search"}]]]
     [:div {:class "navbar-element"}
     (user-menu user)]])

   ;; main
   [:div {:id "main-view"}
    [:div {:id "data-view"}
     [:div {:id "view-view"}]]]))
