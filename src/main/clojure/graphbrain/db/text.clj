(ns graphbrain.db.text
  (:require [graphbrain.db.id :as id]))

(defn text->id
  [text]
  (let [short-text (subs text 0 (min (count text) 20))
        hash (id/hashed text)]
    (str "t/" hash "/" (id/sanitize short-text))))

(defn id->text
  [id]
  {:id id
   :type :text
   :text ""
   :degree -1
   :ts -1})

(defn text->vertex
  [text]
  {:id (text->id text)
   :type :text
   :text text
   :degree 0
   :ts -1})
