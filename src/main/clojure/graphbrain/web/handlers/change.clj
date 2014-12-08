(ns graphbrain.web.handlers.change
  (:require [graphbrain.web.common :as common]
            [graphbrain.web.contexts :as contexts]
            [graphbrain.db.gbdb :as gb]
            [graphbrain.db.id :as id]
            [graphbrain.db.maps :as maps]
            [graphbrain.db.entity :as entity]
            [graphbrain.db.searchinterface :as si]))

(defn reply
  []
  (pr-str {:type :change}))

(defn process
  [user edge-id old-id new-id ctxts]
  (let [edge (maps/id->edge edge-id)
        user-id (:id user)
        old-eid (gb/id->eid common/gbdb old-id)
        new-eid (gb/id->eid common/gbdb new-id)
        ids (id/id->ids edge-id)
        new-ids (map #(if (= % old-eid) new-eid %)
                     ids)
        new-edge (maps/ids->edge new-ids)]
    (gb/remove! common/gbdb edge user-id)
    (gb/putv! common/gbdb new-edge user-id))
  (reply))

(defn handle
  [request]
  (let [edge ((request :form-params) "edge")
        old-id ((request :form-params) "old-id")
        new-id ((request :form-params) "new-id")
        user (common/get-user request)
        ctxts (contexts/active-ctxts request user)]
    (process user edge old-id new-id ctxts)))
