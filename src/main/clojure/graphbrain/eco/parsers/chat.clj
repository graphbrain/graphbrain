(ns graphbrain.eco.parsers.chat
  (:use graphbrain.eco.eco
        [graphbrain.utils :only [dbg]]))

(ecoparser chat)

(pattern chat
         [xwith [(w "with")]
          x1 [(w "a")]
          a [(!w ",")]
          x2 [(w ",")]
          b [!verb]
          verb [verb]
          c [!verb]]
         (let [x (p chat a)
               y (p chat b)
               z (p chat (concat b verb c))
               k ["r/has" y x]]
           (edge "r/+list" k z)))

(pattern chat
         [a [!verb]
          verb1 [verb]
          b [(!w "and") !verb]
          xand1 [(w "and")]
          verb2 [verb]
          c [!verb]]
         (let [x (concat a verb1 b)
               y (concat a verb2 c)]
           (edge "r/+list" (p chat x) (p chat y))))

(pattern chat
         [a [!verb]
          verb1 [verb]
          b [!verb]
          xand2 [(w "and")]
          verb2 [verb]
          c [!verb]]
         (let [x (concat a verb1 b)
               y (concat a verb2 c)]
           (edge "r/+list" x y)))

(pattern chat
         [a [!verb]
          verb [verb]
          b [!verb !ind]
          in1 [ind]
          c [!verb]
          xand3 [(w "and")]
          d [!verb !ind]
          in2 [ind]
          e [!verb]]
         (if (not (ends-with (concat in1 c) (concat in2 e)))
           (let [x (p chat (concat a verb b in1 c))
                 y (p chat (concat a verb d in2 e))]
             (edge "r/+list" x y))))

(pattern chat
         [a [!verb]
          verb [verb]
          b [!verb (!w "and")]
          xand4 [(w "and")]
          c [!verb !ind]
          in [ind]
          d [!verb]]
         (let [x (p chat (concat a verb b in d))
               y (p chat (concat a verb c in d))]
           (edge "r/+list" x y)))

(pattern chat
         [a [!verb]
          verb [verb]
          b [!verb (!w "and")]
          xand5 [(w "and")]
          c [!verb]]
         (let [x (concat a verb b)
               y (concat a verb c)]
           (edge "r/+list" x y)))

(pattern chat
         [a [!verb]
          verb [verb]
          in [ind]
          b [!verb]]
         (let [orig (p chat a)
               rel (rel (concat verb in))
               targ (p chat b)]
           (edge rel orig targ)))

(pattern chat
         [a []
          verb [verb]
          c []]
         (let [orig (p chat a)
               rel (rel verb)
               targ (p chat c)]
           (edge rel orig targ)))

(pattern chat
         [a [!verb (!w "of")]
          xof [(w "of")]
          b [verb]]
         (let [x (p chat a)
               y (p chat b)]
           (eid "r/+of" (words->str a xof b) x y)))

(pattern chat
         [a [(!w "'s")]
          xs [(w "'s")]
          b []]
         (let [x (p chat a)
               y (p chat b)]
           (eid "r/+poss" (str (words->str a) "'s " (words->str b)) x y)))

(pattern chat
         [a [(!w "in")]
          xin [(w "in")]
          b []]
         (let [x (p chat a)
               y (p chat b)]
           (eid  "r/+in" (words->str a xin b) x y)))

(pattern chat
         [prop [#(or (adjective %) (adverb %))]
          obj [!verb]]
         (let [x (p chat prop)
               y (p chat obj)]
           (eid "r/+prop" (words->str prop obj) x y)))

(pattern chat
         [a [det]
          no-dt []]
         (p chat no-dt))

(pattern chat
         [i [(w "i")]]
         (user env))

(pattern chat
         [this [(w "this")]]
         (root env))

(pattern chat
         [obj []]
         (entity obj))
