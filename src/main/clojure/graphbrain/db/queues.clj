(ns graphbrain.db.queues)

(def consensus-queue (java.util.concurrent.LinkedBlockingQueue.))
(def consensus-active (atom false))

(defn consensus-enqueue!
  [id]
  (if @consensus-active (.put consensus-queue id)))
