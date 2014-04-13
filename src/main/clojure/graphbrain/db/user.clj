(ns graphbrain.db.user
  (:require [graphbrain.db.graph :as gb])
  (:import (com.graphbrain.db UserNode)))

(defn check-session
  [user session]
  (.checkSession (gb/map->user-obj user) session))
