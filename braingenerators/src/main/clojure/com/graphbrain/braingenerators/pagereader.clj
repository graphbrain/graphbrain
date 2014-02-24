(ns com.graphbrain.braingenerators
  (:import (java.net URL)
           (de.l3s.boilerpipe.extractors CommonExtractors)
           (de.l3s.boilerpipe.sax HTMLHighlighter)
           (org.jsoup Jsoup)
           (org.jsoup.nodes TextNode)
           (opennlp.tools.sentdetect SentenceModel SentenceDetectorME)
           (com.graphbrain.eco Prog POSTagger Words)
           (com.graphbrain.db Graph)))

; create opennlp sentence detector
(def sentence-detector
  (let [model-in (. (. (Thread/currentThread) getContextClassLoader)
                   getResourceAsStream "pos_models/en-sent.bin")
        sentence-model (new SentenceModel model-in)]
    (. model-in close)
    (new SentenceDetectorME sentence-model)))

; create graph
(def graph (new Graph))

; create prog
(def prog
  (Prog/fromString (slurp "eco/page.eco") graph))

(defn extract-html
  "Extract 'meat' from a page, returns html string"
  [url-str]
  (let [url (new URL url-str)
        extractor CommonExtractors/ARTICLE_EXTRACTOR
        hh (HTMLHighlighter/newExtractingInstance)]
    (. hh process url extractor)))

(defn deep-text
  "Extract the text from a html node"
  [node]
  (let [cn  (. node childNode 0)]
    (. cn text)))

(defn non-white-count
  "Count number of non-whitespace characters in a string"
  [text]
  (loop [rest-text text
         count 0]
    (if (empty? rest-text)
      count
      (if (Character/isWhitespace (first rest-text))
        (recur (rest rest-text) count)
        (recur (rest rest-text) (inc count))))))

(defn parse-node
  "Parse html node to a list of maps with info about text and certain tags"
  [node char-count]
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

(defn split-text-tags
  "Split output of parse-node into a string with all the text
  and list of maps with information about tags"
  [text+tags]
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

(defn sentence2words
  "Convert sentence string to Words object"
  [sentence]
  (new Words
    (POSTagger/annotate sentence)))

(defn extract-sentences
  "Divide a text into sentences"
  [text]
  (map sentence2words
    (. sentence-detector sentDetect text)))

(defn parse-words
  "Use eco program to parse sequence of words to graphbrain vertex"
  [words]
  (let [ctxts-list (. prog wv words 0 nil)
        ctxts (if (not (empty? ctxts-list))
               (. (first ctxts-list) getCtxts))
        ctxt (if (not (or (nil? ctxts) (empty? ctxts)))
               (first ctxts))]
    (if (not (nil? ctxt))
      (. ctxt getTopRetVertex))))

(defn parse-doc
  [doc]
  (map parse-words
    (extract-sentences
      ((split-text-tags (parse-node doc 0)) :text))))

(defn read-page
  [url-str]
  (parse-doc (Jsoup/parse (extract-html url-str))))

(defn print-words
  [words]
  (doseq [word words]
    (print (str (. word getWord) "[" (. word getPos) "] ")))
  (println ""))

(println (read-page "http://www.realclimate.org/index.php/archives/2014/02/going-with-the-wind/"))