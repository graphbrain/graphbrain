(ns graphbrain.web.handlers.search
  (:use (graphbrain.web common))
  (:require [clojure.data.json :as json]
            [graphbrain.web.common :as common]
            [graphbrain.db.gbdb :as gb]
            [graphbrain.db.id :as id]
            [graphbrain.db.entity :as entity]
            [graphbrain.db.searchinterface :as si]))

(defn handle-search
  [request]
  (let [q ((request :form-params) "q")
        user (common/get-user request)
        results (si/query gbdb q (common/user->ctxts user))
        results-list (map #(list (id/eid->id %) (entity/description %)) results)]
    (json/write-str {:count (count results-list)
                     :results results-list})))