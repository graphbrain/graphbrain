(ns graphbrain.braingenerators.meat
  (:use graphbrain.braingenerators.webtools)
  (:import (java.io StringReader)
           (de.l3s.boilerpipe.extractors CommonExtractors)
           (de.l3s.boilerpipe.sax HTMLHighlighter BoilerpipeSAXInput HTMLDocument)))

(defn extract-meat
  "Extract 'meat' from a html string"
  [html-str]
  (let [html-doc (new HTMLDocument html-str)
        doc (. (new BoilerpipeSAXInput (. html-doc toInputSource)) getTextDocument)
        extractor CommonExtractors/ARTICLE_EXTRACTOR
        hh (HTMLHighlighter/newExtractingInstance)
        is (. html-doc toInputSource)]
    (. extractor process doc)
    (. hh process doc is)))

(defn extract-meat-url
  "Get html from url and extract 'meat'"
  [url-str]
  (let [html-str (slurp-url url-str)]
    (extract-meat html-str)))
