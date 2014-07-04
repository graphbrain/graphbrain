(ns graphbrain.core
  (:require [clojure.tools.cli :as cli]
            [graphbrain.braingenerators.wordnet :as wordnet]
            [graphbrain.tools.contexts :as ctxts]
            [graphbrain.braingenerators.emmanuel :as emmanuel]
            [graphbrain.web.server :as server]))

(def cli-options
  [["-p" "--port PORT" "Port number"
    :default 80
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 0x10000) "Must be a number between 0 and 65536"]]
   ["-v" nil "Verbosity level"
    :id :verbosity
    :default 0
    :assoc-fn (fn [m k _] (update-in m [k] inc))]
   ["-h" "--help"]])

(defn -main
  [& args]
  (let [opts (cli/parse-opts args cli-options)
        arg (first (:arguments opts))
        arg1 (second (:arguments opts))
        arg2 (nth (:arguments opts) 2)]
    (case arg
      "wordnet" (wordnet/run!)
      "webapp" (server/run!)
      "addcontext" (ctxts/add-context-to-user! arg1 arg2)
      "emmanuel" (emmanuel/run! arg1)
      (prn (str "Unknown command: " arg)))))
