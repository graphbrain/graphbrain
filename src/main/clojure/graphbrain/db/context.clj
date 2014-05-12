(ns graphbrain.db.context)

(defn id->context
  ([id degree ts]
     {:id id
      :type :context
      :name ""
      :access ""
      :size 0
      :degree degree
      :ts ts})
  ([id]
     (id->context id 0 -1)))
