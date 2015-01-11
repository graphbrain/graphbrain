(ns graphbrain.web.contexts
  (:require [graphbrain.db.id :as id]
            [graphbrain.db.context :as context]
            [graphbrain.web.colors :as colors]))

(defn context-data
  [id user-id]
  (let [ctxt (id/context id)
        ctxt (if ctxt ctxt user-id)
        name (if (= ctxt user-id)
               "Personal"
               (context/label ctxt))]
    {:id ctxt
     :name name}))

(defn active-ctxts
  [id user]
  (let [ctxt (id/context id)
        user-id (:id user)
        ctxts ["c/wordnet" "c/web" ctxt]
        ctxts (if user
                (conj ctxts user-id)
                ctxts)]
    (into #{}
          (filter #(not (nil? %)) ctxts))))

(defn color
  [ctxt-id user-id]
  (condp = ctxt-id
    "c/wordnet" "#332288"
    "c/web" "#88CCEE"
    user-id "#117733"
    (nth colors/colors (mod (Math/abs (.hashCode ctxt-id)) (count colors/colors)))))
