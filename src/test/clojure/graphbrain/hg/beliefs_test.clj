(ns graphbrain.hg.beliefs-test
  (:use clojure.test
        graphbrain.hg.beliefs)
  (:require [graphbrain.hg.connection :as conn]
            [graphbrain.hg.ops :as ops]))

(deftest beliefs-test
  (let [hg (conn/create :mysql "gbtest")]
    (add! hg "mary/1" ["is" "graphbrain/1" "great/1"])
    (is (= (sources hg ["is" "graphbrain/1" "great/1"]) #{"mary/1"}))
    (add! hg "john/1" ["is" "graphbrain/1" "great/1"])
    (is (= (sources hg ["is" "graphbrain/1" "great/1"]) #{"mary/1" "john/1"}))
    (remove! hg "mary/1" ["is" "graphbrain/1" "great/1"])
    (is (ops/exists? hg ["is" "graphbrain/1" "great/1"]))
    (remove! hg "john/1" ["is" "graphbrain/1" "great/1"])
    (is (not (ops/exists? hg ["is" "graphbrain/1" "great/1"])))))
