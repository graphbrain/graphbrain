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

(ns graphbrain.hg.symbol
  (:import (java.security SecureRandom)))

(defn hashed
  "Creates an hash code for a string."
  [str]
  (let [h (loop [s str
                 x 1125899906842597]  ;; prime
            (if (empty? s) x
              (recur (rest s) (unchecked-multiply 31 (+ x (long (first s)))))))]
    (Long/toHexString h)))

(defn random-hash
  "Creates random hash code."
  []
  (hashed
   (.toString
    (BigInteger. 130 (new SecureRandom)) 32)))

(defn sym-type
  "Type of symbol: :concept, :edge, :integer, :float or :url"
  [sym]
  (cond
   (coll? sym) :edge
   (integer? sym) :integer
   (float? sym) :float
   (clojure.string/starts-with? sym "http://") :url
   (clojure.string/starts-with? sym "https://") :url
   :else :concept))

(defn parts
  "Splits a symbol into its parts.
  All symbol types except :concept only have one part."
  [sym]
  (if (= (sym-type sym) :concept)
    (clojure.string/split sym #"/")
    [sym]))

(defn root
  "Extracts the root of a symbol (e.g. the root of graphbrain/1 is graphbrain)"
  [sym]
  (first (parts sym)))

(defn root?
  "Is the symbol the root of itself?"
  [sym]
  (= sym (root sym)))

(defn build
  "Build a concept symbol for a collection of strings."
  [parts]
  (clojure.string/join "/" parts))

(defn str->symbol
  "Converts a string into a valid symbol"
  [str]
  (clojure.string/replace
   (clojure.string/replace (.toLowerCase str) "/" "_") " " "_"))

(defn symbol->str
  "Converts a symbol into a string representation."
  [sym]
  (case (sym-type sym)
    :concept (clojure.string/replace (root sym) "_" " ")
    :url sym
    (str sym)))

(defn new-meaning
  "Creates a new symbol for the given root.
   If given symbols is not a root, return it unchanged."
  [symb]
  (if (root? symb)
    (build [symb (random-hash)])
    symb))
