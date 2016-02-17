(ns graphbrain.hg.knowledge-test
  (:use clojure.test
        graphbrain.hg.knowledge)
  (:require [graphbrain.hg.connection :as conn]
            [graphbrain.hg.ops :as ops]))

(deftest beliefs-test
  (let [hg (conn/create :mysql "gbtest")]
    (add-belief! hg "mary/1" ["is" "graphbrain/1" "great/1"])
    (is (= (sources hg ["is" "graphbrain/1" "great/1"]) #{"mary/1"}))
    (add-belief! hg "john/1" ["is" "graphbrain/1" "great/1"])
    (is (= (sources hg ["is" "graphbrain/1" "great/1"]) #{"mary/1" "john/1"}))
    (remove-belief! hg "mary/1" ["is" "graphbrain/1" "great/1"])
    (is (ops/exists? hg ["is" "graphbrain/1" "great/1"]))
    (remove-belief! hg "john/1" ["is" "graphbrain/1" "great/1"])
    (is (not (ops/exists? hg ["is" "graphbrain/1" "great/1"])))))
