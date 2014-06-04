(ns graphbrain.db.text
  (:require [graphbrain.db.id :as id]))

(defn sanitize-text
  [str]
  (clojure.string/replace
   (clojure.string/replace
    (clojure.string/replace
     (clojure.string/replace str "/" "_") " " "_") "(" "_") ")" "_"))

(defn id->text
  [gbdb id]
  (let [text (id/last-part id)
        text (clojure.string/replace text "_" " ")]
    {:id id
     :type :text
     :text text}))

(defn text->id
  [text]
  (let [short-text (subs text 0 (min (count text) 100))
        hash (id/hashed text)]
    (str "t/" hash "/" (sanitize-text short-text))))

(defn text->vertex
  [text]
  {:id (text->id text)
   :type :text
   :text text})
