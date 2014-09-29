(ns graphbrain.eco.parsers.chat
  (:use graphbrain.eco.eco
        [graphbrain.utils :only [dbg]]))

(ecoparser chat)

(pattern chat
         [xwith "with", x1 "a", a ?, x2 ",", b ?, verb verb, c ?]
         (let [x (! a)
               y (! b)
               z (! (concat b verb c))
               k ["r/has" y x]]
           (edge "r/+list" k z)))

(pattern chat
         [a ?, verb1 verb, b ?, xand1 "and", verb2 verb, c ?]
         (let [x (concat a verb1 b)
               y (concat a verb2 c)]
           (edge "r/+list" (! x) (! y))))

(pattern chat
         [a ?, verb1 verb, b ?, xand2 "and", verb2 verb, c ?]
         (let [x (concat a verb1 b)
               y (concat a verb2 c)]
           (edge "r/+list" x y)))

(pattern chat
         [a ?, verb verb, b ?, in1 ind, c ?, xand3 "and", d ?, in2 ind, e ?]
         (if (not (ends-with (concat in1 c) (concat in2 e)))
           (let [x (! (concat a verb b in1 c))
                 y (! (concat a verb d in2 e))]
             (edge "r/+list" x y))))

(pattern chat
         [a ?, verb verb, b ?, xand4 "and", c ?, in ind, d ?]
         (let [x (! (concat a verb b in d))
               y (! (concat a verb c in d))]
           (edge "r/+list" x y)))

(pattern chat
         [a ?, verb verb, b ?, xand5 "and", c ?]
         (let [x (concat a verb b)
               y (concat a verb c)]
           (edge "r/+list" x y)))

(pattern chat
         [a ?, verb verb, in ind, b ?]
         (let [orig (! a)
               rel (rel (concat verb in))
               targ (! b)]
           (edge rel orig targ)))

(pattern chat
         [a ?, verb verb, c ?]
         (let [orig (! a)
               rel (rel verb)
               targ (! c)]
           (edge rel orig targ)))

(pattern chat
         [a ?, xof "of", b verb]
         (let [x (! a)
               y (! b)]
           (eid "r/+of" (words->str a xof b) x y)))

(pattern chat
         [a ?, xs "'s", b ?]
         (let [x (! a)
               y (! b)]
           (eid "r/+poss" (str (words->str a) "'s " (words->str b)) x y)))

(pattern chat
         [a ?, xin "in", b ?]
         (let [x (! a)
               y (! b)]
           (eid  "r/+in" (words->str a xin b) x y)))

(pattern chat
         [prop (| adjective adverb), obj ?]
         (let [x (! prop)
               y (! obj)]
           (eid "r/+prop" (words->str prop obj) x y)))

(pattern chat
         [a det, no-dt ?]
         (! no-dt))

(pattern chat
         [i "i"]
         (user env))

(pattern chat
         [this "this"]
         (root env))

(pattern chat
         [obj ?]
         (entity obj))

(defn chat-test
  [s]
  (let [env {:root "f43806bb591e3b87/berlin", :user "u/telmo"}]
    (parse-str graphbrain.eco.parsers.chat/chat s env)))