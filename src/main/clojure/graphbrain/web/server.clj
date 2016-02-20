;   Copyright (c) 2016 CNRS - Centre national de la recherche scientifique.
;   All rights reserved.
;
;   Written by Telmo Menezes <telmo@telmomenezes.com>
;
;   This file is part of GraphBrain.
;
;   GraphBrain is free software: you can redistribute it and/or modify
;   it under the terms of the GNU Affero General Public License as published by
;   the Free Software Foundation, either version 3 of the License, or
;   (at your option) any later version.
;
;   GraphBrain is distributed in the hope that it will be useful,
;   but WITHOUT ANY WARRANTY; without even the implied warranty of
;   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;   GNU Affero General Public License for more details.
;
;   You should have received a copy of the GNU Affero General Public License
;   along with GraphBrain.  If not, see <http://www.gnu.org/licenses/>.

(ns graphbrain.web.server
  (:use compojure.core
        ring.adapter.jetty
        (ring.middleware resource file-info cookies params))
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [graphbrain.hg.connection :as conn]
            [graphbrain.web.handlers.home :as home]
            [graphbrain.web.handlers.nodeactions :as nodeactions]
            [graphbrain.web.handlers.raw :as raw]
            [graphbrain.web.handlers.eco :as eco]
            [graphbrain.web.handlers.nodepage :as nodepage]
            [graphbrain.web.handlers.intersect :as intersect]
            [graphbrain.web.handlers.input :as input]
            [graphbrain.web.handlers.search :as search]
            [graphbrain.web.handlers.change :as change]
            [graphbrain.web.handlers.define :as define]
            [graphbrain.web.handlers.edgedata :as ed]))

(defn app-routes
  [hg]
  (routes (GET "/" request (home/handle request))
          (GET "/n/*" request (nodepage/handle request hg))
          (GET "/x" request (intersect/handle request hg))
          (GET "/raw/*" request (raw/handle request hg))
          (GET "/eco" request (eco/handle request hg))
          
          (POST "/n/*" request (nodeactions/handle request hg))
          (POST "/input" request (input/handle request hg))
          (POST "/search" request (search/handle request hg))
          (POST "/eco" request (eco/handle request hg))
          (POST "/change" request (change/handle request hg))
          (POST "/define" request (define/handle request hg))
          (POST "/edge-data" request (ed/handle request hg))

          (route/not-found "<h1>Page not found</h1>")))

(defn wrap-dev
  [handler dev]
  (fn [request]
    (let [response (handler request)]
      (assoc response :dev dev))))

(defn app
  [hg dev]
  (-> hg
      app-routes
      (wrap-resource "")
      wrap-file-info
      wrap-params
      wrap-cookies
      (wrap-dev dev)))

(def handler
  (handler/site
   (app (conn/create) true)))

(defn start!
  [port]
  (println (str "Starting web server on port " port))
  (run-jetty (app (conn/create) false) {:port port}))
