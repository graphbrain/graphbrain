(ns graphbrain.hg.null-test
  (:use clojure.test
        graphbrain.hg.null)
  (:require [graphbrain.hg.ops :as ops]))

(deftest null-test-1
  (let [hg (connection)]
    (ops/add! hg ["is" "graphbrain/1" "great/1"])
    (is (not (ops/exists? hg ["is" "graphbrain/1" "great/1"])))
    (ops/remove! hg ["is" "graphbrain/1" "great/1"])
    (is (not (ops/exists? hg ["is" "graphbrain/1" "great/1"])))))

(deftest null-test-2
  (let [hg (connection)]
    (ops/add! hg ["size" "graphbrain/1" 7])
    (is (not (ops/exists? hg ["size" "graphbrain/1" 7])))
    (ops/remove! hg ["size" "graphbrain/1" 7])
    (is (not (ops/exists? hg ["size" "graphbrain/1" 7])))))

(deftest null-test-3
  (let [hg (connection)]
    (ops/add! hg ["size" "graphbrain/1" 7.0])
    (is (not (ops/exists? hg ["size" "graphbrain/1" 7.0])))
    (ops/remove! hg ["size" "graphbrain/1" 7.0])
    (is (not (ops/exists? hg ["size" "graphbrain/1" 7.0])))))

(deftest null-test-4
  (let [hg (connection)]
    (ops/add! hg ["size" "graphbrain/1" -7])
    (is (not (ops/exists? hg ["size" "graphbrain/1" -7])))
    (ops/remove! hg ["size" "graphbrain/1" -7])
    (is (not (ops/exists? hg ["size" "graphbrain/1" -7])))))

(deftest null-test-5
  (let [hg (connection)]
    (ops/add! hg ["size" "graphbrain/1" -7.0])
    (is (not (ops/exists? hg ["size" "graphbrain/1" -7.0])))
    (ops/remove! hg ["size" "graphbrain/1" -7.0])
    (is (not (ops/exists? hg ["size" "graphbrain/1" -7.0])))))

(deftest null-test-6
  (let [hg (connection)]
    (ops/add! hg ["src" "graphbrain/1" ["size" "graphbrain/1" -7.0]])
    (is (not (ops/exists? hg ["src" "graphbrain/1" ["size" "graphbrain/1" -7.0]])))
    (ops/remove! hg ["src" "graphbrain/1" ["size" "graphbrain/1" -7.0]])
    (is (not (ops/exists? hg ["src" "graphbrain/1" ["size" "graphbrain/1" -7.0]])))))

(deftest destroy-test
  (let [hg (connection)]
    (ops/add! hg ["src" "graphbrain/1" ["size" "graphbrain/1" -7.0]])
    (is (not (ops/exists? hg ["src" "graphbrain/1" ["size" "graphbrain/1" -7.0]])))
    (ops/destroy! hg)
    (is (not (ops/exists? hg ["src" "graphbrain/1" ["size" "graphbrain/1" -7.0]])))))

(deftest pattern->edges-test
  (let [hg (connection)]
    (ops/add! hg ["is" "graphbrain/1" "great/1"])
    (is (= (ops/pattern->edges hg [nil "graphbrain/1" nil])
           #{}))
    (is (= (ops/pattern->edges hg ["is" "graphbrain/1" nil])
           #{}))
    (is (= (ops/pattern->edges hg ["x" nil nil])
           #{}))
    (ops/remove! hg ["is" "graphbrain/1" "great/1"])))

(deftest star-test
  (let [hg (connection)]
    (ops/add! hg ["is" "graphbrain/1" "great/1"])
    (is (= (ops/star hg "graphbrain/1")
           #{}))
    (is (= (ops/star hg "graphbrain/2")
           #{}))
    (ops/remove! hg ["is" "graphbrain/1" "great/1"])))
