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
            [graphbrain.web.common :as common]
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

(defroutes app-routes
  (GET "/" request (home/handle request))
  (POST "/n/*" request (nodeactions/handle request))
  (GET "/n/*" request (nodepage/handle request))
  (GET "/x" request (intersect/handle request))
  (GET "/raw/*" request (raw/handle request))
  (POST "/input" request (input/handle request))
  (POST "/search" request (search/handle request))
  (GET "/eco" request (eco/handle request))
  (POST "/eco" request (eco/handle request))
  (POST "/change" request (change/handle request))
  (POST "/define" request (define/handle request))
  (POST "/edge-data" request (ed/handle request))
  (route/not-found "<h1>Page not found</h1>"))

(defn init-server!
  []
  (common/init-graph!))

(def app
  (-> app-routes
      (wrap-resource "")
      wrap-file-info
      wrap-params
      wrap-cookies))

(def handler
  (handler/site app))

(defn start!
  []
  (init-server!)
  (let [port (if common/production? 80 3000)]
    (run-jetty app {:port port})))
