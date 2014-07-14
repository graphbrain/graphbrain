(ns graphbrain.braingenerators.wordnet
  (:require [graphbrain.db.gbdb :as gb]
            [graphbrain.db.id :as id]
            [graphbrain.db.maps :as maps]
            [graphbrain.db.text :as text]
            [graphbrain.db.constants :as consts])
  (:import (org.w3c.dom ElementTraversal)
           (net.sf.extjwnl.data PointerUtils
                                POS)
           (net.sf.extjwnl.data.list PointerTargetNode
                                     PointerTargetNodeList)
           (net.sf.extjwnl.dictionary Dictionary)
           (java.io FileInputStream)
           (java.security NoSuchAlgorithmException)))

(def dryrun false)

(defn add-relation!
  [gbdb rel]
  (prn (str "rel: " rel))
  (if (not dryrun) (gb/putv! gbdb (maps/id->edge rel) "c/wordnet")))

(defn super-types
  [word]
  (let [concept (.getSynset word)
        hypernyms (PointerUtils/getDirectHypernyms concept)]
    (if (not (empty? hypernyms))
        (let [hypernym (.getFirst hypernyms)]
          (if hypernym
            (.getWords (.getSynset hypernym)))))))

(defn example
  [word]
  (let [synset (.getSynset word)
        example (.getGloss synset)]
    (text/text->vertex example)))

(declare vertex-id)

(defn vertex-id-raw
  [word]
  (let [name (.getLemma word)
        sts (super-types word)
        stids (if sts (map #(id/eid->id (vertex-id %)) sts))
        classes (if sts stids)]
    (id/name+ids->eid consts/type-eid-rel name classes)))

(def vertex-id (memoize vertex-id-raw))

(defn- major-form-class-id
  [gbdb dictionary]
  (id/eid->id
     (vertex-id (.get
                  (.getWords
                   (.get
                    (.getSenses
                     (. dictionary getIndexWord POS/NOUN "major form class")) 0)) 0))))

(defn- set-globals!
  [gbdb dictionary]
  (let [mfc (major-form-class-id gbdb dictionary)]
    (def noun (str "(r/+t noun " mfc ")"))
    (def verb (str "(r/+t verb " mfc ")"))
    (def adjective (str "(r/+t adjective " mfc ")"))
    (def adverb (str "(r/+t adverb " mfc ")"))))

(defn process-super-types!
  [gbdb vid word]
  (let [concept (.getSynset word)
        hypernyms (PointerUtils/getDirectHypernyms concept)]
    (doseq [hypernym hypernyms]
      (let [super-word (first (.getWords (.getSynset hypernym)))
            super-id (vertex-id super-word)
            rel (str "(r/*type_of " vid " " super-id ")")]
        (add-relation! gbdb rel)))))

(defn process-synonyms!
  [gbdb synset]
  (let [word-list (.getWords synset)
        main-word (nth word-list 0)
        vid (vertex-id main-word)]
    (doseq [syn word-list]
      (let [syn-id (vertex-id syn)
            rel (str "(r/*synonym " vid " " syn-id ")")]
        (if (not (= vid syn-id))
                 (add-relation! gbdb rel))))))

(defn process-meronyms!
  [gbdb vid word]
  (let [concept (.getSynset word)
        results (PointerUtils/getMeronyms concept)]
    (doseq [result results]
      (let [part-word (first (.getWords (.getSynset result)))
            part-id (vertex-id part-word)
            rel (str "(r/*part_of " part-id " " vid ")")]
        (add-relation! gbdb rel)))))

(defn process-antonyms!
  [gbdb vid word]
  (let [concept (.getSynset word)
        results (PointerUtils/getAntonyms concept)]
    (doseq [result results]
      (let [ant-word (first (.getWords (.getSynset result)))
            ant-id (vertex-id ant-word)
            rel (str "(r/*antonym " vid " " ant-id ")")]
        (add-relation! gbdb rel)))))

(defn process-also-sees!
  [gbdb vid word]
  (let [concept (.getSynset word)
        results (PointerUtils/getAlsoSees concept)]
    (doseq [result results]
      (let [also-word (first (.getWords (.getSynset result)))
            also-id (vertex-id also-word)
            rel (str "(r/*also_see " vid " " also-id ")")]
        (add-relation! gbdb rel)))))

(defn process-pos!
  [gbdb vid word]
  (let [pos (.getPOS word)]
    (if pos
      (let [pos-id (cond
                     (.equals pos POS/NOUN) noun
                     (.equals pos POS/VERB) verb
                     (.equals pos POS/ADJECTIVE) adjective
                     (.equals pos POS/ADVERB) adverb)
            rel (str "(r/*pos " vid " " pos-id ")")]
        (add-relation! gbdb rel)))))

(defn process-example!
  [gbdb vid word]
  (let [tn (example word)]
    (if (not dryrun) (gb/putv! gbdb tn "c/wordnet"))
    (let [rel (str "(r/*example " vid " " (:id tn) ")")]
      (add-relation! gbdb rel))))

(defn process-synset!
  [gbdb synset]
  (process-synonyms! gbdb synset)
  (let [main-word (first (.getWords synset))
        mwid (vertex-id main-word)]
    (process-meronyms! gbdb mwid main-word)
    (process-antonyms! gbdb mwid main-word)
    (process-also-sees! gbdb mwid main-word)
    (process-example! gbdb mwid main-word)
    (let [words (.getWords synset)]
          (doseq [word words]
            (let [vid (vertex-id word)]
              (prn vid)
              (process-super-types! gbdb vid word)
              (process-pos! gbdb vid word))))))

(defn process-pos-synset!
  [gbdb dictionary pos]
  (let [iter (.getSynsetIterator dictionary pos)]
    (while (.hasNext iter)
      (let [synset (.next iter)]
        (prn synset)
        (process-synset! gbdb synset)))))

(defn process!
  [gbdb dictionary]
  (process-pos-synset! gbdb dictionary (POS/NOUN))
  (process-pos-synset! gbdb dictionary (POS/VERB))
  (process-pos-synset! gbdb dictionary (POS/ADJECTIVE))
  (process-pos-synset! gbdb dictionary (POS/ADVERB)))

(defn run!
  []
  (let [dictionary (Dictionary/getDefaultResourceInstance)
        gbdb (gb/gbdb)]
    (set-globals! gbdb dictionary)
    (process! gbdb dictionary)))
