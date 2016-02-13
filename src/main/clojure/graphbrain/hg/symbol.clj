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
  "Type of symbol: :concept, :integer, :float or :url"
  [sym]
  (cond
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
