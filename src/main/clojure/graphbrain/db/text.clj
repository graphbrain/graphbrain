(ns graphbrain.db.text)

(defn id->text
  [id]
  {:id id
   :type :prog
   :text ""
   :degree 0
   :ts -1})
