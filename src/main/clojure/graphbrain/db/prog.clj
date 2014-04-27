(ns graphbrain.db.prog)

(defn id->prog
  [id]
  {:id id
   :type :prog
   :prog ""
   :degree 0
   :ts -1})
