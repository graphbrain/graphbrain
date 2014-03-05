(ns graphbrain.web.views.raw
  (:use hiccup.core))

(def ^:private style "
<style type=\"text/css\">
  body {
    padding-top: 60px;
    background:#FFF;
  }
</style>
")

(defn raw-view
  [html-content]
  (html
    style
    [:div {:class "container"}
     [:div {:class "row-fluid"}
      [:div {:class "span12"} html-content]]]))