(ns graphbrain.web.handlers.search
  (:use (graphbrain.web common))
  (:require [clojure.data.json :as json]
            [graphbrain.db.graph :as gb]
            [graphbrain.db.searchinterface :as si]))

(defn handle-search
  [request]
  (let [q ((request :form-params) "q")
        results (si/query graph q)
        results-list (map (fn [x] (list x (gb/description graph x))) results)]
    (json/write-str {:count (count results-list)
                     :results results-list})))
