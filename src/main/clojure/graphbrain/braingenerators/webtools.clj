(ns graphbrain.braingenerators.webtools
  (:require [clj-http.client :as client])
  (:import (java.net URL)
           (java.io BufferedReader InputStreamReader)))


(defn slurp-url[url-str]
  (:body (client/get url-str)))
