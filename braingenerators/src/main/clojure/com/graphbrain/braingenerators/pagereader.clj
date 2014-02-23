(ns com.graphbrain.braingenerators
  (:import (java.net URL)
           (de.l3s.boilerpipe.extractors CommonExtractors)
           (de.l3s.boilerpipe.sax HTMLHighlighter)
           (org.jsoup Jsoup)
           (org.jsoup.nodes TextNode)
           (opennlp.tools.sentdetect SentenceModel SentenceDetectorME)))

(def sentence-detector
  (let [model-in (. (. (Thread/currentThread) getContextClassLoader)
                   getResourceAsStream "pos_models/en-sent.bin")
        sentence-model (new SentenceModel model-in)]
    (. model-in close)
    (new SentenceDetectorME sentence-model)))

(defn extract-html [url-str]
  (let [url (new URL url-str)
        extractor CommonExtractors/ARTICLE_EXTRACTOR
        hh (HTMLHighlighter/newExtractingInstance)]
    (. hh process url extractor)))

(defn deep-text [node]
  (let [cn  (. node childNode 0)]
    (. cn text)))

(defn non-white-count [text]
  (loop [rest-text text
         count 0]
    (if (empty? rest-text)
      count
      (if (Character/isWhitespace (first rest-text))
        (recur (rest rest-text) count)
        (recur (rest rest-text) (inc count))))))

(defn parse-node [node char-count]
  (flatten (let [children (. node childNodes)]
    (loop [rest-chil children
           res []
           cc char-count]
      (if (empty? rest-chil)
        res
        (let [n (first rest-chil)
              elem (cond
                (instance? TextNode n) {:text (. n text)}
                (= "a" (. n nodeName)) {:text (deep-text n), :tag "a", :href (. n attr "abs:href")}
                (= "strong" (. n nodeName)) {:text (deep-text n), :tag "strong"}
                :else (parse-node n cc))
              length (if (map? elem)
                       (non-white-count (elem :text))
                       (if (empty? elem)
                         0
                         (- ((last elem) :end) cc)))]
          (recur
            (rest rest-chil)
            (conj res (if (map? elem)
                        (merge elem {:start cc, :end (+ cc length)})
                        elem))
            (+ cc length))))))))

(defn split-text-tags [text+tags]
  (loop [list text+tags
         text ""
         tags []]
    (if (empty? list)
      {:text text, :tags tags}
      (let [elem (first list)]
        (recur
          (rest list)
          (str text " " (elem :text))
          (if (nil? (elem :tag))
            tags
            (conj tags elem)))))))

(defn extract-sentences [text]
  (. sentence-detector sentDetect text))

(defn parse-doc [doc]
  (extract-sentences
    ((split-text-tags (parse-node doc 0)) :text)))

(defn read-page [url-str]
  (parse-doc (Jsoup/parse (extract-html url-str))))

(doseq [sentence (read-page "http://www.realclimate.org/index.php/archives/2014/02/going-with-the-wind/")]
  (println sentence))