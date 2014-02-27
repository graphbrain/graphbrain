(ns graphbrain.web.views.ecocontexts
  (:use hiccup.core
        hiccup.page
        [clojure.string :only [join]]))

(defn- render-context
  [context coll]
  (html
    [:div {:class "panel panel-default"}
     [:div {:class "panel-heading"}
      [:h4 {:class "panel-title"}
       [:a {:data-toggle "collapse" :data-parent "#accordion" :href (str "#collapse" coll)}
        (if (. context isTest)
          (if (. context correct)
            [:span {:class "text-success"} "[ok]"]
            [:span {:class "text-danger"} "[failed]"]))
        (. context getHtmlVertex)]]]
     [:div {:id (str "collapse" coll) :class "panel-collapse collapse"}
      [:div {:class "panel-body"}
       (str
         (. context getHtmlWords)
         (if (and (. context isTest) (. context correct))
           [:p {:class "text-danger"} (str "target: ") (. context targetVertex)]
           "<br />")
         "<br />"
         (. context getHtmlContext))]]]))

(defn render-ecocontexts
  [ctxt-list]
  (join
    (loop [l ctxt-list
           pos 0
           render-list ()]
          (if (empty? l)
            render-list
            (recur (rest l)
              (inc pos)
              (conj render-list (render-context (first l) pos)))))))