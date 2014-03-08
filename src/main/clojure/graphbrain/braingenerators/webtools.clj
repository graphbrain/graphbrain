(ns graphbrain.braingenerators.webtools
  (:use [clojure.string :only [join]])
  (:import (java.net URL)
           (java.io BufferedReader InputStreamReader)))


(defn slurp-url[address]
  (with-open [stream (.openStream (java.net.URL. address))]
    (let  [buf (java.io.BufferedReader. 
                (java.io.InputStreamReader. stream))]
      (apply str (line-seq buf)))))
