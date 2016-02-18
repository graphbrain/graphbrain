(ns graphbrain.hg.ops-test
  (:use clojure.test
        graphbrain.hg.ops)
  (:require [graphbrain.hg.connection :as conn]))

(deftest ops-test-1
  (let [hg (conn/create :mysql "gbtest")]
    (add! hg ["is" "graphbrain/1" "great/1"])
    (is (exists? hg ["is" "graphbrain/1" "great/1"]))
    (remove! hg ["is" "graphbrain/1" "great/1"])
    (is (not (exists? hg ["is" "graphbrain/1" "great/1"])))))

(deftest ops-test-2
  (let [hg (conn/create :mysql "gbtest")]
    (add! hg ["size" "graphbrain/1" 7])
    (is (exists? hg ["size" "graphbrain/1" 7]))
    (remove! hg ["size" "graphbrain/1" 7])
    (is (not (exists? hg ["size" "graphbrain/1" 7])))))

(deftest ops-test-3
  (let [hg (conn/create :mysql "gbtest")]
    (add! hg ["size" "graphbrain/1" 7.0])
    (is (exists? hg ["size" "graphbrain/1" 7.0]))
    (remove! hg ["size" "graphbrain/1" 7.0])
    (is (not (exists? hg ["size" "graphbrain/1" 7.0])))))

(deftest ops-test-4
  (let [hg (conn/create :mysql "gbtest")]
    (add! hg ["size" "graphbrain/1" -7])
    (is (exists? hg ["size" "graphbrain/1" -7]))
    (remove! hg ["size" "graphbrain/1" -7])
    (is (not (exists? hg ["size" "graphbrain/1" -7])))))

(deftest ops-test-5
  (let [hg (conn/create :mysql "gbtest")]
    (add! hg ["size" "graphbrain/1" -7.0])
    (is (exists? hg ["size" "graphbrain/1" -7.0]))
    (remove! hg ["size" "graphbrain/1" -7.0])
    (is (not (exists? hg ["size" "graphbrain/1" -7.0])))))

(deftest ops-test-6
  (let [hg (conn/create :mysql "gbtest")]
    (add! hg ["src" "graphbrain/1" ["size" "graphbrain/1" -7.0]])
    (is (exists? hg ["src" "graphbrain/1" ["size" "graphbrain/1" -7.0]]))
    (remove! hg ["src" "graphbrain/1" ["size" "graphbrain/1" -7.0]])
    (is (not (exists? hg ["src" "graphbrain/1" ["size" "graphbrain/1" -7.0]])))))

(deftest ops-test-add-remove-multiple
  (let [hg (conn/create :mysql "gbtest")]
    (add! hg [["is" "graphbrain/1" "great/1"]
              ["src" "graphbrain/1" ["size" "graphbrain/1" -7.0]]])
    (is (exists? hg ["is" "graphbrain/1" "great/1"]))
    (is (exists? hg ["src" "graphbrain/1" ["size" "graphbrain/1" -7.0]]))
    (remove! hg [["is" "graphbrain/1" "great/1"]
                 ["src" "graphbrain/1" ["size" "graphbrain/1" -7.0]]])
    (is (not (exists? hg ["is" "graphbrain/1" "great/1"])))
    (is (not (exists? hg ["src" "graphbrain/1" ["size" "graphbrain/1" -7.0]])))))

(deftest pattern->edges-test
  (let [hg (conn/create :mysql "gbtest")]
    (add! hg ["is" "graphbrain/1" "great/1"])
    (add! hg ["says" "mary/1" ["is" "graphbrain/1" "great/1"]])
    (is (= (pattern->edges hg [nil "graphbrain/1" nil])
           '(["is" "graphbrain/1" "great/1"])))
    (is (= (pattern->edges hg ["is" "graphbrain/1" nil])
           '(["is" "graphbrain/1" "great/1"])))
    (is (= (pattern->edges hg ["x" nil nil])
           '()))
    (is (= (pattern->edges hg ["says" nil ["is" "graphbrain/1" "great/1"]])
           '(["says" "mary/1" ["is" "graphbrain/1" "great/1"]])))
    (remove! hg ["is" "graphbrain/1" "great/1"])
    (remove! hg ["says" "mary/1" ["is" "graphbrain/1" "great/1"]])))

(deftest star-test
  (let [hg (conn/create :mysql "gbtest")]
    (add! hg ["is" "graphbrain/1" "great/1"])
    (is (= (star hg "graphbrain/1")
           #{["is" "graphbrain/1" "great/1"]}))
    (is (= (star hg "graphbrain/2")
           #{}))
    (remove! hg ["is" "graphbrain/1" "great/1"])))

(deftest symbols-with-root-test
  (let [hg (conn/create :mysql "gbtest")]
    (add! hg ["is" "graphbrain/1" "great/1"])
    (is (= (symbols-with-root hg "graphbrain") #{"graphbrain/1"}))
    (add! hg ["is" "graphbrain/2" "great/1"])
    (is (= (symbols-with-root hg "graphbrain") #{"graphbrain/1" "graphbrain/2"}))
    (remove! hg ["is" "graphbrain/1" "great/1"])
    (remove! hg ["is" "graphbrain/2" "great/1"])
    (is (= (symbols-with-root hg "graphbrain") #{}))))
