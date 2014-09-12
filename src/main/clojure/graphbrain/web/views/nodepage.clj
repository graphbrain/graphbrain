(ns graphbrain.web.views.nodepage
  (:use hiccup.core))

(def ^:private style "
<style type=\"text/css\">
  body {
    padding-top: 60px;
    background:#FFF;
  }
</style>
")

(defn nodepage-view
  [html-content]
  (html
    style
    [:div {:class "container"}
     [:div {:class "row-fluid"}
      [:div {:class "span12"} html-content]]]))
