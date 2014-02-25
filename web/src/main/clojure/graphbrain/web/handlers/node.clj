(ns graphbrain.web.handlers.node
  (:use (graphbrain.web common)
        (graphbrain.web.views page node))
  (:import (com.graphbrain.web NavBar CssAndJs VisualGraph)))

(defn- js
  [node user]
  (str "var data = " (VisualGraph/generate (. node id) user)  ";\n"
    "var errorMsg = \"\";\n"))

(defn handle-node
  [response]
  (let
    [user (get-user response)
     vert (. graph get (:* (:route-params response)))]
    (page
      :title (. vert label)
      :css-and-js (. (new CssAndJs) cssAndJs)
      :navbar (. (new NavBar user "node") html)
      :body-fun node-view
      :js (js vert user))))