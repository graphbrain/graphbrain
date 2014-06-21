(ns graphbrain.tools.removecontext
  (:require[graphbrain.db.gbdb :as gb]))

(defn run!
  [ctxt]
  (let [g (gb/gbdb)]
    (prn (str "removing context: " ctxt))
    (gb/remove-context! g ctxt)
    (prn "done.")))
