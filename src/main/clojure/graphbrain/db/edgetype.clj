(ns graphbrain.db.edgetype
  (:require [graphbrain.db.id :as id]))

(defn build-id
  [text]
  (str "r/" (id/sanitize text)))

(defn label
  [id]
  (let [last (id/last-part id)]
    (case last
      "*pos" "is"
      "*can_mean" "can mean"
      "*type_of" "is type of"
      "*synonym" "is synonym of"
      "*part_of" "is part of"
      "*antonym" "is opposite of"
      "*also_see" "is related to"
      "*example" "has example"
      (clojure.string/replace last "_" " "))))
