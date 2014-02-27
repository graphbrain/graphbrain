(ns graphbrain.web.handlers.ecoparser
  (:use (graphbrain.web common)
        (graphbrain.web.views ecopage ecoparser))
  (:require [clojure.data.json :as json])
  (:import (com.graphbrain.eco Prog Text)
           (com.graphbrain.web VisualContext)))

(defn- get-code
  []
  (let
    [prog (. graph getProgNode "prog/prog")]
    (if prog
      (. prog getProg) "")))

(defn- text2sentences
  [text]
  (. text getSentences))

(defn- javalist2list
  [java-list]
  (loop
    [jl java-list
     l ()]
    (if (empty? jl)
      (reverse l)
      (recur (rest jl) (conj l (first jl))))))

(defn- sentence2ctxts-list
  [prog sentence]
  (javalist2list
    (. prog wv sentence 0)))

(defn- sentences2ctxts-list
  [prog sentences]
  (flatten
    (map
      (fn [s] (sentence2ctxts-list prog s)) sentences)))

(defn- ctxts-list2ctxt-list
  [ctxts-list]
  (flatten
    (map (fn [ctxts] (javalist2list (. ctxts getCtxts))) ctxts-list)))

(defn- ctxt-list2vc-list
  [ctxt-list]
  (map (fn [c] (new VisualContext c)) ctxt-list))

(defn- text2vc-list
  [prog text]
  (ctxt-list2vc-list
    (ctxts-list2ctxt-list
      (sentences2ctxts-list prog
        (text2sentences text)))))

(defn- render-parser
  [request parse-text]
  (let
    [cookie-text (:value ((request :cookies) "parse_text"))
     text (if (empty? parse-text)
       (if cookie-text cookie-text "")
       parse-text)
     t (new Text text)
     p (Prog/fromString (get-code) graph)
     vcl (text2vc-list p t)]
    (ecopage
      :title "Parse"
      :body-fun (fn [] (ecoparser-view text vcl)))))

(defn handle-ecoparser
  [request]
  (render-parser request ""))