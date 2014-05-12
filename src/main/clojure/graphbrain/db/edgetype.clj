(ns graphbrain.db.edgetype
  (:require [graphbrain.db.id :as id]))

(defn id->edgetype
  ([id degree ts]
   {:id id
    :type :edge-type
    :label ""
    :degree degree
    :ts ts})
  ([id]
   (id->edgetype id 0 -1)))

(defn negative?
  [id]
  (= (first (id/parts (id/local->global id))) "n"))

(defn negate
  [id]
  (if (negative? id) id
    (let [owner-id (id/owner id)
          global-id (id/local->global id)
          neg-id (str "n/" global-id)]
      (if (empty? owner-id)
        neg-id
        (id/global->local neg-id owner-id)))))

(defn build-id
  [text]
  (str "r/" (id/sanitize text)))

(defn label
  [id]
  (let [last (id/last-part id)]
    (case last
      "+pos" "is"
      "+can_mean" "can mean"
      "+type_of" "is type of"
      "+synonym" "is synonym of"
      "+part_of" "is part of"
      "+antonym" "is opposite of"
      "+also_see" "related to"
      (clojure.string/replace last "_" " "))))
