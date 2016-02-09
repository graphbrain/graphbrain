(ns graphbrain.web.common
  (:require [graphbrain.db.gbdb :as gb]
            [clojure.tools.logging :as log]))

(def production?
  (= "production" (get (System/getenv) "APP_ENV")))

(def development?
  (not production?))

(defn init-graph!
  []
  (def gbdb (gb/gbdb)))

(defn log
  [request msg]
  (log/info
   (str "[" (:remote-addr request) "] " msg)))
