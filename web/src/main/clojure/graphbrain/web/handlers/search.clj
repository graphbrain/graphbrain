(ns graphbrain.web.handlers.search
  (:use (graphbrain.web common))
  (:require [clojure.data.json :as json])
  (:import (com.graphbrain.db SearchInterface)))

(defn handle-search
  [request]
  (let [q ((request :form-params) "q")
        si (new SearchInterface graph)
        results (. si query q)
        results-list (map (fn [x] (list x (. graph description x))) results)]
    (json/write-str {:count (count results-list)
                     :results results-list})))