(ns graphbrain.hg.sql-test
  (:use clojure.test
        graphbrain.hg.sql)
  (:require [graphbrain.hg.ops :as ops]))

(deftest edge-matches-pattern?-test
  (is (edge-matches-pattern? ["a" "b" "c"] [nil "b" nil]))
  (is (edge-matches-pattern? ["a" "b" "c"] [nil nil nil]))
  (is (edge-matches-pattern? ["a" "b" "c"] ["a" "b" "c"]))
  (is (edge-matches-pattern? ["a" "b" "c"] ["a" nil nil]))
  (is (not (edge-matches-pattern? ["a" "b" "c"] [nil "x" nil]))))
