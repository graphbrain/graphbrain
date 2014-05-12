(ns graphbrain.db.id
  (:require [graphbrain.db.edgeparser :as edgeparser]))

(defn hashed
  [str]
  (let [h (loop [s str
                 x 1125899906842597]  ;; prime
            (if (empty? s) x
              (recur (rest s) (unchecked-multiply 31 (+ x (long (first s)))))))]
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
        "u" (cond (= nparts 1) :entity
                  (= nparts 2) :user
                  (= (nth parts 2) "h") :url
                  (= (nth parts 2) "r") :edge-type
                  (= (nth parts 2) "n") (cond (<= nparts 4) :entity
                                              (= (nth parts 3) "r") :edge-type
                                              :else :entity)
                  :else :entity)
        "c" (cond (= nparts 1) :entity
                  (= nparts 2) :context
                  (= (nth parts 2) "h") :url
                  (= (nth parts 2) "r") :edge-type
                  (= (nth parts 2) "n") (cond (<= nparts 4) :entity
                                              (= (nth parts 3) "r") :edge-type
                                              :else :entity)
                  :else :entity)
        "r" (if (= nparts 1) :entity :edge-type)
        "n" (cond (<= nparts 2) :entity
                  (= (second parts) "r") :edge-type
                  :else :entity)
        "h" (if (= nparts 1) :entity :url)
        "p" (if (= nparts 1) :entity :prog)
        "t" (if (= nparts 1) :entity :text)
        :entity))))

(defn type?
  [id tp]
  (if (= (id->type id) tp) id))

(defn edge?
  [id]
  (type? id :edge))

(defn user?
  [id]
  (type? id :user))

(defn context?
  [id]
  (type? id :context))

(defn id->ids
  [id]
  (let [s (subs id 1 (dec (count id)))]
    (edgeparser/split-edge s)))

(defn ids->id
  [ids]
  (str "(" (clojure.string/join " " ids) ")"))

(defn- space-in-set?
  [id spaces-set]
  (if (edge? id)
    (let [ids (id->ids id)]
      (some #(space-in-set? % spaces-set) ids))
    (let [p (parts id)]
      (and (spaces-set (first p)) (> (count p) 2)))))

(defn user-space?
  [id]
  (space-in-set? id #{"u"}))

(defn context-space?
  [id]
  (space-in-set? id #{"c"}))

(defn local-space?
  [id]
  (space-in-set? id #{"u" "c"}))

(defn global-space?
  [id]
  (not (local-space? id)))

(defn last-part
  [id]
  (let [p (parts id)]
    (if (= (id->type id) :url)
      (let [start (if (user-space? id) 3 1)]
        (clojure.string/join "/" (drop start p)))
      (last p))))

(defn username->id
  [username]
  (str "u/" (clojure.string/replace username " " "_")))

(defn sanitize
  [str]
  (clojure.string/replace
   (clojure.string/replace (.toLowerCase str) "/" "_") " " "_"))

(defn global->local
  [id owner]
  (cond
   (edge? id) (ids->id (map #(global->local % owner) (id->ids id)))
   (local-space? id) id
   (user? id) id
   (context? id) id
   :else (str owner "/" id)))

(defn local->global
  [id]
  (cond
   (edge? id) (ids->id (map local->global (id->ids id)))
   (local-space? id) (build-id (drop 2 (parts id)))
   :else id))

(defn owner
  [id]
  (if (global-space? id) nil
    (if (edge? id)
      (owner (first (id->ids id)))
      (build-id (take 2 (parts id))))))

(defn owner-user
  [id]
  (user? (owner id)))

(defn owner-context
  [id]
  (context? (owner id)))
