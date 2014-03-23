(defproject gbui "0.1.0-SNAPSHOT"
  :description "GraphBrain's user interface"
  :url "http://graphbrain.com"

  ;; CLJ AND CLJS source code path
  :source-paths ["src/clj" "src/cljs"]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2173"]
                 [compojure "1.1.6"]]

  ;; lein-cljsbuild plugin to build a CLJS project
  :plugins [[lein-cljsbuild "1.0.0"]
            [lein-ring "0.8.8"]]

  :ring {:handler graphbrain.gbui.server/handler}

  ;; cljsbuild options configuration
  :cljsbuild {:buildsp
              [{;; CLJS source code path
                :source-paths ["src/cljs"]

                ;; Google Closure (CLS) options configuration
                :compiler {;; CLS generated JS script filename
                           :output-to "resources/public/js/gbui.js"

                           ;; minimal JS optimization directive
                           :optimizations :whitespace

                           ;; generated JS code prettyfication
                           :pretty-print true}}]})
