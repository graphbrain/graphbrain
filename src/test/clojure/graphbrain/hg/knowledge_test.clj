(ns graphbrain.hg.knowledge-test
  (:use clojure.test
        graphbrain.hg.knowledge)
  (:require [graphbrain.hg.connection :as conn]))

(deftest degree-test
  (let [hg (conn/create :mysql "gbtest")]
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
  (let [hg (conn/create :mysql "gbtest")]
    (add! hg ["is" "graphbrain/1" "great/1"])
    (is (= (symbols-with-root hg "graphbrain") #{"graphbrain/1"}))
    (add! hg ["is" "graphbrain/2" "great/1"])
    (is (= (symbols-with-root hg "graphbrain") #{"graphbrain/1" "graphbrain/2"}))
    (remove! hg ["is" "graphbrain/1" "great/1"])
    (remove! hg ["is" "graphbrain/2" "great/1"])
    (is (= (symbols-with-root hg "graphbrain") #{}))))

(deftest beliefs-test
  (let [hg (conn/create :mysql "gbtest")]
    (add-belief! hg "mary/1" ["is" "graphbrain/1" "great/1"])
    (is (= (sources hg ["is" "graphbrain/1" "great/1"]) #{"mary/1"}))
    (add-belief! hg "john/1" ["is" "graphbrain/1" "great/1"])
    (is (= (sources hg ["is" "graphbrain/1" "great/1"]) #{"mary/1" "john/1"}))
    (remove-belief! hg "mary/1" ["is" "graphbrain/1" "great/1"])
    (is (exists? hg ["is" "graphbrain/1" "great/1"]))
    (remove-belief! hg "john/1" ["is" "graphbrain/1" "great/1"])
    (is (not (exists? hg ["is" "graphbrain/1" "great/1"])))))
