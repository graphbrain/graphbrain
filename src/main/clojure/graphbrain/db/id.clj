(ns graphbrain.db.id
  (:require [graphbrain.db.edgeparser :as edgeparser])
  (:import (com.graphbrain.utils RandUtils)))

(defn hashed
  [str]
  (let [h (loop [s str
                 x 1125899906842597]  ;; prime
            (if (empty? s) x
              (recur (rest s) (unchecked-multiply 31 (+ x (long (first s)))))))]
    (Long/toHexString h)))

(defn random-hash
  []
  (hashed
   (.toString
    (BigInteger. 130 (RandUtils/secRand)) 32)))

(defn- count-end-slashes
  [str]
  (loop [s (reverse str)
         c 0]
    (if (= (first s) \/)
      (recur (rest s) (inc c))
      c)))

(defn parts
  [id]
  (let [ps (clojure.string/split id #"/")
        c (count-end-slashes id)]
    (if (> c 0)
      (apply conj ps (repeat c ""))
      ps)))

(defn count-parts
  [id]
  (count (parts id)))

(defn build
  [parts]
  (clojure.string/join "/" parts))

(defn id->type
  [id]
  (if id
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
                    (= (nth parts 2) "t") :text
                    :else :entity)
          "c" (cond (= nparts 1) :entity
                    (= nparts 2) :context
                    (= (nth parts 2) "h") :url
                    (= (nth parts 2) "r") :edge-type
                    (= (nth parts 2) "n") (cond (<= nparts 4) :entity
                                                (= (nth parts 3) "r") :edge-type
                                                :else :entity)
                    (= (nth parts 2) "t") :text
                    :else :entity)
          "k" (cond (= nparts 1) :entity
                    (= nparts 2) :entity
                    (= nparts 3) :context
                    (= (nth parts 3) "h") :url
                    (= (nth parts 3) "r") :edge-type
                    (= (nth parts 3) "n") (cond (<= nparts 5) :entity
                                                (= (nth parts 4) "r") :edge-type
                                                :else :entity)
                    (= (nth parts 2) "t") :text
                    :else :entity)
          "r" (if (= nparts 1) :entity :edge-type)
          "n" (cond (<= nparts 2) :entity
                    (= (second parts) "r") :edge-type
                    :else :entity)
          "h" (if (= nparts 1) :entity :url)
          "p" (if (= nparts 1) :entity :prog)
          "t" (if (= nparts 1) :entity :text)
          :entity)))
    :unknown))

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
  (if (= (first id) \()
    (let [s (subs id 1 (dec (count id)))]
     (edgeparser/split-edge s))))

(defn ids->id
  [ids]
  (str "(" (clojure.string/join " " ids) ")"))

(defn- space-length
  [space]
  (if (= space "k") 3 2))

(defn- space-in-set?
  [id spaces-set]
  (if (edge? id)
    (let [ids (id->ids id)]
      (some #(space-in-set? % spaces-set) ids))
    (let [p (parts id)
          space (first p)]
      (and (spaces-set space) (> (count p) (space-length space))))))

(defn user-space?
  [id]
  (space-in-set? id #{"u"}))

(defn context-space?
  [id]
  (space-in-set? id #{"c" "k"}))

(defn local-space?
  [id]
  (or (user-space? id) (context-space? id)))

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


;; global / local

(defn global->local
  [id ctxt]
  (cond
   (edge? id) (ids->id (map #(global->local % ctxt) (id->ids id)))
   (user? id) id
   (context? id) id
   (local-space? id) id
   :else (str ctxt "/" id)))

(defn local->global
  [id]
  (cond
   (edge? id) (ids->id (map local->global (id->ids id)))
   (user? id) id
   (context? id) id
   (local-space? id) (let [p (parts id)
                           space (first p)]
                       (build (drop (space-length space) (parts id))))
   :else id))


;; edges

(defn negative?
  [id]
  (= (first
      (parts
       (local->global id)))
     "n"))

(defn positive?
  [id]
  (not (negative? id)))

(defn edge-rel
  [eid]
  (if (= (id->type eid) :edge)
    (-> eid
        id->ids
        first
        local->global)))


;; context

(defn context
  [id]
  (if (user? id) id
      (if (global-space? id) nil
          (if (edge? id)
            (context (first (id->ids id)))
            (let [pars (parts id)
                  space (first pars)]
              (build (take (space-length space) pars)))))))


;; eids

(defn eid?
  [id]
  (if (= (id->type id) :edge)
    (let [ids (id->ids id)
          rel (local->global (first ids))]
      (= (first (second (parts rel))) \+))))

(defn eid->id
  [eid]
  (if (eid? eid)
    (let [ctxt (context eid)
          geid (local->global eid)
          hsh (hashed geid)
          ids (id->ids geid)
          name (second ids)
          id (build [hsh name])
          id (if ctxt (global->local id ctxt) id)]
      id)
    eid))

(defn eid->name
  [eid]
  (if (eid? eid)
    (-> eid
        local->global
        id->ids
        second)))

(defn name+ids->eid
  [rel name ids]
  (let [base-id (sanitize name)]
    (if (empty? ids)
      base-id
      (str "(" rel " " base-id " "
           (clojure.string/join " " ids) ")"))))

(defn new-meaning
  [id ctxt]
  (if ctxt
    (let [name (if (eid? id)
                 (eid->name id)
                 (last-part id))]
      (name+ids->eid "r/+temp" name [ctxt]))
    id))

(defn undefined-eid?
  [eid]
  (= (edge-rel eid) "r/+temp"))
