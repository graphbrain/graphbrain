(ns graphbrain.web.views.nodepage
  (:use hiccup.core)
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

(defn view
  [vert user]
  (html
   style
   ;; topbar
   (html
    [:div {:id "topbar"}
     [:div {:class "topbar-menu topbar-center"}
      (user-menu user)]
     [:div {:class "topbar-element topbar-center"}
      [:a {:href "/"}
        [:img {:src "/images/GB_logo_XS.png"
               :alt "graphbrain"}]]]
     [:div {:class "topbar-input-area topbar-center"}
      [:form {:class "top-input" :id "search-field"}
       [:input {:type "text"
                :id "search-input-field"
                :class "top-input-field"
                :placeholder "Search or say something"}]]]])

   ;; main
   [:div {:id "nodeback"}
    [:div {:id "nodepage"}
     [:div {:class "np-title"}
      (vertex/label vert)]
     [:div {:class "np-desc"}
      (entity/description vert)]
     [:div {:id "frames"}]]]))
