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

(ns graphbrain.web.visualvert
  (:require [graphbrain.hg.ops :as ops]
            [graphbrain.hg.symbol :as symb]))

(defn sym->html
  [sym]
  (str "<a href='/n/" sym "'>"
       (symb/symbol->str sym)
       "</a>"))

(defn sym->visual
  [sym]
  (let [sym-type (symb/sym-type sym)
        vnode (case sym-type
                :concept {:text (symb/symbol->str sym)
                          :sub "description"}
                :url {:text sym
                      :url sym
                      :icon (str "http://www.google.com/s2/favicons?domain=" sym)}
                :edge {:id (str sym)
                       :text (str sym)}
                {:text (symb/symbol->str sym)})]
    (assoc vnode :type sym-type :id (str sym))))
