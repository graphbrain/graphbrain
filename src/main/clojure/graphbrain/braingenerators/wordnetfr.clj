(ns graphbrain.braingenerators.wordnetfr
  (:require [clojure.xml :as xml]
            [graphbrain.db.gbdb :as gb]
            [graphbrain.db.id :as id]
            [graphbrain.db.maps :as maps]
            [graphbrain.db.text :as text]
            [graphbrain.db.constants :as consts]))

(defn parse-synonym-part
  [l item]
  (case (:tag item)
    :LITERAL (conj l (first (:content item)))
    l))

(defn parse-synonym
  [dict item]
  (assoc dict
    :synonyms
    (reduce parse-synonym-part (:synonyms dict) (:content item))))

(defn parse-ilr
  [dict item]
  (case (:type (:attrs item))
    "hypernym" (assoc dict :hypernyms
                      (conj (:hypernyms dict) (first (:content item))))
    dict))

(defn parse-ss-item
  [dict item]
  ;;(prn item)
  (case (:tag item)
    :ID (assoc dict :id (first (:content item)))
    :ILR (parse-ilr dict item)
    :SYNONYM (parse-synonym dict item)
    dict))

(defn parse-synset
  [ss]
  (if (= (:tag ss) :SYNSET)
    (reduce parse-ss-item {} (:content ss))
    ss))

(defn wn->synlist
  [wn]
  (map parse-synset
       (:content wn)))

(defn synlist->synmap
  [sl]
  (reduce #(assoc %1 (:id %2) %2) {} sl))

(defn add-relation!
  [gbdb rel]
  (prn (str "rel: " rel))
  #_(gb/putv! gbdb (maps/id->edge rel) "c/wordnet"))

(defn super-types
  [sm syn]
  (map sm (:hypernyms syn)))

#_(declare vertex-id)

(defn vertex-id
  [sm syn word]
  (prn word)
  (let [sts (super-types sm syn)
        stids (if sts (map #(id/eid->id (vertex-id sm % (first (:synonyms %)))) sts))
        classes (if sts stids)]
    (id/name+ids->eid consts/type-eid-rel word classes)))

#_(def vertex-id (memoize vertex-id-raw))

(defn process-super-types!
  [gbdb sm vid syn]
  (let [hypernyms (:hypernyms syn)]
    (doseq [hypernym hypernyms]
      (let [super-syn (sm hypernym)
            super-word (first (:synonyms super-syn))
            super-id (vertex-id sm super-syn super-word)
            rel (str "(r/*type_of " vid " " super-id ")")]
        (add-relation! gbdb rel)))))

(defn process-synonyms!
  [gbdb sm syn]
  (let [word-list (:synonyms syn)
        main-word (first word-list)
        vid (vertex-id sm syn main-word)]
    (doseq [word word-list]
      (let [syn-id (vertex-id sm syn word)
            rel (str "(r/*synonym " vid " " syn-id ")")]
        (if (not (= vid syn-id))
          (add-relation! gbdb rel))))))

(defn process-synset!
  [gbdb sm syn]
  (process-synonyms! gbdb sm syn)
  (let [words (:synonyms syn)
        main-word (first words)
        mwid (vertex-id sm syn main-word)]
    (doseq [word words]
      (let [vid (vertex-id sm syn word)]
        (prn vid)
        (process-super-types! gbdb sm vid syn)))))

(defn run!
  []
  (let [;;data (xml/parse "/Users/telmo/wordnet/wonef-fscore-0.1.xml")
        wn (xml/parse "/Users/telmo/wordnet/wolf-1.0b4.xml")
        gbdb (gb/gbdb)
        sl (wn->synlist wn)
        sm (synlist->synmap sl)]
    (doseq [syn sl]
      (process-synset! gbdb sm syn))))
