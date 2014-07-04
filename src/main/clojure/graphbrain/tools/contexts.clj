(ns graphbrain.tools.contexts
  (:require[graphbrain.db.gbdb :as gb]))

(defn add-context-to-user!
  [user-id ctxt-id]
  (let [g (def g (gb/gbdb))
        user (gb/getv g user-id)]
    (prn (str "adding context " ctxt-id " to " user-id))
    (gb/add-ctxt-to-user! g user ctxt-id)
    (prn "done.")))
