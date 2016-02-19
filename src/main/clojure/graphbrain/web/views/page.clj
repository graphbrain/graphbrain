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

(ns graphbrain.web.views.page
  (:use hiccup.core
        hiccup.page))

(defn page [& {:keys [title css-and-js body-fun js]}]
  (html5 {:lang "en"}
         [:head
          [:meta {:charset "utf-8"}]
          [:meta {:viewport "width=device-width, initial-scale=1.0"}]
          [:meta {:description ""}]
          [:meta {:author ""}]
          [:title (str title " - Graphbrain")]
          [:link {:href "/css/bootstrap.min.css" :type "text/css" :rel "Stylesheet"}]
          css-and-js
          [:script {:src "http://html5shim.googlecode.com/svn/trunk/html5.js"}]]
         [:body
          (body-fun)
          [:script {:language "javascript"} js]]))
