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

(ns graphbrain.web.cssandjs
  (:use [graphbrain.web.common :as common]))

(defn- version
  []
  (if common/production?
    ""
    (str "?" (rand-int 999999999))))

(defonce analytics-js
  "<script type='text/javascript'>
     var _gaq = _gaq || [];
     _gaq.push(['_setAccount', 'UA-30917836-1']);
     _gaq.push(['_trackPageview']);
     (function() {
         var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
         ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
         var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
    })();
  </script>")

(defn css+js
  []
  (str "<link href='/css/gb.css" (version) "' type='text/css' rel='Stylesheet' />"
       "<script src='/js/jquery-1.11.1.min.js' type='text/javascript'></script>"
       "<script src='/js/bootstrap.min.js' type='text/javascript' ></script>"
       "<script src='/js/gbui.js" (version) "' type='text/javascript' ></script>"))
