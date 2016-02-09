(ns graphbrain.kr.wordnet
  (:require [graphbrain.db.gbdb :as gb]
            [graphbrain.db.id :as id]
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
  (if (not dryrun) (gb/add! gbdb rel)))

(defn super-types
  [word]
  (let [concept (.getSynset word)
        hypernyms (PointerUtils/getDirectHypernyms concept)]
    (if (not (empty? hypernyms))
        (let [hypernym (.getFirst hypernyms)]
          (if hypernym
            (.getWords (.getSynset hypernym)))))))

(declare vertex-id)

(defn vertex-id-raw
  [word]
  (let [name (.getLemma word)
        sts (super-types word)
        stids (if sts (map #(vertex-id %) sts))
        classes (if sts stids)
        hash (id/hashed (str name " " (clojure.string/join " " stids)))]
    (str name "/" hash)))

(def vertex-id (memoize vertex-id-raw))

(defn- set-globals!
  [gbdb]
  (def noun "noun/1")
  (def verb "verb/1")
  (def adjective "adjective/1")
  (def adverb "adverb/1"))

(defn process-super-types!
  [gbdb vid word]
  (let [concept (.getSynset word)
        hypernyms (PointerUtils/getDirectHypernyms concept)]
    (doseq [hypernym hypernyms]
      (let [super-word (first (.getWords (.getSynset hypernym)))
            super-id (vertex-id super-word)
            rel ["type_of/1" vid super-id]]
        (add-relation! gbdb rel)))))

(defn process-synonyms!
  [gbdb synset]
  (let [word-list (.getWords synset)
        main-word (nth word-list 0)
        vid (vertex-id main-word)]
    (doseq [syn word-list]
      (let [syn-id (vertex-id syn)
            rel ["synonym/1" vid syn-id]]
        (if (not (= vid syn-id))
                 (add-relation! gbdb rel))))))

(defn process-meronyms!
  [gbdb vid word]
  (let [concept (.getSynset word)
        results (PointerUtils/getMeronyms concept)]
    (doseq [result results]
      (let [part-word (first (.getWords (.getSynset result)))
            part-id (vertex-id part-word)
            rel ["part_of/1" part-id vid]]
        (add-relation! gbdb rel)))))

(defn process-antonyms!
  [gbdb vid word]
  (let [concept (.getSynset word)
        results (PointerUtils/getAntonyms concept)]
    (doseq [result results]
      (let [ant-word (first (.getWords (.getSynset result)))
            ant-id (vertex-id ant-word)
            rel ["antonym/1" vid ant-id]]
        (add-relation! gbdb rel)))))

(defn process-also-sees!
  [gbdb vid word]
  (let [concept (.getSynset word)
        results (PointerUtils/getAlsoSees concept)]
    (doseq [result results]
      (let [also-word (first (.getWords (.getSynset result)))
            also-id (vertex-id also-word)
            rel ["also_see/1" vid also-id]]
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
            rel ["part-of-speech/1" vid pos-id]]
        (add-relation! gbdb rel)))))

(defn process-synset!
  [gbdb synset]
  (process-synonyms! gbdb synset)
  (let [main-word (first (.getWords synset))
        mwid (vertex-id main-word)]
    (process-meronyms! gbdb mwid main-word)
    (process-antonyms! gbdb mwid main-word)
    (process-also-sees! gbdb mwid main-word)
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

(defn import!
  []
  (let [dictionary (Dictionary/getDefaultResourceInstance)
        gbdb (gb/gbdb)]
    (set-globals! gbdb dictionary)
    (process! gbdb dictionary)))
