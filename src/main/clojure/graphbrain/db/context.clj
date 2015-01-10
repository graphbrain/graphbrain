(ns graphbrain.db.context
  (:require [graphbrain.db.id :as id]))

(defn text
  [id]
  (clojure.string/replace (id/last-part id) "_" " "))

(defn label
  [id]
  (let [desc (text id)]
    (clojure.string/join
     (cons (.toUpperCase (str (first desc))) (rest desc)))))
