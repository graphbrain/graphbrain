(ns graphbrain.hg.mysql-test
  (:use clojure.test
        graphbrain.hg.mysql
        graphbrain.hg.ops-test)
  (:require [graphbrain.hg.ops :as ops]))

(deftest mysql-tests
  (ops-tests
   (connection "gbtest")))
