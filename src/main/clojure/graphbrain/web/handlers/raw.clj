(ns graphbrain.web.handlers.raw
  (:use [clojure.string :only [join]]
        (graphbrain.web common)
        (graphbrain.web.views page raw))
  (:require [graphbrain.web.cssandjs :as css+js]))

(defn- raw-html
  [request user vertex]
  (let
    [vertex-id (. vertex id)]
    (str "<h2>Vertex: " vertex-id "</h2>"
      (. vertex raw)
      (let
        [user-id (if user (. user id))
         edges (. graph edges vertex-id user-id)]
        (join
          (map
            (fn [x] (str (. x id) "<br />")) edges))))))

(defn handle-raw
  [request]
  (let [user (get-user request)
        vertex (. graph get (:* (:route-params request)))]
    (page :title (. vertex label)
          :css-and-js (css+js/css+js)
          :user user
          :page :raw
          :body-fun (fn [] (raw-view (raw-html request user vertex)))
          :js "")))
