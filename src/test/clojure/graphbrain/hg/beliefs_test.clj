(ns graphbrain.hg.beliefs-test
  (:use clojure.test
        graphbrain.hg.beliefs)
  (:require [graphbrain.hg.connection :as conn]
            [graphbrain.hg.ops :as ops]
            [graphbrain.hg.constants :as const]))

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

(deftest timestamp-test
  (let [hg (conn/create :mysql "gbtest")]
    (ops/destroy! hg)
    (is (= (ops/timestamp hg "graphbrain/1") -1))
    (add! hg "mary/1" ["is" "graphbrain/1" "great/1"] :timestamp 123456789)
    (is (= (ops/timestamp hg "graphbrain/1") 123456789))
    (is (= (ops/timestamp hg "great/1") 123456789))
    (is (= (ops/timestamp hg "mary/1") 123456789))
    (is (= (ops/timestamp hg ["is" "graphbrain/1" "great/1"]) 123456789))
    (is (= (ops/timestamp hg [const/source ["is" "graphbrain/1" "great/1"] "mary/1"])
           123456789))
    (add! hg "john/1" ["is" "graphbrain/1" "great/1"])
    (is (= (ops/timestamp hg "graphbrain/1") 123456789))
    (is (= (ops/timestamp hg "great/1") 123456789))
    (is (= (ops/timestamp hg "john/1") -1))
    (is (= (ops/timestamp hg ["is" "graphbrain/1" "great/1"]) 123456789))
    (is (= (ops/timestamp hg [const/source ["is" "graphbrain/1" "great/1"] "john/1"])
           -1))
    (remove! hg "mary/1" ["is" "graphbrain/1" "great/1"])
    (is (= (ops/timestamp hg "graphbrain/1") 123456789))
    (is (= (ops/timestamp hg "great/1") 123456789))
    (is (= (ops/timestamp hg "mary/1") 123456789))
    (is (= (ops/timestamp hg ["is" "graphbrain/1" "great/1"]) 123456789))
    (is (= (ops/timestamp hg [const/source ["is" "graphbrain/1" "great/1"] "mary/1"])
           -1))
    (remove! hg "john/1" ["is" "graphbrain/1" "great/1"])
    (is (= (ops/timestamp hg "graphbrain/1") 123456789))
    (is (= (ops/timestamp hg "great/1") 123456789))
    (is (= (ops/timestamp hg ["is" "graphbrain/1" "great/1"]) -1))
    (is (= (ops/timestamp hg [const/source ["is" "graphbrain/1" "great/1"] "john/1"])
           -1))))
