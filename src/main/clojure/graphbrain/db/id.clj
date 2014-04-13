(ns graphbrain.db.id
  (:import (com.graphbrain.db ID)))

(defn last-part
  [id]
  (ID/lastPart id))
