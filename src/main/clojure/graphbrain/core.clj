(ns graphbrain.core
  (:require [clojure.tools.cli :as cli]
            [graphbrain.kr.wordnet :as wordnet]
            [graphbrain.tools.contexts :as ctxts]
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
        arg (first (:arguments opts))]
    (case arg
      "wordnet" (wordnet/import!)
      "webapp" (server/start!)
      "addcontext" (ctxts/add-context-to-user!
                    (second (:arguments opts))
                    (nth (:arguments opts) 2))
      (prn (str "Unknown command: " arg)))))
