(ns graphbrain.db.urlnode
  (:import (com.graphbrain.db URLNode)))

(defn url->id
  [url-str]
  (URLNode/urlToId url-str))
