(ns graphbrain.braingenerators.emmanuel
  (:require [clojure-csv.core :as csv]
            [clojure.math.combinatorics :as comb]
            [graphbrain.db.gbdb :as gb]
            [graphbrain.db.id :as id]
            [graphbrain.db.maps :as maps]
            [graphbrain.db.text :as text]
            [graphbrain.db.constants :as consts]))

(def g (gb/gbdb))

(def ctxt "k/emmanuel/psychosis")

(defn- take-csv
  [fname]
  (with-open [file (clojure.java.io/reader fname)]
    (csv/parse-csv (slurp file))))

(defn- take-data
  [filename]
  (take-csv filename))

(defn- author-list
  [authors]
  (map clojure.string/trim
       (clojure.string/split authors #"\x1D")))

(defn- row->map
  [row]
  {:year (clojure.string/trim (nth row 12))
   :authors (author-list (nth row 13))
   :title (clojure.string/trim (nth row 22))
   :abstract (clojure.string/trim (nth row 15))
   :journal (clojure.string/trim (nth row 18))})

(defn- csv->map
  [filename]
  (let [data (take-data filename)]
    (filter #(not (empty? (:authors %))) (map row->map data))))

(defn- researcher-node
  [name]
  (maps/eid->entity
   (id/name+ids->eid consts/type-eid-rel name ["645112e583987a2c/researcher"])))

(defn- article-node
  [title]
  (maps/eid->entity
   (id/name+ids->eid consts/type-eid-rel title ["47e9752fc47633dc/article"])))

(defn- journal-node
  [name]
  (maps/eid->entity
   (id/name+ids->eid consts/type-eid-rel name ["8d921bed0d349259/journal"])))

(defn- row-text->nodes
  [row]
  {:authors (map researcher-node (:authors row))
   :article (article-node (:title row))
   :abstract (if (not (empty? (:abstract row))) (text/text->vertex (:abstract row)))
   :journal (journal-node (:journal row))})

(defn- map-text->nodes
  [data]
  (map row-text->nodes data))

(defn- process-author-pair!
  [pair]
  (let [edge-id1 (id/ids->id ["r/has_coauthor" (first pair) (second pair)])
        edge-id2 (id/ids->id ["r/has_coauthor" (second pair) (first pair)])]
    (prn edge-id1)
    (gb/putv! g (maps/id->edge edge-id1) ctxt)
    (gb/putv! g (maps/id->edge edge-id2) ctxt)))

(defn- process-coauthors!
  [authors]
  (let [eids (map :eid authors)
        pairs (comb/combinations eids 2)]
    (doseq [pair pairs]
      (process-author-pair! pair))))

(defn- process-author!
  [art-eid auth-eid]
  (let [edge-id (id/ids->id ["r/has_author" art-eid auth-eid])]
    (prn edge-id)
    (gb/putv! g (maps/id->edge edge-id) ctxt)))

(defn- process-authors!
  [article authors]
  (if (not (nil? article))
    (let [art-eid (:eid article)
         auth-eids (map :eid authors)]
     (doseq [auth-eid auth-eids]
       (process-author! art-eid auth-eid)))))

(defn- process-abstract!
  [article abstract]
  (if (not (nil? abstract))
    (let [art-eid (:eid article)
         abs-eid (:eid abstract)
         edge-id (id/ids->id ["r/has_abstract" art-eid abs-eid])]
     (gb/putv! g abstract ctxt)
     (prn edge-id)
     (gb/putv! g (maps/id->edge edge-id) ctxt))))

(defn- process-journal!
  [journal article]
  (if (and (not (nil? journal)) (not (nil? article)))
    (let [art-eid (:eid article)
         journ-eid (:eid journal)
         edge-id (id/ids->id ["r/has_article" journ-eid art-eid])]
     (prn edge-id)
     (gb/putv! g (maps/id->edge edge-id) ctxt))))

(defn- process-row!
  [row]
  (process-coauthors! (:authors row))
  (process-authors! (:article row) (:authors row))
  (process-abstract! (:article row) (:abstract row))
  (process-journal! (:journal row) (:article row)))

(defn run!
  [filename]
  (prn "running...")
  (let [rows (csv->map filename)
        rows (map-text->nodes rows)]
    (doseq [row rows]
      (process-row! row)))
  (prn "done."))
