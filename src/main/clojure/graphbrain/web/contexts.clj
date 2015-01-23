(ns graphbrain.web.contexts
  (:require [graphbrain.db.id :as id]
            [graphbrain.db.context :as context]
            [graphbrain.db.perms :as perms]
            [graphbrain.web.colors :as colors]
            [graphbrain.web.common :as common]))

(defn- admin?
  [id user-id ctxt-id]
  (and (= id ctxt-id)
       (perms/is-admin? common/gbdb
                        user-id
                        ctxt-id)))

(defn- follow-unfollow?
  [id ctxt-id admin]
  (and (not admin)
       (= id ctxt-id)))

(defn context-data
  [id user-id]
  (let [ctxt-id (id/context id)
        ctxt-id (if ctxt-id ctxt-id user-id)
        name (if (= ctxt-id user-id)
               "Personal"
               (context/label ctxt-id))
        admin (admin? id user-id ctxt-id)
        follow-unfollow (follow-unfollow? id ctxt-id admin)]
    {:id ctxt-id
     :name name
     :admin admin
     :follow-unfollow follow-unfollow
     :following (if follow-unfollow
                  (perms/is-following? common/gbdb user-id ctxt-id))}))

(defn active-ctxts
  [id user]
  (let [ctxt (id/context id)
        user-id (:id user)
        ctxts ["c/wordnet" ctxt]
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
    (nth colors/colors
         (mod (Math/abs (.hashCode ctxt-id)) (count colors/colors)))))

(defn contexts-map
  [ctxts user-id]
  (zipmap ctxts
          (map #(hash-map
                 :name (context/label %)
                 :color (color % user-id))
               ctxts)))
