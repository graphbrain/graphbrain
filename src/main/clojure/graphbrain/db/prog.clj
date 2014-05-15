(ns graphbrain.db.prog)

(defn id->prog
  [id]
  {:id id
   :type :prog
   :prog ""
   :degree -1
   :ts -1})
