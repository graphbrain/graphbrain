(ns graphbrain.braingenerators.wordnet
  (:require [graphbrain.db.graph :as gb]
            [graphbrain.db.id :as id]
            [graphbrain.db.edge :as edge]
            [graphbrain.db.text :as text])
  (:import (org.w3c.dom ElementTraversal)
           (net.sf.extjwnl.data PointerUtils
                                POS)
           (net.sf.extjwnl.data.list PointerTargetNode
                                     PointerTargetNodeList)
           (net.sf.extjwnl.dictionary Dictionary)
           (java.io FileInputStream)
           (java.security NoSuchAlgorithmException)))

(def dryrun true)

(defn add-relation!
  [graph rel]
  (prn rel)
  (if (not dryrun) (gb/putv! graph (edge/id->edge rel) "wordnet")))

(defn super-type
  [word]
  (let [concept (.getSynset word)
        hypernyms (PointerUtils/getDirectHypernyms concept)]
    (if (not (empty? hypernyms))
        (let [hypernym (.getFirst hypernyms)]
          (if hypernym
            (first (.getWords (.getSynset hypernym))))))))

(defn example
  [word]
  (let [synset (.getSynset word)
        example (.getGloss synset)]
    (text/text->vertex example)))

(defn vertex-id
  [word]
  (let [id (id/sanitize (.getLemma word))]
    (if (= id "entity") id
        (let [st (super-type word)
              rel (if st
                    (str "(r/+type_of " id " " (vertex-id st) ")")
                    (let [tn (example word)]
                      (str "(r/+example " id " " (:id tn) ")")))]
          (str (id/hashed rel) "/" id)))))

(defn process-super-types!
  [graph vid word]
  (let [concept (.getSynset word)
        hypernyms (PointerUtils/getDirectHypernyms concept)]
    (doseq [hypernym hypernyms]
      (let [super-word (first (.getWords (.getSynset hypernym)))
            super-id (vertex-id super-word)
            rel (str "(r/+type_of " vid " " super-id ")")]
        (add-relation! graph rel)))))

(defn process-synonyms!
  [graph synset]
  (let [word-list (.getWords synset)
        main-word (nth word-list 0)
        vid (vertex-id main-word)]
    (doseq [syn word-list]
      (let [syn-id (vertex-id syn)
            rel (str "(r/+synonym " vid " " syn-id ")")]
        (if (not (= vid syn-id))
                 (add-relation! graph rel))))))

(defn process-meronyms!
  [graph vid word]
  (let [concept (.getSynset word)
        results (PointerUtils/getMeronyms concept)]
    (doseq [result results]
      (let [part-word (first (.getWords (.getSynset result)))
            part-id (vertex-id part-word)
            rel (str "(r/+part_of " part-id " " vid ")")]
        (add-relation! graph rel)))))

(defn process-antonyms!
  [graph vid word]
  (let [concept (.getSynset word)
        results (PointerUtils/getAntonyms concept)]
    (doseq [result results]
      (let [ant-word (first (.getWords (.getSynset result)))
            ant-id (vertex-id ant-word)
            rel (str "(r/+antonym " vid " " ant-id ")")]
        (add-relation! graph rel)))))

(defn process-also-sees!
  [graph vid word]
  (let [concept (.getSynset word)
        results (PointerUtils/getAlsoSees concept)]
    (doseq [result results]
      (let [also-word (first (.getWords (.getSynset result)))
            also-id (vertex-id also-word)
            rel (str "(r/+also_see " vid " " also-id ")")]
        (add-relation! graph rel)))))

(defn process-can-mean!
  [graph vid word]
  (if (super-type word)
    (let [sid (id/sanitize (.getLemma word))
          rel (str "(r/+can_mean " sid " " vid ")")]
      (add-relation! graph rel))))

(defn process-pos!
  [graph vid word]
  (let [pos (.getPOS word)]
    (if pos
      (let [pos-id (cond
                     (.equals pos POS/NOUN) "850e2accee28f70e/noun"
                     (.equals pos POS/VERB) "b43b5b40bb0873e9/verb"
                     (.equals pos POS/ADJECTIVE) "90a283c76334fb9d/adjective"
                     (.equals pos POS/ADVERB) "20383f8100e0be26/adverb")
            rel (str "(r/+pos " vid " " pos-id ")")]
        (add-relation! graph rel)))))

(defn process-example!
  [graph vid word]
  (let [tn (example word)]
    (if (not dryrun) (gb/putv! tn))
    (let [rel (str "(r/+example " vid " " (:id tn) ")")]
      (add-relation! graph rel))))

(defn process-synset!
  [graph synset]
  (process-synonyms! graph synset)
  (let [main-word (first (.getWords synset))
        mwid (vertex-id main-word)]
    (process-meronyms! graph mwid main-word)
    (process-antonyms! graph mwid main-word)
    (process-also-sees! graph mwid main-word)
    (process-example! graph mwid main-word)
    (let [words (.getWords synset)]
          (doseq [word words]
            (let [vid (vertex-id word)]
              (prn vid)
              (process-can-mean! graph vid word)
              (process-super-types! graph vid word)
              (process-pos! graph vid word))))))

(defn process-pos-synset!
  [graph dictionary pos]
  (let [iter (.getSynsetIterator dictionary pos)]
    (while (.hasNext iter)
      (let [synset (.next iter)]
        (prn synset)
        (process-synset! graph synset)))))

(defn process!
  [graph dictionary]
  (gb/create-user! graph "wordnet" "wordnet" "" "" "crawler")
  (process-pos-synset! graph dictionary (POS/NOUN))
  (process-pos-synset! graph dictionary (POS/VERB))
  (process-pos-synset! graph dictionary (POS/ADJECTIVE))
  (process-pos-synset! graph dictionary (POS/ADVERB)))

(defn run!
  []
  (let [dictionary (Dictionary/getDefaultResourceInstance)
        graph (gb/graph)]
    (process! graph dictionary)))

(defn -main
  []
  (run!))
