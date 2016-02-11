(ns graphbrain.hg.ops-test
  (:use clojure.test
        graphbrain.hg.ops))

(deftest ops-test-1
  (let [hg (hg :mysql "gbtest")]
    (add! hg ["is" "graphbrain/1" "great/1"])
    (is (exists? hg ["is" "graphbrain/1" "great/1"]))
    (remove! hg ["is" "graphbrain/1" "great/1"])
    (is (not (exists? hg ["is" "graphbrain/1" "great/1"])))))

(deftest ops-test-2
  (let [hg (hg :mysql "gbtest")]
    (add! hg ["size" "graphbrain/1" 7])
    (is (exists? hg ["size" "graphbrain/1" 7]))
    (remove! hg ["size" "graphbrain/1" 7])
    (is (not (exists? hg ["size" "graphbrain/1" 7])))))

(deftest ops-test-3
  (let [hg (hg :mysql "gbtest")]
    (add! hg ["size" "graphbrain/1" 7.0])
    (is (exists? hg ["size" "graphbrain/1" 7.0]))
    (remove! hg ["size" "graphbrain/1" 7.0])
    (is (not (exists? hg ["size" "graphbrain/1" 7.0])))))

(deftest ops-test-4
  (let [hg (hg :mysql "gbtest")]
    (add! hg ["size" "graphbrain/1" -7])
    (is (exists? hg ["size" "graphbrain/1" -7]))
    (remove! hg ["size" "graphbrain/1" -7])
    (is (not (exists? hg ["size" "graphbrain/1" -7])))))

(deftest ops-test-5
  (let [hg (hg :mysql "gbtest")]
    (add! hg ["size" "graphbrain/1" -7.0])
    (is (exists? hg ["size" "graphbrain/1" -7.0]))
    (remove! hg ["size" "graphbrain/1" -7.0])
    (is (not (exists? hg ["size" "graphbrain/1" -7.0])))))

(deftest ops-test-6
  (let [hg (hg :mysql "gbtest")]
    (add! hg ["src" "graphbrain/1" ["size" "graphbrain/1" -7.0]])
    (is (exists? hg ["src" "graphbrain/1" ["size" "graphbrain/1" -7.0]]))
    (remove! hg ["src" "graphbrain/1" ["size" "graphbrain/1" -7.0]])
    (is (not (exists? hg ["src" "graphbrain/1" ["size" "graphbrain/1" -7.0]])))))

(deftest pattern->edges-test
  (let [hg (hg :mysql "gbtest")]
    (add! hg ["is" "graphbrain/1" "great/1"])
    (is (= (pattern->edges hg ["*" "graphbrain/1" "*"])
           '(["is" "graphbrain/1" "great/1"])))
    (is (= (pattern->edges hg ["is" "graphbrain/1" "*"])
           '(["is" "graphbrain/1" "great/1"])))
    (is (= (pattern->edges hg ["x" "*" "*"])
           '()))
    (remove! hg ["is" "graphbrain/1" "great/1"])))

(deftest star-test
  (let [hg (hg :mysql "gbtest")]
    (add! hg ["is" "graphbrain/1" "great/1"])
    (is (= (star hg "graphbrain/1")
           #{["is" "graphbrain/1" "great/1"]}))
    (is (= (star hg "graphbrain/2")
           #{}))
    (remove! hg ["is" "graphbrain/1" "great/1"])))
