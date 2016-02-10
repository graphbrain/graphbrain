(ns graphbrain.hg.edgestr-test
  (:use clojure.test
        graphbrain.hg.edgestr))

(deftest split-edge-str-test
  (is (= (split-edge-str "(is graphbrain/1 great/1)") ["is" "graphbrain/1" "great/1"]))
  (is (= (split-edge-str "(size graphbrain/1 7)") ["size" "graphbrain/1" "7"]))
  (is (= (split-edge-str "(size graphbrain/1 7.0)") ["size" "graphbrain/1" "7.0"]))
  (is (= (split-edge-str "(size graphbrain/1 -7)") ["size" "graphbrain/1" "-7"]))
  (is (= (split-edge-str "(size graphbrain/1 -7.0)") ["size" "graphbrain/1" "-7.0"]))
  (is (= (split-edge-str "(src graphbrain/1 (is graphbrain/1 great/1))")
         ["src" "graphbrain/1" "(is graphbrain/1 great/1)"])))

(deftest str->edge-test
  (is (= (str->edge "(is graphbrain/1 great/1)") ["is" "graphbrain/1" "great/1"]))
  (is (= (str->edge "(size graphbrain/1 7)") ["size" "graphbrain/1" 7]))
  (is (= (str->edge "(size graphbrain/1 7.0)") ["size" "graphbrain/1" 7.]))
  (is (= (str->edge "(size graphbrain/1 -7)") ["size" "graphbrain/1" -7]))
  (is (= (str->edge "(size graphbrain/1 -7.0)") ["size" "graphbrain/1" -7.]))
  (is (= (str->edge "(src graphbrain/1 (is graphbrain/1 great/1))")
         ["src" "graphbrain/1" ["is" "graphbrain/1" "great/1"]])))

(deftest edge->str-test
  (is (= (edge->str ["is" "graphbrain/1" "great/1"]) "(is graphbrain/1 great/1)"))
  (is (= (edge->str ["size" "graphbrain/1" 7]) "(size graphbrain/1 7)"))
  (is (= (edge->str ["size" "graphbrain/1" 7.]) "(size graphbrain/1 7.0)"))
  (is (= (edge->str ["src" "graphbrain/1" ["is" "graphbrain/1" "great/1"]])
         "(src graphbrain/1 (is graphbrain/1 great/1))")))
