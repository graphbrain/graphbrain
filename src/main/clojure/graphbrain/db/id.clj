(ns graphbrain.db.id
  (:require [graphbrain.db.edgeparser :as edgeparser]))

(defn hashed
  [str]
  (let [h (loop [s str
                 x 1125899906842597]  ;; prime
            (if (empty? s) x
                (recur (rest s) (* 31 (+ x (long (first s)))))))]
    (Long/toHexString h)))

(defn parts
  [id]
  (clojure.string/split id #"/"))

(defn count-parts
  [id]
  (count (parts id)))

(defn build-id
  [parts]
  (clojure.string/join "/" parts))

(defn id->type
  [id]
  (if (some #{\space} (seq id))
    :edge
    (let [parts (parts id)
          nparts (count parts)]
      (case (first parts)
        "user" (cond (= nparts 1) :entity
                     (= nparts 2) :user
                     (= (nth parts 2) "url") :url
                     (= (nth parts 2) "r") :edge-type
                     (= (nth parts 2) "neg") (cond (<= nparts 4) :entity
                                                   (= (nth parts 3) "r") :edge-type
                                                   :else :entity)
                     :else :entity)
        "r" (if (= nparts 1) :entity :edge-type)
        "neg" (cond (<= nparts 2) :entity
                    (= (second parts) "r") :edge-type
                    :else :entity)
        "url" (if (= nparts 1) :entity :url)
        "prog" (if (= nparts 1) :entity :prog)
        "text" (if (= nparts 1) :entity :text)
        :entity))))

(defn edge?
  [id-or-ns]
  (= (first id-or-ns) \())

(defn id->ids
  [id]
  (let [s (subs id 1 (dec (count id)))]
      (edgeparser/split-edge s)))

(defn ids->id
  [ids]
  (str "(" (clojure.string/join " " ids) ")"))

(defn user-space?
  [id-or-ns]
  (if (edge? id-or-ns)
    (let [ids (id->ids id-or-ns)]
      (some user-space? ids))
    (let [p (parts id-or-ns)]
      (and (= (first p) "user") (> (count p) 2)))))

(defn user?
  [id-or-ns]
  (let [p (parts id-or-ns)]
    (and (= (first p) "user") (= (count p) 2))))

(defn global-space?
  [id-or-ns]
  (not (user-space? id-or-ns)))

(defn last-part
  [id]
  (let [p (parts id)]
    (if (= (id->type id) :url)
      (let [start (if (user-space? id) 3 1)]
        (clojure.string/join "/" (drop start p)))
      (last p))))

(defn username->id
  [username]
  (str "user/" (clojure.string/replace username " " "_")))

(defn sanitize
  [str]
  (clojure.string/replace
   (clojure.string/replace (.toLowerCase str) "/" "_") " " "_"))

(defn global->user
  [id-or-ns user-id]
  (cond
   (edge? id-or-ns) (ids->id (map #(global->user % user-id) (id->ids id-or-ns)))
   (user-space? id-or-ns) id-or-ns
   (user? id-or-ns) id-or-ns
   :else (if (and (> (count-parts user-id) 0)
                  (= (first (parts user-id)) "user"))
           (str user-id "/" id-or-ns)
           (str "user/" user-id "/" id-or-ns))))

(defn user->global
  [id-or-ns]
  (cond
   (edge? id-or-ns) (ids->id (map user->global (id->ids id-or-ns)))
   (user-space? id-or-ns) (build-id (drop 2 (parts id-or-ns)))
   :else id-or-ns))

(defn owner-id
  [id-or-ns]
  (let [tokens (parts id-or-ns)]
    (if (= (first tokens) "user")
      (str "user/" (first tokens)) "")))
