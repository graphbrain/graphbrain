(ns graphbrain.db.text
  (:require [graphbrain.db.id :as id]
            [cemerick.url :as url]))

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

(defn text->pseudo
  [text]
  (str "t/"
       (url/url-encode text)))

(defn pseudo?
  [id]
  (let [parts (id/parts id)]
    (and (= (count parts) 2)
         (= (first parts) "t"))))

(defn text->vertex
  [text]
  {:id (text->id text)
   :type :text
   :text text})

(defn pseudo->vertex
  [pseudo]
  (text->vertex
   (url/url-decode
    (second (id/parts pseudo)))))
