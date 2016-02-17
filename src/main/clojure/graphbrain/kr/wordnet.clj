;   Copyright (c) 2016 CNRS - Centre national de la recherche scientifique.
;   All rights reserved.
;
;   Written by Telmo Menezes <telmo@telmomenezes.com>
;
;   This file is part of GraphBrain.
;
;   GraphBrain is free software: you can redistribute it and/or modify
;   it under the terms of the GNU Affero General Public License as published by
;   the Free Software Foundation, either version 3 of the License, or
;   (at your option) any later version.
;
;   GraphBrain is distributed in the hope that it will be useful,
;   but WITHOUT ANY WARRANTY; without even the implied warranty of
;   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;   GNU Affero General Public License for more details.
;
;   You should have received a copy of the GNU Affero General Public License
;   along with GraphBrain.  If not, see <http://www.gnu.org/licenses/>.

(ns graphbrain.kr.wordnet
  (:require [graphbrain.hg.beliefs :as beliefs]
            [graphbrain.hg.symbol :as sym]
            [graphbrain.hg.constants :as const])
  (:import (org.w3c.dom ElementTraversal)
           (net.sf.extjwnl.data PointerUtils
                                POS)
           (net.sf.extjwnl.data.list PointerTargetNode
                                     PointerTargetNodeList)
           (net.sf.extjwnl.dictionary Dictionary)
           (java.io FileInputStream)
           (java.security NoSuchAlgorithmException)))

(defn- add-fact!
  [hg fact]
  (println (str "fact: " fact))
  (beliefs/add! hg const/wordnet fact))

(defn- super-types
  [word]
  (let [concept (.getSynset word)
        hypernyms (PointerUtils/getDirectHypernyms concept)]
    (if (not (empty? hypernyms))
        (let [hypernym (.getFirst hypernyms)]
          (if hypernym
            (.getWords (.getSynset hypernym)))))))

(declare word->symb)

(defn- word->symb-raw
  [word]
  (let [symb (sym/str->symbol (.getLemma word))
        sts (super-types word)
        stids (if sts (map #(word->symb %) sts))
        classes (if sts stids)
        hash (sym/hashed (str symb " " (clojure.string/join " " stids)))]
    (str symb "/" hash)))

(def word->symb (memoize word->symb-raw))

(defn- process-super-types!
  [hg symb word]
  (let [concept (.getSynset word)
        hypernyms (PointerUtils/getDirectHypernyms concept)]
    (doseq [hypernym hypernyms]
      (let [super-word (first (.getWords (.getSynset hypernym)))
            super-id (word->symb super-word)
            fact [const/type-of symb super-id]]
        (add-fact! hg fact)))))

(defn- process-synonyms!
  [hg synset]
  (let [word-list (.getWords synset)
        main-word (nth word-list 0)
        symb (word->symb main-word)]
    (doseq [syn word-list]
      (let [syn-id (word->symb syn)
            fact [const/synonym symb syn-id]]
        (if (not (= symb syn-id))
                 (add-fact! hg fact))))))

(defn- process-meronyms!
  [hg symb word]
  (let [concept (.getSynset word)
        results (PointerUtils/getMeronyms concept)]
    (doseq [result results]
      (let [part-word (first (.getWords (.getSynset result)))
            part-id (word->symb part-word)
            fact [const/part-of part-id symb]]
        (add-fact! hg fact)))))

(defn- process-antonyms!
  [hg symb word]
  (let [concept (.getSynset word)
        results (PointerUtils/getAntonyms concept)]
    (doseq [result results]
      (let [ant-word (first (.getWords (.getSynset result)))
            ant-id (word->symb ant-word)
            fact [const/antonym symb ant-id]]
        (add-fact! hg fact)))))

(defn- process-also-sees!
  [hg symb word]
  (let [concept (.getSynset word)
        results (PointerUtils/getAlsoSees concept)]
    (doseq [result results]
      (let [also-word (first (.getWords (.getSynset result)))
            also-id (word->symb also-word)
            fact [const/related symb also-id]]
        (add-fact! hg fact)))))

(defn- process-pos!
  [hg symb word]
  (let [pos (.getPOS word)]
    (if pos
      (let [pos-id (cond
                     (.equals pos POS/NOUN) const/noun
                     (.equals pos POS/VERB) const/verb
                     (.equals pos POS/ADJECTIVE) const/adjective
                     (.equals pos POS/ADVERB) const/adverb)
            fact [const/part-of-speech symb pos-id]]
        (add-fact! hg fact)))))

(defn- process-synset!
  [hg synset]
  (process-synonyms! hg synset)
  (let [main-word (first (.getWords synset))
        mwid (word->symb main-word)]
    (process-meronyms! hg mwid main-word)
    (process-antonyms! hg mwid main-word)
    (process-also-sees! hg mwid main-word)
    (let [words (.getWords synset)]
          (doseq [word words]
            (let [symb (word->symb word)]
              (println (str "symbol: " symb))
              (process-super-types! hg symb word)
              (process-pos! hg symb word))))))

(defn- process-pos-synset!
  [hg dictionary pos]
  (let [iter (.getSynsetIterator dictionary pos)]
    (while (.hasNext iter)
      (let [synset (.next iter)]
        (println (str "synset: " synset))
        (process-synset! hg synset)))))

(defn- process!
  [hg dictionary]
  (process-pos-synset! hg dictionary (POS/NOUN))
  (process-pos-synset! hg dictionary (POS/VERB))
  (process-pos-synset! hg dictionary (POS/ADJECTIVE))
  (process-pos-synset! hg dictionary (POS/ADVERB)))

(defn import!
  [hg]
  (let [dictionary (Dictionary/getDefaultResourceInstance)]
    (process! hg dictionary)))
