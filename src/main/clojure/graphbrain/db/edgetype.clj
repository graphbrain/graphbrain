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
  (= (first (id/parts (id/user->global id))) "neg"))

(defn negate
  [id]
  (if (negative? id) id
      (let [user-id (id/owner-id id)
            global-id (id/user->global id)
            neg-id (str "neg/" global-id)]
        (if (empty? user-id)
          neg-id
          (id/global->user neg-id user-id)))))

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
