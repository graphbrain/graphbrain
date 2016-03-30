(ns graphbrain.hg.symbol-test
  (:use clojure.test
        graphbrain.hg.symbol))

(deftest hashed-test
  (is (= (hashed "graphbrain/1") "821dd667c0d1e35b")))

(deftest sym-type-test
  (is (= (sym-type "graphbrain/1") :concept))
  (is (= (sym-type 42) :integer))
  (is (= (sym-type -7.9) :float))
  (is (= (sym-type "http://graphbrain.org") :url))
  (is (= (sym-type "https://graphbrain.org") :url)))

(deftest parts-test
  (is (= (parts "graphbrain/1") ["graphbrain" "1"]))
  (is (= (parts "graphbrain") ["graphbrain"]))
  (is (= (parts "http://graphbrain.org") ["http://graphbrain.org"]))
  (is (= (parts 1)) [1])
  (is (= (parts 1.)) [1.]))

(deftest root-test
  (is (= (root "graphbrain/1") "graphbrain"))
  (is (= (root "graphbrain") "graphbrain"))
  (is (= (root "http://graphbrain.org") "http://graphbrain.org"))
  (is (= (root 1)) 1)
  (is (= (root 1.)) 1.))

(deftest root?-test
  (is (not (root? "graphbrain/1")))
  (is (root? "graphbrain"))
  (is (root? "http://graphbrain.org"))
  (is (root? 1))
  (is (root? 1.)))

(deftest build-test
  (is (= (build ["graphbrain" "1"]) "graphbrain/1")))

(deftest negative?-test
  (is (negative? "~is"))
  (is (not (negative? "is"))))

(deftest negative-test
  (is (= (negative "~is") "is"))
  (is (= (negative "is") "~is")))
