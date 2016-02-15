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

(ns graphbrain.web.handlers.docs
  (:require [graphbrain.hg.ops :as hgops]
            [graphbrain.web.common :as common]
            [graphbrain.web.cssandjs :as css+js]
            [graphbrain.web.views.docs :as docs]
            [graphbrain.web.encoder :as enc]
            [markdown.core :as md]))

(defn- data
  [user ctxt ctxts]
  #_{:ctxts (contexts/contexts-map ctxts (:id user))
   :context (contexts/context-data (:id user) (:id user))})

(defn- js
  [user ctxt ctxts]
  (str "var ptype='help';"
       "var data='" (enc/encode (pr-str
                      (data user ctxt ctxts))) "';"))

(defn handle
  [request]
  #_(let
      [user (common/get-user request)
       page (:* (:route-params request))
       ctxts (contexts/active-ctxts (:id user) user)
       ctxt (contexts/context-data (:id user) (:id user))
       html (md/md-to-html-string
             (slurp
              (clojure.java.io/resource
               (str "docs/" page ".md"))))]
    (common/log request "docs")
    (docs/docs :title "Docs"
               :css-and-js (css+js/css+js)
               :user user
               :ctxt ctxt
               :js (js user ctxt ctxts)
               :html html)))
