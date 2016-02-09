(ns graphbrain.web.handlers.raw
  (:use [clojure.string :only [join]]
        (graphbrain.web.views page raw))
  (:require [graphbrain.db.gbdb :as gb]
            [graphbrain.db.edgestr :as es]
            [graphbrain.web.common :as common]
            [graphbrain.web.cssandjs :as css+js]
            [graphbrain.web.views.barpage :as bar]))

(defn- js
  []
  "var ptype='raw';")

(defn- raw-html
  [request vertex-id]
  (str "<h2>Vertex: " vertex-id "</h2><br/><br/>"
       (let [edges (gb/neighbors common/gbdb vertex-id)]
         (join (map #(str (es/edge->str %) "<br />") edges)))))

(defn handle
  [request]
  (let [vertex-id (:* (:route-params request))]
    (bar/barpage :title vertex-id
                 :css-and-js (css+js/css+js)
                 :content-fun #(raw-view (raw-html request vertex-id))
                 :js (js))))
