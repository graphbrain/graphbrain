(ns graphbrain.gbui.contexts
  (:require [jayq.core :as jq]
            [graphbrain.gbui.globals :as g])
  (:use [jayq.core :only [$]]))

(defn- ctxt->html
  [ctxt]
  (str "<div id='ctxt' class='dropdown'>"
       "<a href='#' class='dropdown-toggle' data-toggle='dropdown'>" (:name ctxt) ""
       "<ul class='dropdown-menu dropup'><li><a href='#'>Remove</a></li><li><a href='#'>Write</a></li></ul>"
       "</div>"))

(defn init-contexts!
  []
  (let [ctxts (:ctxts @g/graph)
        html (clojure.string/join (map ctxt->html ctxts))
        html (str html "<div id='ctxt'>+</div>")]
    (.html ($ "#ctxt-area") html)))
