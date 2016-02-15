;   Copyright (c) 2016 CNRS - Centre national de la recherche scientifique.
;   All rights reserved.
;
;   Written by Telmo Menezes <telmo@telmomenezes.com>
;
;   This file is part of GraphBrain.
;
;   GraphBrain is free software: you can redistribute it and/or modify
;   it under the terms of the GNU Affero General Public License as published by
;   the Free Software Foundation, either version 3 of the License, or
;   (at your option) any later version.
;
;   GraphBrain is distributed in the hope that it will be useful,
;   but WITHOUT ANY WARRANTY; without even the implied warranty of
;   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;   GNU Affero General Public License for more details.
;
;   You should have received a copy of the GNU Affero General Public License
;   along with GraphBrain.  If not, see <http://www.gnu.org/licenses/>.

(ns graphbrain.web.views.barpage
  (:use hiccup.core
        (graphbrain.web.views page)))

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
       [:a {:href "/presentation"
            :role "menu"}
        "About GraphBrain"]]
      [:li {:role "presentation"}
       [:a {:href (str "/n/u/" (:username user))
            :role "menu"}
        "Home"]]
      [:li {:role "presentation"}
       [:a {:href "/docs/help"
            :role "menu"}
        "Help"]]
      [:li {:role "presentation"}
       [:a {:href "#"
            :id "logoutLink"
            :role "menu"}
        "Logout"]]]]))

(defn- context-menu
  [ctxt]
  [:div {:class "dropdown"}
   [:button {:type "button"
             :class "btn btn-info dropdown-toggle"
             :aria-label "Left Align"
             :id "context-menu"
             :data-toggle "dropdown"
             :aria-expanded "true"}
    (str (:name ctxt) " ")
    [:span {:class "caret"}]]
   [:ul {:class "dropdown-menu dropdown-menu-right"
         :role "menu"
         :aria-labelledby "context-menu"}
    [:li {:role "presentation"
          :class "dropdown-header"}
     "Knowledge Graphs"]
    [:li {:role "presentation"}
     [:a {:href "#"
          :id "switch-context-link"
          :role "menu"}
      "Switch"]]
    [:li {:role "presentation"}
     [:a {:href "#"
          :id "create-context-link"
          :role "menu"}
      "Create"]]
    [:li {:role "presentation"}
     [:a {:href (str "/n/" (:id ctxt))
          :role "menu"}
      (str (:name ctxt) " Home")]]]])

(defn view
  [user ctxt content-fun]
  (html
   [:div {:id "nodeback"}
    [:div {:id "topbar"}

     [:div {:class "pull-right topbar-vcenter topbar-menu"}
      (user-menu user)]
     
     (if ctxt
       [:div {:class "pull-right topbar-vcenter topbar-menu"}
        (context-menu ctxt)])
  
     [:div {:class "topbar-element topbar-center topbar-vcenter"}
      [:a {:href "/"}
       [:img {:src "/images/GB_logo_XS.png"
              :alt "graphbrain"}]]]

     [:div {:class "topbar-input-area topbar-center topbar-vcenter"}
      [:form {:class "top-input"
              :id "top-input-field"
              :autocomplete "off"
              :action "/eco"
              :method "post"}
       [:input {:type "text"
                :id "main-input-field"
                :class "top-input-field"
                :placeholder "Search or tell me something"
                :name "input-field"
                :autofocus ""}]]]]

    [:div {:class "alert alert-danger"
           :id "msgbar"
           :role "alert"}]
    
    (content-fun)]))

(defn barpage
  [& {:keys [title css-and-js user ctxt js content-fun]}]
  (page :title title
        :css-and-js css-and-js
        :body-fun #(view user ctxt content-fun)
        :js js))
