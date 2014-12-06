(ns graphbrain.disambig.entityguesser
  (:require [graphbrain.db.gbdb :as gbdb]
            [graphbrain.db.id :as id]
            [graphbrain.db.maps :as maps]
            [graphbrain.db.entity :as ent]))

(defn substring?
  [sub st]
  (not= (.indexOf st sub) -1))

(defn can-mean
  [gbdb name ctxts]
  (let [base-id (id/sanitize name)
        can-mean (gbdb/pattern->edges gbdb ["r/*can_mean" base-id "*"] ctxts)]
    (map #(gbdb/getv gbdb
                     (id/eid->id (nth (maps/ids %) 2))
                     ctxts)
         can-mean)))

(defn vertex->words
  [gbdb vertex ctxts]
  (let [edges (gbdb/id->edges gbdb (:id vertex) ctxts)
        verts (map maps/participant-ids edges)
        verts (flatten verts)
        verts (set verts)
        verts (map maps/id->vertex verts)
        verts (filter #(= :entity (:type %)) verts)
        ids (map #(vector (:id %) (ent/subentities %)) verts)
        ids (flatten ids)
        ids (filter #(not (nil? %)) ids)
        words (map ent/text ids)
        words (map #(clojure.string/split % #" ") words)
        words (flatten words)
        words (set words)]
    words))

(defn- text-word-score
  [text word]
  (if (substring? word text) 1.0 0.0))

(defn text-score
  [gbdb text vertex ctxts]
  (let [words (vertex->words gbdb vertex ctxts)]
    (apply + (map #(text-word-score text %) words))))

(defn guess
  [gbdb name text eid ctxts]
  (if (> (id/count-parts name) 1)
    (gbdb/getv gbdb name ctxts)
    (let [text (clojure.string/lower-case text)
         can-mean (can-mean gbdb name ctxts)]
     (if (empty? can-mean)
       (if eid
         (maps/eid->entity eid)
         (maps/id->vertex (id/sanitize name)))
       (let [scored (map #(hash-map
                           :vertex %
                           :score (text-score gbdb text % ctxts))
                         can-mean)
             max-score (apply max (map :score scored))
             high-scores (filter #(>= (:score %) max-score) scored)]
         (apply max-key :degree (map :vertex high-scores)))))))

(defn guess-eid
  [gbdb name text eid ctxts]
  (let [entity (guess gbdb name text eid ctxts)]
    (id/local->global (:eid entity))))
