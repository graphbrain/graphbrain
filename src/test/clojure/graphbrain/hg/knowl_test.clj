(ns graphbrain.hg.knowl-test
  (:use clojure.test
        graphbrain.hg.knowl)
  (:require [graphbrain.hg.ops :as ops]))

(deftest degree-test
  (let [hg (ops/hg :mysql "gbtest")]
    (add! hg ["is" "graphbrain/1" "great/1"])
    (is (= (degree hg "graphbrain/1") 1))
    (add! hg ["belongs" "graphbrain/1" "multiverse/1"])
    (is (= (degree hg "graphbrain/1") 2))
    (is (= (degree hg "graphbrain/2") 0))
    (is (= (degree hg "great/1") 1))
    (is (= (degree hg "belongs") 1))
    (remove! hg ["is" "graphbrain/1" "great/1"])
    (is (= (degree hg "graphbrain/1") 1))
    (remove! hg ["belongs" "graphbrain/1" "multiverse/1"])
    (is (= (degree hg "graphbrain/1") 0))))

(deftest index-test
  (let [hg (ops/hg :mysql "gbtest")]
    (add! hg ["is" "graphbrain/1" "great/1"])
    (is (= (symbols-with-root hg "graphbrain") #{"graphbrain/1"}))
    (add! hg ["is" "graphbrain/2" "great/1"])
    (is (= (symbols-with-root hg "graphbrain") #{"graphbrain/1" "graphbrain/2"}))
    (remove! hg ["is" "graphbrain/1" "great/1"])
    (remove! hg ["is" "graphbrain/2" "great/1"])
    (is (= (symbols-with-root hg "graphbrain") #{}))))
