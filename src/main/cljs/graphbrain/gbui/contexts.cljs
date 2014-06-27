(ns graphbrain.gbui.contexts
  (:require-macros [hiccups.core :as hiccups])
  (:require [jayq.core :as jq]
            [graphbrain.gbui.globals :as g]
            [hiccups.runtime :as hiccupsrt])
  (:use [jayq.core :only [$]]))

(hiccups/defhtml ctxt->html [ctxt]
  [:div {:class "dropdown dropup ctxt"}
    [:a {:href "#" :class "dropdown-toggle" :data-toggle "dropdown" :id (:name ctxt)} (:name ctxt)]
    [:ul {:class "dropdown-menu" :role "menu" :aria-labelledby (:name ctxt)}
      [:li
        [:a {:href "#"} "Remove"]]
      [:li
        [:a {:href "#"} "Write"]]]])

(defn init-contexts!
  []
  (let [ctxts (:ctxts @g/graph)
        html (clojure.string/join (map ctxt->html ctxts))
        html (str html "<div class='ctxt'>+</div>")]
    (.html ($ "#ctxt-area") html)))
