(ns graphbrain.web.handlers.nodepage
  (:use [clojure.string :only [join]]
        (graphbrain.web.views page nodepage))
  (:require [graphbrain.db.gbdb :as gb]
            [graphbrain.db.vertex :as vertex]
            [graphbrain.web.common :as common]
            [graphbrain.web.contexts :as contexts]
            [graphbrain.web.cssandjs :as css+js]))

(defn- raw-html
  [request user vertex ctxts]
  (let
    [vertex-id (:id vertex)]
    (str "<h2>Vertex: " vertex-id "</h2>" (str vertex) "<br/><br/>"
      (let [user-id (if user (:id user) "")
            edges (gb/id->edges common/gbdb vertex-id ctxts)]
        (join (map (fn [x] (str (:id x) "<br />")) edges))))))

(defn handle-nodepage
  [request]
  (let [user (common/get-user request)
        ctxts (contexts/active-ctxts request user)
        vertex (gb/getv common/gbdb (:* (:route-params request)) ctxts)]
    (page :title (vertex/label vertex)
          :user user
          :page :raw
          :body-fun (fn [] (nodepage-view (raw-html request user vertex ctxts)))
          :js "")))
