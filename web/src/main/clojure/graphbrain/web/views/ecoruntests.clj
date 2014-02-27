(ns graphbrain.web.views.ecoruntests
  (:use hiccup.core
        (graphbrain.web.views ecocontexts)))

(defn ecoruntests-view
  [ctxt-list]
  (html
    [:form {:role "form" :action "runtests" :method "post"}
     [:button {:type "submit" :class "btn btn-default"} "Run"]]
    [:br]
    [:br]
    (render-ecocontexts ctxt-list)))