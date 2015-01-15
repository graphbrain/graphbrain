(ns graphbrain.web.handlers.contexts
  (:require [graphbrain.web.common :as common]
            [graphbrain.db.gbdb :as gb]
            [graphbrain.db.id :as id]
            [graphbrain.db.maps :as maps]
            [graphbrain.db.context :as context]))

(defn query
  [gbdb user-id]
  (let [follows (gb/pattern->edges gbdb ["r/*following" user-id "*"] [user-id])]
    (map #(second (maps/participant-ids %)) follows)))

(defn results
  [user]
  (cons
   (list (:id user) "Personal")
   (map #(list (id/eid->id %) (context/label %))
        (query common/gbdb (:id user)))))

(defn reply
  [results]
  (pr-str {:results results}))

(defn handle
  [request]
  (reply
   (results
    (common/get-user request))))
