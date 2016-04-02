(ns graphbrain.kr.wikidump
  (:require [clojure.data.xml :refer [parse]]
            [clojure.zip :refer [xml-zip]]
            [clojure.data.zip.xml :refer [xml-> xml1-> attr attr= text]]
            [clojure.java.io :as io]
            [clj-time.format :as fmt]
            [graphbrain.kr.wikipedia :as w])
  (:import [org.apache.commons.compress.compressors.bzip2
            BZip2CompressorInputStream]))

(defn bz2-reader
  "produce a Reader on a bzipped file"
  [filename]
  (-> filename
      io/file
      io/input-stream 
      BZip2CompressorInputStream.
      io/reader))

(defn revision->map
  [revision]
  (let [z (xml-zip revision)]
    {:id (xml1-> revision :id text)
     :user (xml1-> revision :contributor :username text)
     :timestamp (/ (.getMillis
                    (fmt/parse (xml1-> revision :timestamp text)))
                   1000)
     :text (xml1-> revision :text text)}))

(defn page->map
  [page]
  (let [z (xml-zip page)]
    {:title (xml1-> z :title text)
     :redirect (xml1-> z :redirect (attr :title))
     :ns (xml1-> z :ns text)
     :revisions (xml-> z :revision revision->map)}))

(defn process?
  [page]
  (not
   ((into #{} (:title page)) \:)))

(defn process-page!
  [hg page]
  (if (process? page)
    (do
      (println (str "processing article: " (:title page)))
      (w/process-page! hg page))))

(defn process!
  [hg file-path]
  (with-open [rdr (bz2-reader file-path)]
    (doall
     (->> rdr
          parse
          :content
          (filter #(= :page (:tag %)))
          (map page->map)
          (map #(process-page! hg %))))))
