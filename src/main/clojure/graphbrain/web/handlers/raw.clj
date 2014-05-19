(ns graphbrain.web.handlers.raw
  (:use [clojure.string :only [join]]
        (graphbrain.web common)
        (graphbrain.web.views page raw))
  (:require [graphbrain.db.gbdb :as gb]
            [graphbrain.web.cssandjs :as css+js]))

(defn- raw-html
  [request user vertex]
  (let
    [vertex-id (:id vertex)]
    (str "<h2>Vertex: " vertex-id "</h2>" (str vertex) "<br/><br/>"
      (let [user-id (if user (:id user) "")
            edges (gb/id->edges gbdb vertex-id user-id)]
        (join (map (fn [x] (str (:id x) "<br />")) edges))))))

(defn handle-raw
  [request]
  (let [user (get-user request)
        vertex (gb/getv gbdb (:* (:route-params request)))]
    (page :title (:label vertex)
          :css-and-js (css+js/css+js)
          :user user
          :page :raw
          :body-fun (fn [] (raw-view (raw-html request user vertex)))
          :js "")))
