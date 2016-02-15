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
  (:require [graphbrain.web.common :as common])
  (:use hiccup.core
        hiccup.page))


(def google-analytics
  "(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){\\
(i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),\\
m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)\\
})(window,document,'script','//www.google-analytics.com/analytics.js','ga');\\
\\
ga('create', 'UA-58921414-1', 'auto');\\
ga('send', 'pageview');")


(defn page [& {:keys [title css-and-js body-fun js]}]
  (html5 {:lang "en"}
         [:head
          [:meta {:charset "utf-8"}]
          [:meta {:viewport "width=device-width, initial-scale=1.0"}]
          [:meta {:description ""}]
          [:meta {:author ""}]
          [:title (str title " - GraphBrain")]
          [:link {:href "/css/bootstrap.min.css" :type "text/css" :rel "Stylesheet"}]
          css-and-js
          [:script {:src "http://html5shim.googlecode.com/svn/trunk/html5.js"}]]
         [:body
          (if common/production? [:script google-analytics])
          (body-fun)
          [:script {:language "javascript"} js]]))
