(ns graphbrain.web.handlers.search
  (:require [graphbrain.web.common :as common]
            [graphbrain.web.contexts :as contexts]
            [graphbrain.db.gbdb :as gb]
            [graphbrain.db.id :as id]
            [graphbrain.db.maps :as maps]
            [graphbrain.db.entity :as entity]
            [graphbrain.db.searchinterface :as si]))

(defn results
  [q ctxts]
  (map #(list (id/eid->id %) (entity/description %))
       (si/query common/gbdb q ctxts)))

(defn reply
  [results mode]
  (pr-str {:type :search
           :mode mode
           :results results}))

(defn process
  [q ctxts mode]
  (reply
   (results q ctxts)
   mode))

(defn handle
  [request]
  (let [q ((request :form-params) "q")
        mode (keyword ((request :form-params) "mode"))
        ctxt ((request :form-params) "ctxt")
        user (common/get-user request)
        ctxts (contexts/active-ctxts ctxt user)]
    (process q ctxts mode)))
