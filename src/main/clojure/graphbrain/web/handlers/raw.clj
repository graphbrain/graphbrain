(ns graphbrain.web.handlers.raw
  (:use [clojure.string :only [join]]
        (graphbrain.web.views page raw))
  (:require [graphbrain.db.gbdb :as gb]
            [graphbrain.db.vertex :as vertex]
            [graphbrain.web.common :as common]
            [graphbrain.web.contexts :as contexts]
            [graphbrain.web.cssandjs :as css+js]
            [graphbrain.web.views.barpage :as bar]))

(defn- js
  []
  "var ptype='raw';")

(defn- raw-html
  [request user vertex ctxts]
  (let [vertex-id (:id vertex)]
    (str "<h2>Vertex: " vertex-id "</h2>" (str vertex) "<br/><br/>"
         (let [user-id (if user (:id user) "")
               edges (gb/id->edges common/gbdb vertex-id ctxts)]
           (join (map #(str (:id %) "<br />") edges))))))

(defn handle
  [request]
  (let [user (common/get-user request)
        ctxts (contexts/active-ctxts request user)
        vertex (gb/getv common/gbdb (:* (:route-params request)) ctxts)]
    (bar/barpage :title (vertex/label vertex)
                 :css-and-js (css+js/css+js)
                 :user user
                 :content-fun #(raw-view (raw-html request user vertex ctxts))
                 :js (js))))
