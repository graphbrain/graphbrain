(ns graphbrain.web.handlers.ecoruntests
  (:use (graphbrain.web common)
        (graphbrain.web.views ecopage ecoruntests))
  (:import (com.graphbrain.db TextNode)
           (com.graphbrain.eco Prog Tests)
           (com.graphbrain.web VisualContext)))

(defn- javalist2list
  [java-list]
  (loop
    [jl java-list
     l ()]
    (if (empty? jl)
      (reverse l)
      (recur (rest jl) (conj l (first jl))))))

(defn- test2ctxts-list
  [prog test]
  (javalist2list
      (. prog wv test 0)))

(defn- test-flatten
  [tests]
  (loop
    [t tests
     f ()]
    (if (empty? t)
      f
      (recur
        (rest t)
        (concat f
          (map (fn [x] (list x (second (first t)))) (first (first t))))))))

(defn- tests2ctxts-list
  [prog tests]
  (test-flatten
    (map
      (fn [t] (list
                (test2ctxts-list prog (first t))
                (second t))) tests)))

(defn- ctxts-list2ctxt-list
  [ctxts-list]
  (test-flatten
    (map
      (fn [ctxts]
        (list
          (javalist2list (. (first ctxts) getCtxts))
          (second ctxts))) ctxts-list)))

(defn- ctxt-list2vc-list
  [ctxt-list]
  (map
    (fn [c]
      (new VisualContext (first c) (second c))) ctxt-list))

(defn- tests2vc-list
  [prog tests]
  (ctxt-list2vc-list
    (ctxts-list2ctxt-list
      (tests2ctxts-list prog tests))))

(defn- array2tests
  [test-array]
  (loop
    [ta test-array
     tests ()]
    (if (empty? ta)
      (reverse tests)
      (recur
        (rest ta)
        (conj tests
          (list
            (aget (first ta) 0)
            (aget (first ta) 1)))))))

(defn handle-ecoruntests-get
  [request]
  (ecopage
      :title "Run Tests"
      :body-fun (fn [] (ecoruntests-view nil))))

(defn handle-ecoruntests-post
  [request]
  (let
    [tests-str (get-tests)
     prog (Prog/fromString (get-code) graph)
     tests (array2tests (. (new Tests tests-str) getTests))
     vcl (tests2vc-list prog tests)]
    (ecopage
      :title "Run Tests"
      :body-fun (fn [] (ecoruntests-view vcl)))))