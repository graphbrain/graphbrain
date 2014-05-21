(ns graphbrain.db.text
  (:require [graphbrain.db.id :as id]))

(defn text->id
  [text]
  (let [short-text (subs text 0 (min (count text) 20))
        hash (id/hashed text)]
    (str "t/" hash "/" (id/sanitize short-text))))

(defn text->vertex
  [text]
  {:id (text->id text)
   :type :text
   :text text})
