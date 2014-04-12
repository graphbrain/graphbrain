(ns graphbrain.web.views.node
  (:use hiccup.core))

(def ^:private style "
<style type=\"text/css\">
  body {
    background:#FFF;
    overflow:hidden;
  }
</style>")

(defn node-view []
  (html
    style
    [:div {:id "main-view"}
      [:div {:id "nav-spacer"}]
      [:div {:id "graph-view"}]]
    [:div {:id "ai-chat"}]
    [:div {:id "rel-list"}]))
