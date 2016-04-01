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

(ns graphbrain.kr.wikicrawl
  (:import (org.wikipedia Wiki)))

(defn keep-trying*
  "Executes thunk. If an exception is thrown, will retry. At most n retries
  are done. If still some exception is thrown it is bubbled upwards in
  the call chain."
  [thunk]
  (loop [n 0]
    (if (> n 0)
      (do
        (println (str "sleeping for " n " secs."))
        (Thread/sleep (* n 1000))
        (println (str "retry #" n))))
    (if-let [result (try
                      [(thunk)]
                      (catch Exception e nil))]
      (result 0)
      (recur (inc n)))))

(defmacro keep-trying
  "Executes body. If an exception is thrown, will retry. At most n retries
  are done. If still some exception is thrown it is bubbled upwards in
  the call chain."
  [& body]
  `(keep-trying* (fn [] ~@body)))

(defn revisions
  [title]
  (let [wiki (Wiki. "en.wikipedia.org")
        hist (reverse
              (keep-trying (.getPageHistory wiki title)))]
    (map #(hash-map :user (.getUser %)
                    :timestamp (.getTimeInMillis
                                (.getTimestamp %))
                    :text (keep-trying (.getText %)))
         hist)))

(defn follow
  [title]
  (not
   ((into #{} title) \:)))

(defn process!
  [hg titles process-page!]
  (loop [queue titles
         done #{}
         n 1]
    (if (empty? queue)
      (println "done.")
      (do
        (println (str "queue: " (count queue) "; done: " (count done)))
        (let [title-depth (first queue)
              title (first title-depth)
              depth (second title-depth)
              new-queue (rest queue)
              new-done (conj done title)
              dummy (println
                     (str "processing page #" n ": " title " [depth: " depth "]"))
              new-titles (process-page! hg title)
              new-queue (if (> depth 1)
                          new-queue
                          (reduce #(if (or (new-done %2) (not (follow %2))) %1
                                       (conj %1 [%2 (inc depth)]))
                                  new-queue new-titles))]
          (recur new-queue new-done (inc n)))))))

(defn crawl!
  [hg title]
  (process! hg [[title 0]]))
