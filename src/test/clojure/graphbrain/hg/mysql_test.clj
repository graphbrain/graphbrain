(ns graphbrain.hg.mysql-test
  (:use clojure.test
        graphbrain.hg.mysql))

(deftest mysql-test-1
  (let [hg (mysql-hg "gbtest")]
    (add! hg ["is" "graphbrain/1" "great/1"])
    (is (exists? hg ["is" "graphbrain/1" "great/1"]))
    (remove! hg ["is" "graphbrain/1" "great/1"])
    (is (not (exists? hg ["is" "graphbrain/1" "great/1"])))))

(deftest mysql-test-2
  (let [hg (mysql-hg "gbtest")]
    (add! hg ["size" "graphbrain/1" 7])
    (is (exists? hg ["size" "graphbrain/1" 7]))
    (remove! hg ["size" "graphbrain/1" 7])
    (is (not (exists? hg ["size" "graphbrain/1" 7])))))

(deftest mysql-test-3
  (let [hg (mysql-hg "gbtest")]
    (add! hg ["size" "graphbrain/1" 7.0])
    (is (exists? hg ["size" "graphbrain/1" 7.0]))
    (remove! hg ["size" "graphbrain/1" 7.0])
    (is (not (exists? hg ["size" "graphbrain/1" 7.0])))))

(deftest mysql-test-4
  (let [hg (mysql-hg "gbtest")]
    (add! hg ["size" "graphbrain/1" -7])
    (is (exists? hg ["size" "graphbrain/1" -7]))
    (remove! hg ["size" "graphbrain/1" -7])
    (is (not (exists? hg ["size" "graphbrain/1" -7])))))

(deftest mysql-test-5
  (let [hg (mysql-hg "gbtest")]
    (add! hg ["size" "graphbrain/1" -7.0])
    (is (exists? hg ["size" "graphbrain/1" -7.0]))
    (remove! hg ["size" "graphbrain/1" -7.0])
    (is (not (exists? hg ["size" "graphbrain/1" -7.0])))))

(deftest mysql-test-6
  (let [hg (mysql-hg "gbtest")]
    (add! hg ["src" "graphbrain/1" ["size" "graphbrain/1" -7.0]])
    (is (exists? hg ["src" "graphbrain/1" ["size" "graphbrain/1" -7.0]]))
    (remove! hg ["src" "graphbrain/1" ["size" "graphbrain/1" -7.0]])
    (is (not (exists? hg ["src" "graphbrain/1" ["size" "graphbrain/1" -7.0]])))))

(deftest edge-matches-pattern?-test
  (is (edge-matches-pattern? ["a" "b" "c"] [nil "b" nil]))
  (is (edge-matches-pattern? ["a" "b" "c"] [nil nil nil]))
  (is (edge-matches-pattern? ["a" "b" "c"] ["a" "b" "c"]))
  (is (edge-matches-pattern? ["a" "b" "c"] ["a" nil nil]))
  (is (not (edge-matches-pattern? ["a" "b" "c"] [nil "x" nil]))))

(deftest pattern->edges-test
  (let [hg (mysql-hg "gbtest")]
    (add! hg ["is" "graphbrain/1" "great/1"])
    (is (= (pattern->edges hg [nil "graphbrain/1" nil])
           '(["is" "graphbrain/1" "great/1"])))
    (is (= (pattern->edges hg ["is" "graphbrain/1" nil])
           '(["is" "graphbrain/1" "great/1"])))
    (is (= (pattern->edges hg ["x" nil nil])
           '()))
    (remove! hg ["is" "graphbrain/1" "great/1"])))

(deftest star-test
  (let [hg (mysql-hg "gbtest")]
    (add! hg ["is" "graphbrain/1" "great/1"])
    (is (= (star hg "graphbrain/1")
           #{["is" "graphbrain/1" "great/1"]}))
    (is (= (star hg "graphbrain/2")
           #{}))
    (remove! hg ["is" "graphbrain/1" "great/1"])))
