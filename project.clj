(defproject graphbrain "0.1.0-SNAPSHOT"
  :description "GraphBrain project"
  :url "http://graphbrain.com/"
  :dependencies  [[org.clojure/clojure "1.5.1"]
                 [commons-io/commons-io "2.4"]
                 [org.apache.commons/commons-lang3 "3.1"]
                 [commons-lang/commons-lang "2.6"]
                 [mysql/mysql-connector-java "5.1.27"]
                 [org.mindrot/jbcrypt "0.3m"]
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
                 [ring/ring-jetty-adapter "0.3.11"]
                 [hiccup/hiccup "1.0.5"]
                 [org.clojure/data.json "0.2.4"]
                 [org.clojure/tools.nrepl "0.2.3"]
                 [org.clojure/math.combinatorics "0.0.7"]
                 [clj-http "0.9.0"]
                 [org.clojure/clojurescript "0.0-2173"]
                 [jayq "2.5.0"]]
  :plugins [[lein-ring "0.8.10"]
            [lein-cljsbuild "1.0.0"]]
  :source-paths ["src/main/clojure" "src/main/cljs"]
  :java-source-paths ["src/main/java"]
  :test-paths ["test" "src/test/clojure"]
  :resource-paths ["src/main/resources"]
  :ring {:handler graphbrain.web.server/handler}

  ;; cljsbuild options configuration
  :cljsbuild {:builds
              [{:source-paths ["src/main/cljs"],
                :compiler
                {:output-to "src/main/resources/js/gbui.js",
                 :foreign-libs
                 [{:file "src/main/js/seedrandom.js" :provides ["seedrandom"]}
                  {:file "src/main/js/jquery.cookie.js" :provides ["jquery.cookie"]}
                  {:file "src/main/js/jquery.mousewheel.js" :provides ["jquery.mousewheel"]}
                  {:file "src/main/js/slimScroll.js" :provides ["slimscroll"]}
                  {:file "src/main/js/browsers.js" :provides ["browsers"]}
                  {:file "src/main/js/vec3mat4x4.js" :provides ["vec3mat4x4"]}
                  {:file "src/main/js/Quaternion.js" :provides ["quaternion"]}
                  {:file "src/main/js/alerts.js" :provides ["alerts"]}
                  {:file "src/main/js/interface.js" :provides ["interf"]}
                  {:file "src/main/js/layout.js" :provides ["layout"]}
                  {:file "src/main/js/search.js" :provides ["search"]}
                  {:file "src/main/js/disambiguate.js" :provides ["disambiguate"]}
                  {:file "src/main/js/undo.js" :provides ["undo"]}
                  {:file "src/main/js/user.js" :provides ["user"]}
                  {:file "src/main/js/relations.js" :provides ["relations"]}
                  {:file "src/main/js/aichat.js" :provides ["aichat"]}
                  {:file "src/main/js/State.js" :provides ["state"]}
                  {:file "src/main/js/main.js" :provides ["main"]}],
                 :optimizations :whitespace,
                 :pretty-print true}}]})
