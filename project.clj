(defproject graphbrain "0.1.0-SNAPSHOT"
  :description "GraphBrain project"
  :url "http://graphbrain.com/"
  :jvm-opts ["-Xmx750M" "-XX:-OmitStackTraceInFastThrow"]
  :dependencies  [[org.clojure/clojure "1.6.0"]
                  [org.clojure/tools.trace "0.7.8"]
                  [org.clojure/tools.cli "0.3.1"]
                  [commons-io/commons-io "2.4"]
                  [org.apache.commons/commons-lang3 "3.1"]
                  [commons-lang/commons-lang "2.6"]
                  [org.clojure/java.jdbc "0.3.3"]
                  [mysql/mysql-connector-java "5.1.27"]
                  [com.mchange/c3p0 "0.9.2.1"]
                  [org.mindrot/jbcrypt "0.3m"]
                  [xml-apis/xml-apis "1.4.01"]
                  [net.sf.extjwnl/extjwnl "1.7.1"]
                  [net.sf.extjwnl/extjwnl-data-wn31 "1.1"]
                  [com.zaxxer/HikariCP "1.2.8"]
                  [org.jsoup/jsoup "1.7.2"]
                  [edu.stanford.nlp/stanford-corenlp "3.2.0"]
                  [edu.stanford.nlp/stanford-corenlp "3.2.0" :classifier "models"]
                  [edu.stanford.nlp/stanford-parser "3.2.0"]
                  [edu.stanford.nlp/stanford-parser "3.2.0" :classifier "models"]
                  [org.json/json "20131018"]
                  [net.sourceforge.nekohtml/nekohtml "1.9.19"]
                  [compojure/compojure "1.1.6"]
                  [ring/ring-core "1.3.0"]
                  [ring/ring-jetty-adapter "1.3.0"]
                  [org.clojure/data.json "0.2.4"]
                  [org.clojure/tools.nrepl "0.2.3"]
                  [org.clojure/math.combinatorics "0.0.7"]
                  [clj-http "0.9.0"]
                  [hiccup "1.0.5"]
                  [garden "1.1.5" :exclusions [org.clojure/clojure]]
                  ;; ClojureScript
                  [org.clojure/clojurescript "0.0-2173"]
                  [jayq "2.5.0"]
                  [alandipert/storage-atom "1.2.2"]
                  [com.cemerick/pprng "0.0.2"]
                  [clojure-csv/clojure-csv "2.0.1"]
                  [hiccups "0.3.0"]]
  :plugins [[lein-ring "0.8.10"]
            [lein-cljsbuild "1.0.2"]
            [lein-garden "0.1.8" :exclusions [org.clojure/clojure]]]
  :source-paths ["src/main/clojure" "src/main/cljs"]
  :java-source-paths ["src/main/java"]
  :test-paths ["test" "src/test/clojure"]
  :resource-paths ["src/main/resources"]
  :main graphbrain.core
  :ring {:handler graphbrain.web.server/handler}
  :aot :all
  :cljsbuild {:builds
              [{:source-paths ["src/main/cljs"]
                :compiler {:output-to "src/main/resources/js/gbui.js"
                           :libs [""]
                           :externs ["src/main/resources/js/jquery-1.7.2.min.js"
                                     "src/main/resources/js/jquery-ui-1.8.18.custom.min.js"
                                     "src/main/resources/js/bootstrap.min.js"]
                           :optimizations :whitespace
                           :pretty-print true}}]}

  :garden {:builds [{:stylesheet graphbrain.web.css.main/main
                     :compiler {:output-to "src/main/resources/css/gb.css"
                                :pretty-print? false}}]})
