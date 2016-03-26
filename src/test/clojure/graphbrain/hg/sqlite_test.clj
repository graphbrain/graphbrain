(ns graphbrain.hg.sqlite-test
  (:use clojure.test
        graphbrain.hg.sqlite)
  (:require [graphbrain.hg.ops :as ops]))

(deftest mysql-test-1
  (let [hg (connection "gbtest")]
    (ops/add! hg ["is" "graphbrain/1" "great/1"])
    (is (ops/exists? hg ["is" "graphbrain/1" "great/1"]))
    (ops/remove! hg ["is" "graphbrain/1" "great/1"])
    (is (not (ops/exists? hg ["is" "graphbrain/1" "great/1"])))))

(deftest mysql-test-2
  (let [hg (connection "gbtest")]
    (ops/add! hg ["size" "graphbrain/1" 7])
    (is (ops/exists? hg ["size" "graphbrain/1" 7]))
    (ops/remove! hg ["size" "graphbrain/1" 7])
    (is (not (ops/exists? hg ["size" "graphbrain/1" 7])))))

(deftest mysql-test-3
  (let [hg (connection "gbtest")]
    (ops/add! hg ["size" "graphbrain/1" 7.0])
    (is (ops/exists? hg ["size" "graphbrain/1" 7.0]))
    (ops/remove! hg ["size" "graphbrain/1" 7.0])
    (is (not (ops/exists? hg ["size" "graphbrain/1" 7.0])))))

(deftest mysql-test-4
  (let [hg (connection "gbtest")]
    (ops/add! hg ["size" "graphbrain/1" -7])
    (is (ops/exists? hg ["size" "graphbrain/1" -7]))
    (ops/remove! hg ["size" "graphbrain/1" -7])
    (is (not (ops/exists? hg ["size" "graphbrain/1" -7])))))

(deftest mysql-test-5
  (let [hg (connection "gbtest")]
    (ops/add! hg ["size" "graphbrain/1" -7.0])
    (is (ops/exists? hg ["size" "graphbrain/1" -7.0]))
    (ops/remove! hg ["size" "graphbrain/1" -7.0])
    (is (not (ops/exists? hg ["size" "graphbrain/1" -7.0])))))

(deftest mysql-test-6
  (let [hg (connection "gbtest")]
    (ops/add! hg ["src" "graphbrain/1" ["size" "graphbrain/1" -7.0]])
    (is (ops/exists? hg ["src" "graphbrain/1" ["size" "graphbrain/1" -7.0]]))
    (ops/remove! hg ["src" "graphbrain/1" ["size" "graphbrain/1" -7.0]])
    (is (not (ops/exists? hg ["src" "graphbrain/1" ["size" "graphbrain/1" -7.0]])))))

(deftest destroy-test
  (let [hg (connection "gbtest")]
    (ops/add! hg ["src" "graphbrain/1" ["size" "graphbrain/1" -7.0]])
    (is (ops/exists? hg ["src" "graphbrain/1" ["size" "graphbrain/1" -7.0]]))
    (ops/destroy! hg)
    (is (not (ops/exists? hg ["src" "graphbrain/1" ["size" "graphbrain/1" -7.0]])))))

(deftest pattern->edges-test
  (let [hg (connection "gbtest")]
    (ops/add! hg ["is" "graphbrain/1" "great/1"])
    (is (= (ops/pattern->edges hg [nil "graphbrain/1" nil])
           '(["is" "graphbrain/1" "great/1"])))
    (is (= (ops/pattern->edges hg ["is" "graphbrain/1" nil])
           '(["is" "graphbrain/1" "great/1"])))
    (is (= (ops/pattern->edges hg ["x" nil nil])
           '()))
    (ops/remove! hg ["is" "graphbrain/1" "great/1"])))

(deftest star-test
  (let [hg (connection "gbtest")]
    (ops/add! hg ["is" "graphbrain/1" "great/1"])
    (is (= (ops/star hg "graphbrain/1")
           #{["is" "graphbrain/1" "great/1"]}))
    (is (= (ops/star hg "graphbrain/2")
           #{}))
    (ops/remove! hg ["is" "graphbrain/1" "great/1"])))

(deftest symbols-with-root-test
  (let [hg (connection "gbtest")]
    (ops/add! hg ["is" "graphbrain/1" "great/1"])
    (is (= (ops/symbols-with-root hg "graphbrain") #{"graphbrain/1"}))
    (ops/add! hg ["is" "graphbrain/2" "great/1"])
    (is (= (ops/symbols-with-root hg "graphbrain") #{"graphbrain/1" "graphbrain/2"}))
    (ops/remove! hg ["is" "graphbrain/1" "great/1"])
    (ops/remove! hg ["is" "graphbrain/2" "great/1"])
    (is (= (ops/symbols-with-root hg "graphbrain") #{}))))

(deftest degree-test
  (let [hg (connection "gbtest")]
    (is (= (ops/degree hg "graphbrain/1") 0))
    (ops/add! hg ["is" "graphbrain/1" "great/1"])
    (is (= (ops/degree hg "graphbrain/1") 1))
    (is (= (ops/degree hg "great/1") 1))
    (ops/add! hg ["size" "graphbrain/1" 7])
    (is (= (ops/degree hg "graphbrain/1") 2))
    (is (= (ops/degree hg "great/1") 1))
    (ops/remove! hg ["is" "graphbrain/1" "great/1"])
    (is (= (ops/degree hg "graphbrain/1") 1))
    (is (= (ops/degree hg "great/1") 0))
    (ops/remove! hg ["size" "graphbrain/1" 7])
    (is (= (ops/degree hg "graphbrain/1") 0))))
