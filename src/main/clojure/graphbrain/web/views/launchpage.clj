(ns graphbrain.web.views.launchpage
  (:use hiccup.core
        hiccup.page))

(defn page
  []
  (html5 {:lang "en"}
         [:head
          [:meta {:charset "utf-8"}]
          [:meta {:viewport "width=device-width, initial-scale=1.0"}]
          [:meta {:description ""}]
          [:meta {:author ""}]
          [:title "GraphBrain"]]
         [:body
          ;; Begin LaunchRock Widget
          [:div {:id "lr-widget"
                 :rel "FUKBW1SQ"}]
          [:script {:type "text/javascript"
                    :src "//ignition.launchrock.com/ignition-current.min.js"}]
          ;; End LaunchRock Widget
          ]))
