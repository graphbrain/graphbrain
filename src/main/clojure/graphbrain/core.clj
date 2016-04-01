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

(ns graphbrain.core
  (:require [clojure.tools.cli :as cli]
            [graphbrain.hg.constants :as const]
            [graphbrain.hg.connection :as conn]
            [graphbrain.hg.ops :as ops]
            [graphbrain.kr.wordnet :as wordnet]
            [graphbrain.kr.wikidump :as wikidump]
            [graphbrain.tools.destroy :as destroy]
            [graphbrain.repl :as repl]
            [graphbrain.web.server :as server]
            [clojure.term.colors :refer :all]))

(def cli-options
  [["-s" "--storage STORAGE" "Storage type"
    :default "sqlite"]
   ["-d" "--dbname" "Database name"
    :default "gb"]
   ["-u" "--dbuser" "Database user"
    :default "gb"]
   ["-p" "--dbpass" "Database password"
    :default "gb"]
   ["-P" "--port PORT" "Port number"
    :default 3000
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 0x10000) "Must be a number between 0 and 65536"]]
   ["-v" nil "Verbosity level"
    :id :verbosity
    :default 0
    :assoc-fn (fn [m k _] (update-in m [k] inc))]
   ["-h" "--help"]
   ["-f" "--file FILE" "File"]])

(defn opts->hg
  [opts]
  (conn/create (keyword (:storage opts)) (:dbname opts)))

(defn -main
  [& args]
  (let [cli-data (cli/parse-opts args cli-options)
        opts (:options cli-data)
        port (:port opts)
        command (first (:arguments cli-data))]
    (println (cyan const/ascii-logo))
    (println)
    (case command
      "wordnet" (wordnet/import! (opts->hg opts))
      "destroy" (destroy/do-it! (opts->hg opts))
      "webserver" (server/start! port)
      "process_wikidump" (wikidump/process! (opts->hg opts) (:file opts))
      "repl" (clojure.main/repl :init #(repl/init! (opts->hg opts)))
      (println (str "Unknown command: " command)))))
