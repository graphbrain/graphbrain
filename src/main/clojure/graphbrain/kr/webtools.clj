(ns graphbrain.kr.webtools
  (:require [clj-http.client :as client])
  (:import (java.net URL)
           (java.io BufferedReader InputStreamReader)))

(def http-agent "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.114 Safari/537.36")

(defn slurp-url[url-str]
  (:body (client/get url-str {:headers {"User-Agent" http-agent}})))
