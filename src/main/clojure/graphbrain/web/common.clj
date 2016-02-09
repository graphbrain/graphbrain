(ns graphbrain.web.common
  (:require [graphbrain.hg.ops :as hgops]
            [clojure.tools.logging :as log]))

(def production?
  (= "production" (get (System/getenv) "APP_ENV")))

(def development?
  (not production?))

(defn init-graph!
  []
  (def hg (hgops/hg)))

(defn log
  [request msg]
  (log/info
   (str "[" (:remote-addr request) "] " msg)))
