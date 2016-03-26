(ns graphbrain.hg.ops-test
  (:use clojure.test
        graphbrain.hg.ops)
  (:require [graphbrain.hg.connection :as conn]))

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
