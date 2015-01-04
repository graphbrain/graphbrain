(ns graphbrain.web.views.barpage
  (:use hiccup.core
        (graphbrain.web.views page))
  (:require [graphbrain.db.vertex :as vertex]
            [graphbrain.db.entity :as entity]))

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
     [:a {:class "signupLink" :href "#"} "sign up"]
     "&nbsp; | &nbsp;"
     [:a {:id "loginLink" :href "#"} "login"]]
    
    [:div {:class "dropdown"}
     [:a {:href "#"
          :class ""
          :id "user-menu"
          :data-toggle "dropdown"}
      ;;[:img {:src "http://www.gravatar.com/avatar/205e460b479e2e5b48aec07710c08d50?s=25"}]
      ;;"&nbsp;"
      ;;[:i {:class "icon-user"}]
      (str (:name user) " ")
      [:span {:class "caret"}]]
     [:ul {:class "dropdown-menu dropdown-menu-right"
           :role "menu"
           :aria-labelledby "user-menu"}
      [:li {:role "presentation"}
       [:a {:href "http://graphbrain.com"
            :role "menu"}
        "About GraphBrain"]]
      [:li {:role "presentation"}
       [:a {:href (str "/x/u/" (:username user))
            :role "menu"}
        "Home"]]
      [:li {:role "presentation"}
       [:a {:href "#"
            :id "logoutLink"
            :role "menu"}
        "Logout"]]]]))

(defn view
  [user content-fun]
  (html
   [:div {:id "nodeback"}
    [:div {:id "topbar"}
     [:div {:class "pull-right topbar-vcenter topbar-menu"}
      (user-menu user)]
     [:div {:class "topbar-element topbar-center topbar-vcenter"}
      [:a {:href "/"}
       [:img {:src "/images/GB_logo_XS.png"
              :alt "graphbrain"}]]]
     [:div {:class "topbar-input-area topbar-center topbar-vcenter"}
      [:form {:class "top-input" :id "top-input-field"}
       [:input {:type "text"
                :id "main-input-field"
                :class "top-input-field"
                :placeholder "Search or tell me something"
                :name "input-field"
                :autofocus ""}]]]]
    
    (content-fun)]))

(defn barpage
  [& {:keys [title css-and-js user js content-fun]}]
  (page :title title
        :css-and-js css-and-js
        :body-fun #(view user content-fun)
        :js js))
