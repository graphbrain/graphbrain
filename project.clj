(defproject graphbrain "0.1.0-SNAPSHOT"
  :description "Open Knowledge Hypergraph"
  :url "http://graphbrain.org"
  :jvm-opts ["-Xmx20000M" "-server" "-XX:-OmitStackTraceInFastThrow"
;;             "-Dcom.sun.management.jmxremote"
;;             "-Dcom.sun.management.jmxremote.ssl=false"
;;             "-Dcom.sun.management.jmxremote.authenticate=false"
;;             "-Dcom.sun.management.jmxremote.port=43210"
             ]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 ;; Clojure tools
                 [org.clojure/tools.trace "0.7.9"]
                 [org.clojure/tools.cli "0.3.3"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.clojure/tools.nrepl "0.2.11"]
                 ;; Math
                 [org.clojure/math.combinatorics "0.1.1"]
                 ;; Apache commons
                 [commons-io/commons-io "2.4"]
                 [org.apache.commons/commons-lang3 "3.4"]
                 [commons-lang/commons-lang "2.6"]
                 [org.apache.commons/commons-compress "1.10"]
                 ;; DB
                 [org.clojure/java.jdbc "0.4.2"]
                 [mysql/mysql-connector-java "5.1.38"]
                 [org.xerial/sqlite-jdbc "3.8.11.2"]
                 [com.mchange/c3p0 "0.9.5.2"]
                 [com.zaxxer/HikariCP "2.4.3"]
                 ;; JSON
                 [org.json/json "20151123"]
                 [org.clojure/data.json "0.2.6"]
                 ;; XML
                 [org.clojure/data.xml "0.0.8"]
                 [org.clojure/data.zip "0.1.1"]
                 [xml-apis/xml-apis "1.4.01"]
                 ;; CSV
                 [clojure-csv/clojure-csv "2.0.1"]
                 ;; HTML
                 [org.jsoup/jsoup "1.8.3"]
                 [net.sourceforge.nekohtml/nekohtml "1.9.21"]
                 ;; NLP
                 [edu.stanford.nlp/stanford-corenlp "3.6.0"]
                 [edu.stanford.nlp/stanford-corenlp "3.6.0" :classifier "models"]
                 ;; WordNet
                 [net.sf.extjwnl/extjwnl "1.9.1"]
                 [net.sf.extjwnl/extjwnl-data-wn31 "1.2"]
                 ;; Wikipedia
                 [org.sweble.wikitext/swc-engine "2.0.0"]
                 ;; CLI
                 [clojure-term-colors "0.1.0-SNAPSHOT"]
                 ;; Web App
                 [javax.servlet/servlet-api "2.5"]
                 [ring/ring-core "1.4.0"]
                 [ring/ring-jetty-adapter "1.4.0"]
                 [compojure "1.4.0"]
                 [clj-http "2.0.1"]
                 [hiccup "1.0.5"]
                 [garden "1.3.0" :exclusions [org.clojure/clojure]]
                 [com.cemerick/url "0.1.1"]
                 ;; ClojureScript
                 [org.clojure/clojurescript "1.7.228"]
                 [jayq "2.5.4"]
                 [alandipert/storage-atom "1.2.4"]
                 [com.cemerick/pprng "0.0.3"]
                 [hiccups "0.3.0"]]
  :plugins [[lein-ring "0.9.7"]
            [lein-cljsbuild "1.1.2"]
            [lein-garden "0.2.6" :exclusions [org.clojure/clojure]]]
  :source-paths ["src/main/clojure" "src/main/cljs"]
  :java-source-paths ["src/main/java"]
  :test-paths ["test" "src/test/clojure"]
  :resource-paths ["src/main/resources"]
  :main graphbrain.core
  :repl-options {:init-ns graphbrain.repl
                 :init (graphbrain.repl/init!)}
  :ring {:handler graphbrain.web.server/handler}
  :aot :all
  :cljsbuild {:builds
              [{:source-paths ["src/main/cljs"]
                :compiler {:output-to "src/main/resources/js/gbui.js"
                           :libs [""]
                           :externs ["src/main/resources/js/jquery-1.7.2.min.js"
                                     "src/main/resources/js/bootstrap.min.js"]
                           :optimizations :whitespace
                           :pretty-print true}}]}

  :garden {:builds [{:source-paths ["src/main/clojure/graphbrain/web/css"]
                     :stylesheet graphbrain.web.css.main/main
                     :compiler {:output-to "src/main/resources/css/gb.css"
                                :pretty-print? false}}]})

