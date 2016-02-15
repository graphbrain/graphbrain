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

(ns graphbrain.gbui.encoder)

(defn encode
  [str]
  (clojure.string/replace
   (clojure.string/replace str "#" "##")
   "'" "#1"))

(defn- next-char
  [c0 c]
  (case c
    \# (if (nil? c0) \# nil)
    \1 (if (nil? c0) \' 1)
    c))

(defn- decode-char
  [str c]
  (conj str
        (next-char (first str) c)))

(defn decode
  [s]
  (apply str
         (reverse
          (reduce decode-char '() s))))
