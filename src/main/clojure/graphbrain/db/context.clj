(ns graphbrain.db.context)

(defn id->context
  [id]
  {:id id
   :type :context
   :name ""
   :access ""
   :size -1
   :degree -1
   :ts -1})
