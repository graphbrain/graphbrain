(ns graphbrain.web.views.ecopage
  (:use hiccup.core
        hiccup.page))

(defn ecopage [& {:keys [title body-fun]}]
  (html5 {:lang "en"}
    [:head
      [:meta {:charset "utf-8"}]
      [:meta {:viewport "width=device-width, initial-scale=1.0"}]
      [:meta {:description ""}]
      [:meta {:author ""}]
      [:title (str title " - Eco interface")]
      [:link {:href "/css/bootstrap3.css" :rel "Stylesheet"}]
      [:link {:href "/css/codemirror/codemirror.css" :rel "stylesheet"}]
      [:link {:href "/css/codemirror/3024-day.css" :rel "stylesheet"}]
      [:style "body {margin-top: 60px;}"]
      [:script {:src "/js/codemirror/codemirror.js"}]
      [:script {:src "/js/codemirror/eco.js"}]
      [:script {:src "/js/codemirror/active-line.js"}]
      [:script {:src "/js/codemirror/matchbrackets.js"}]]
    [:body
      [:nav {:class "navbar navbar navbar-fixed-top", :role "navigation"}
       [:div {:class "container"}
        [:div {:class "navbar-header"}
         [:button {:type "button" :class "navbar-toggle" :data-toggle "collapse" :data-target ".navbar-ex1-collapse"}
          [:span {:class "sr-only"} "Toggle navigation"]
          [:span {:class "icon-bar"}]
          [:span {:class "icon-bar"}]
          [:span {:class "icon-bar"}]]
         [:a {:class "navbar-brand" :href "/eco"} "Eco interface"]]
        [:div {:class "collapse navbar-collapse navbar-ex1-collapse"}
         [:ul {:class "nav navbar-nav"}
          [:li [:a {:href "/eco"} "Parse"]]
          [:li [:a {:href "/eco/code"} "Code"]]
          [:li [:a {:href "/eco/runtests"} "Run Tests"]]
          [:li [:a {:href "/eco/edittests"} "Edit Tests"]]]]]]
      [:div {:class "container"}
       (body-fun)]
      [:script {:src "/js/jquery-1.7.2.min.js"}]
      [:script {:src "/js/bootstrap3.min.js"}]]))