(ns graphbrain.core
  (:require [clojure.tools.cli :as cli]
            [graphbrain.hg.constants :as const]
            [graphbrain.hg.ops :as ops]
            [graphbrain.kr.wordnet :as wordnet]
            [graphbrain.web.server :as server]))

(def cli-options
  [["-s" "--storage STORAGE" "Storage type"
    :default "mysql"]
   ["-d" "--dbname" "Database name"
    :default "gb"]
   ["-u" "--dbuser" "Database user"
    :default "gb"]
   ["-p" "--dbpass" "Database password"
    :default "gb"]
   ["-P" "--port PORT" "Port number"
    :default 80
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 0x10000) "Must be a number between 0 and 65536"]]
   ["-v" nil "Verbosity level"
    :id :verbosity
    :default 0
    :assoc-fn (fn [m k _] (update-in m [k] inc))]
   ["-h" "--help"]])

(defn opts->hg
  [opts]
  (ops/hg (keyword (:storage opts)) (:dbname opts)))

(defn -main
  [& args]
  (let [cli-data (cli/parse-opts args cli-options)
        opts (:options cli-data)
        command (first (:arguments cli-data))]
    (println const/ascii-logo)
    (println)
    (case command
      "wordnet" (wordnet/import! (opts->hg opts))
      "webapp" (server/start!)
      (println (str "Unknown command: " command)))))
