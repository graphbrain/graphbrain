(ns graphbrain.hg.ops-test
  (:use clojure.test
        graphbrain.hg.ops)
  (:require [graphbrain.hg.connection :as conn]))

(defn ops-test-1
  [hg]
  (add! hg ["is" "graphbrain/1" "great/1"])
  (is (exists? hg ["is" "graphbrain/1" "great/1"]))
  (remove! hg ["is" "graphbrain/1" "great/1"])
  (is (not (exists? hg ["is" "graphbrain/1" "great/1"]))))

(defn ops-test-2
  [hg]
  (add! hg ["size" "graphbrain/1" 7])
  (is (exists? hg ["size" "graphbrain/1" 7]))
  (remove! hg ["size" "graphbrain/1" 7])
  (is (not (exists? hg ["size" "graphbrain/1" 7]))))

(defn ops-test-3
  [hg]
  (add! hg ["size" "graphbrain/1" 7.0])
  (is (exists? hg ["size" "graphbrain/1" 7.0]))
  (remove! hg ["size" "graphbrain/1" 7.0])
  (is (not (exists? hg ["size" "graphbrain/1" 7.0]))))

(defn ops-test-4
  [hg]
  (add! hg ["size" "graphbrain/1" -7])
  (is (exists? hg ["size" "graphbrain/1" -7]))
  (remove! hg ["size" "graphbrain/1" -7])
  (is (not (exists? hg ["size" "graphbrain/1" -7]))))

(defn ops-test-5
  [hg]
  (add! hg ["size" "graphbrain/1" -7.0])
  (is (exists? hg ["size" "graphbrain/1" -7.0]))
  (remove! hg ["size" "graphbrain/1" -7.0])
  (is (not (exists? hg ["size" "graphbrain/1" -7.0]))))

(defn ops-test-6
  [hg]
  (add! hg ["src" "graphbrain/1" ["size" "graphbrain/1" -7.0]])
  (is (exists? hg ["src" "graphbrain/1" ["size" "graphbrain/1" -7.0]]))
  (remove! hg ["src" "graphbrain/1" ["size" "graphbrain/1" -7.0]])
  (is (not (exists? hg ["src" "graphbrain/1" ["size" "graphbrain/1" -7.0]]))))

(defn destroy-test
  [hg]
  (add! hg ["src" "graphbrain/1" ["size" "graphbrain/1" -7.0]])
  (is (exists? hg ["src" "graphbrain/1" ["size" "graphbrain/1" -7.0]]))
  (destroy! hg)
  (is (not (exists? hg ["src" "graphbrain/1" ["size" "graphbrain/1" -7.0]]))))

(defn pattern->edges-test
  [hg]
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
  (remove! hg ["says" "mary/1" ["is" "graphbrain/1" "great/1"]]))

(defn star-test
  [hg]
  (add! hg ["is" "graphbrain/1" "great/1"])
  (is (= (star hg "graphbrain/1")
         #{["is" "graphbrain/1" "great/1"]}))
  (is (= (star hg "graphbrain/2")
         #{}))
  (remove! hg ["is" "graphbrain/1" "great/1"]))

(defn symbols-with-root-test
  [hg]
  (add! hg ["is" "graphbrain/1" "great/1"])
  (is (= (symbols-with-root hg "graphbrain") #{"graphbrain/1"}))
  (add! hg ["is" "graphbrain/2" "great/1"])
  (is (= (symbols-with-root hg "graphbrain") #{"graphbrain/1" "graphbrain/2"}))
  (remove! hg ["is" "graphbrain/1" "great/1"])
  (remove! hg ["is" "graphbrain/2" "great/1"])
  (is (= (symbols-with-root hg "graphbrain") #{})))

(defn degree-test
  [hg]
  (is (= (degree hg "graphbrain/1") 0))
  (add! hg ["is" "graphbrain/1" "great/1"])
  (is (= (degree hg "graphbrain/1") 1))
  (is (= (degree hg "great/1") 1))
  (add! hg ["size" "graphbrain/1" 7])
  (is (= (degree hg "graphbrain/1") 2))
  (is (= (degree hg "great/1") 1))
  (remove! hg ["is" "graphbrain/1" "great/1"])
  (is (= (degree hg "graphbrain/1") 1))
  (is (= (degree hg "great/1") 0))
  (remove! hg ["size" "graphbrain/1" 7])
  (is (= (degree hg "graphbrain/1") 0)))

(defn add-remove-multiple-test
  [hg]
  (add! hg [["is" "graphbrain/1" "great/1"]
            ["src" "graphbrain/1" ["size" "graphbrain/1" -7.0]]])
  (is (exists? hg ["is" "graphbrain/1" "great/1"]))
  (is (exists? hg ["src" "graphbrain/1" ["size" "graphbrain/1" -7.0]]))
  (remove! hg [["is" "graphbrain/1" "great/1"]
               ["src" "graphbrain/1" ["size" "graphbrain/1" -7.0]]])
  (is (not (exists? hg ["is" "graphbrain/1" "great/1"])))
  (is (not (exists? hg ["src" "graphbrain/1" ["size" "graphbrain/1" -7.0]]))))

(defn batch-exec-test
  [hg]
  (let [f1 #(add! % ["is" "graphbrain/1" "great/1"])
        f2 #(add! % ["src" "graphbrain/1" ["size" "graphbrain/1" -7.0]])]
    (batch-exec! hg [f1 f2]))
  (is (exists? hg ["is" "graphbrain/1" "great/1"]))
  (is (exists? hg ["src" "graphbrain/1" ["size" "graphbrain/1" -7.0]]))
  (let [f1 #(remove! % ["is" "graphbrain/1" "great/1"])
        f2 #(remove! % ["src" "graphbrain/1" ["size" "graphbrain/1" -7.0]])]
    (batch-exec! hg [f1 f2]))
  (is (not (exists? hg ["is" "graphbrain/1" "great/1"])))
  (is (not (exists? hg ["src" "graphbrain/1" ["size" "graphbrain/1" -7.0]]))))

(defn f-all-test
  [hg]
  (destroy! hg)
  (add! hg ["size" "graphbrain/1" -7.0])
  (add! hg ["is" "graphbrain/1" "great/1"])
  (add! hg ["src" "mary/1" ["is" "graphbrain/1" "great/1"]])
  (let [labels (f-all hg #(str (str (:vertex %)) " " (:degree %)))
        labels (into #{} labels)]
    (is (= labels
           #{"size 1"
             "graphbrain/1 2"
             "-7.0 1"
             "is 1"
             "great/1 1"
             "src 1"
             "mary/1 1"
             "[\"is\" \"graphbrain/1\" \"great/1\"] 1"})))
  (destroy! hg)
  (let [labels (f-all hg #(str (str (:vertex %)) " " (:degree %)))
        labels (into #{} labels)]
    (is (= labels #{}))))

(defn ops-tests
  [hg]
  (ops-test-1 hg)
  (ops-test-2 hg)
  (ops-test-3 hg)
  (ops-test-4 hg)
  (ops-test-5 hg)
  (ops-test-6 hg)
  (destroy-test hg)
  (pattern->edges-test hg)
  (star-test hg)
  (symbols-with-root-test hg)
  (degree-test hg)
  (add-remove-multiple-test hg)
  (batch-exec-test hg)
  (f-all-test hg))
