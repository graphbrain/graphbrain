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
      (BZip2CompressorInputStream. true)
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

(defn- process?
  [page-title]
  (not
   ((into #{} page-title) \:)))

(defn page->map
  [page]
  (let [z (xml-zip page)
        title (xml1-> z :title text)]
    (println (str "visiting: " title))
    (if (process? title)
      (do
        (println (str "processing article: " title))
        (let [revs (xml-> z :revision revision->map)]
          (println (str "#revs " (count revs)))
          {:title title
           :redirect (xml1-> z :redirect (attr :title))
           :ns (xml1-> z :ns text)
           :revisions revs})))))

(defn process-page!
  [hg page]
  (w/process-page! hg page))

(defn process!
  [hg file-path]
  (with-open [rdr (bz2-reader file-path)]
    (dorun
     (->> rdr
          parse
          :content
          (filter #(= :page (:tag %)))
          (map page->map)
          (filter #(not (nil? %)))
          (map #(process-page! hg %))))))
