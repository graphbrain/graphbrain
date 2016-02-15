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

(ns graphbrain.eco.eco
  (:use [graphbrain.utils.utils :only [dbg]]
        graphbrain.eco.ecofuns)
  (:require [graphbrain.hg.symbol :as sym]
            [graphbrain.eco.words :as words]
            [clojure.set :as set]))

(defn chunk-def->chunk
  [chunk-def]
  {:var (first chunk-def)
   :word-conds (second chunk-def)})

(defmacro ecoparser
  [name]
  `(def ~name []))

(defn funexpand
  [funs]
  (map #(if (string? %) (w %) %) funs))

(defn funvec
  ([x f]
     (f (if (coll? x) x [x])))
  ([x]
     (funvec x funexpand)))

(defn- cond-weight
  [condf]
  (cond (= condf ?) 0
        (= condf ??) 10
        (= condf ???) 100
        (= condf ????) 1000
        (string? condf) 3
        :else 1))

(defn- rule-priority
  [rule]
  (reduce +
          (map
           #(+ (reduce + (map cond-weight
                              (funvec (:word-conds %)
                                      identity)))
               1)
           (:chunks rule))))

(defn sorted-rules
  [rules]
  (sort #(> (:priority %1) (:priority %2))
        (map #(assoc % :priority (rule-priority %)) rules)))

(defmacro pattern
  [typ rules desc chunks f]
  `(def ~rules
     (sorted-rules
      (conj ~rules
            {:chunks
             (let [~'chunk-defs
                   ~(apply vector (map
                                   #(vector (keyword (first %))
                                            (second %))
                                   (partition 2 (destructure chunks))))]
               (reduce
                (fn [~'v ~'cd]
                  (conj ~'v (chunk-def->chunk ~'cd)))
                [] ~'chunk-defs))
             :f ~(list 'fn*
                       (apply vector
                              (conj (sort
                                     (apply vector
                                            (map
                                             first
                                             (partition 2 (destructure chunks)))))
                                    (symbol 'env)
                                    (symbol 'rules)
                                    (symbol 'depth)))
                       f)
             :desc ~(str desc " ["
                         (clojure.string/join
                          "-"
                          (map #(name (first %))
                               (partition 2 (destructure chunks))))
                         "]")
             :typ ~typ}))))

(defn eval-chunk-word
  [chunk word]
  (if (and chunk word)
    (every? #(% word) (funvec (:word-conds chunk)))))

(declare eval-rule)

(defn- add-var-and-parse-more
  [chunk subsent chunks typ sentence env depth]
  (let [parse (eval-rule
               sentence
               (rest chunks)
               typ
               []
               env
               depth)]
    (if parse (map #(assoc % (:var chunk) subsent) parse))))

(defn eval-rule
  [sentence chunks typ subsent env depth]
  (if (or
       (not= typ :top)
       (= depth 0))
    (let [chunk (first chunks)
          word (first sentence)
          
          cur-word-cur-chunk (eval-chunk-word chunk word)
          
          ;; continue chunk
          continue (if cur-word-cur-chunk
                     (eval-rule
                      (rest sentence)
                      chunks
                      typ
                      (conj subsent word)
                      env
                      depth))
        
          ;; end current chunk
          end (if (and (not cur-word-cur-chunk) (not (empty? subsent)))
                (add-var-and-parse-more
                 chunk subsent chunks typ sentence env depth))
        
          ;; fork chunk
          fork (if (and cur-word-cur-chunk
                        (not (empty? subsent))
                        (eval-chunk-word (second chunks) word))
                 (add-var-and-parse-more
                  chunk subsent chunks typ sentence env depth))]
      (if (and (empty? sentence) (empty? chunks))
        [nil]
        (let [res (filter #(not (empty? %)) (into (into continue end) fork))]
          res)))))

(defn- result->vertex
  [rules rule result env depth]
  (let [ks (sort (keys result))
        vals (map #(% result) ks)
        verts (apply (:f rule) (conj vals env rules depth))]
    (if (map? verts)
      (assoc verts :rule rule)
      (map #(assoc % :rule rule) verts))))

(defn parse
  ([rules words env depth]
     (loop [rs rules
            res []
            c 0]
       (if (or (empty? rs) (> c 1))
         res
         (let [rule (first rs)
               results (eval-rule words (:chunks rule) (:typ rule) [] env depth)
               newc (if (empty? results) c (inc c))
               results (flatten (into []
                                      (map #(result->vertex rules rule % env depth)
                                              results)))]
           (recur (rest rs)
                  (set/union res results)
                  newc)))))
  ([rules words env]
     (parse rules words env 0)))

(defn parse->vertex
  [par]
  #_(let [vert (:vertex par)]
    (if (coll? vert)
      (id/ids->id (map parse->vertex vert))
      vert)))

(defn vert+weight
  [par]
  #_(let [vert (:vertex par)
        rule (:rule par)
        weight (if rule
                 (:priority rule) 0)]
    (if (coll? vert)
      (let [vws (map vert+weight vert)
            id (id/ids->id (map :vert vws))
            w (reduce + (map :weight vws))]
        (assoc par
          :vert id
          :weight (+ w weight)))
      (assoc par
        :vert vert
        :weight weight))))

(defn verts+weights->vertex
  [rules vws env]
  (let [best (apply max (map :weight vws))
        vws (filter #(= (:weight %) best) vws)]
    (:vert (first vws))))

(defn parse-words
  [rules words env]
  (let [par (parse rules words env)
        vws (map vert+weight par)]
    (verts+weights->vertex rules vws env)))

(defn parse-str
  [rules s env]
  (parse-words rules
               (words/str->words s)
               env))

(defmacro !
  [words]
  `(parse ~'rules ~words ~'env ~'(inc depth)))
