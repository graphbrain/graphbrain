(ns graphbrain.hg.sqlite-test
  (:use clojure.test
        graphbrain.hg.sqlite
        graphbrain.hg.ops-test)
  (:require [graphbrain.hg.ops :as ops]))

(deftest sqlite-tests
  (ops-tests
   (connection "gbtest")))
