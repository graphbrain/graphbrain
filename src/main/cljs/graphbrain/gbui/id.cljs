(ns graphbrain.gbui.id
  (:require-macros [hiccups.core :as hiccups])
  (:require [jayq.core :as jq]
            [hiccups.runtime :as hiccupsrt])
  (:use [jayq.core :only [$]]))

(defn clean
  [id]
  (let [cid (clojure.string/replace id "/" "_")
        cid (clojure.string/replace cid "." "_")
        cid (clojure.string/replace cid "#" "_")
        cid (clojure.string/replace cid ":" "_")
        cid (clojure.string/replace cid "?" "_")
        cid (clojure.string/replace cid "=" "_")
        cid (clojure.string/replace cid ";" "_")
        cid (clojure.string/replace cid "(" "_")
        cid (clojure.string/replace cid ")" "_")
        cid (clojure.string/replace cid "+" "_")
        cid (clojure.string/replace cid #"\W" "_")]
    cid))
