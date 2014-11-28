(ns graphbrain.web.views.ecoparser
  (:use hiccup.core
        (graphbrain.web.views ecocontexts)))

(defn ecoparser-view
  [text ctxt-list]
  (html
    [:form {:role "form" :action "eco" :method "post"}
     [:div {:class "form-group"}
      [:textarea {:name "text" :cols "50" :rows "10" :style "width:500px"}
       text]]
     [:button {:type "submit" :class "btn btn-default"} "Parse"]]
    [:br]
    [:br]
    (render-ecocontexts ctxt-list)))