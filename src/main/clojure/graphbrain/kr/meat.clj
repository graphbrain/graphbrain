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

(ns graphbrain.kr.meat
  (:require [graphbrain.kr.webtools :as webtools])
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
  (let [html-str (webtools/slurp-url url-str)]
    (extract-meat html-str)))
