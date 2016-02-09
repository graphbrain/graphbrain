(ns graphbrain.hg.id
  (:import (java.security SecureRandom)))

(defn hashed
  [str]
  (let [h (loop [s str
                 x 1125899906842597]  ;; prime
            (if (empty? s) x
              (recur (rest s) (unchecked-multiply 31 (+ x (long (first s)))))))]
    (Long/toHexString h)))

(defn random-hash
  []
  (hashed
   (.toString
    (BigInteger. 130 (new SecureRandom)) 32)))

(defn- count-end-slashes
  [str]
  (loop [s (reverse str)
         c 0]
    (if (= (first s) \/)
      (recur (rest s) (inc c))
      c)))

(defn parts
  [id]
  (let [ps (clojure.string/split id #"/")
        c (count-end-slashes id)]
    (if (> c 0)
      (apply conj ps (repeat c ""))
      ps)))

(defn count-parts
  [id]
  (count (parts id)))

(defn build
  [parts]
  (clojure.string/join "/" parts))

(defn sanitize
  [str]
  (clojure.string/replace
   (clojure.string/replace (.toLowerCase str) "/" "_") " " "_"))
