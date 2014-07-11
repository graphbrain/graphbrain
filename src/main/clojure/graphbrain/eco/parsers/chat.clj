(ns graphbrain.eco.parsers.chat
  (:use graphbrain.eco.eco))

(ecoparser chat)

(eco-wv chat
        [x0 [(w "with")]
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
          ["r/+list" k z]))

(eco-wv chat
        [a [!verb]
         verb1 [verb]
         b [(!w "and") !verb]
         x0 [(w "and")]
         verb2 [verb]
         c [!verb]]
        (let [x (concat a verb1 b)
              y (concat a verb2 c)]
          ["r/+list" (p chat x) (p chat y)]))

(eco-wv chat
        [a [!verb]
         verb1 [verb]
         b [!verb]
         x0 [(w "and")]
         verb2 [verb]
         c [!verb]]
        (let [x (concat a verb1 b)
              y (concat a verb2 c)]
          ["r/+list" x y]))

(eco-wv chat
        [a [!verb]
         verb [verb]
         b [!verb !ind]
         in1 [ind]
         c [!verb]
         x0 [(w "and")]
         d [!verb !ind]
         in2 [ind]
         e [!verb]]
        (if (not (ends-with (concat in1 c) (concat in2 e)))
          (let [x (p chat (concat a verb b in1 c))
                y (p chat (concat a verb d in2 e))]
            ["r/+list" x y])))

(eco-wv chat
        [a [!verb]
         verb [verb]
         b [!verb (!w "and")]
         x0 (w "and")
         c [!verb !ind]
         in [ind]
         d [!verb]]
        (let [x (p chat (concat a verb b in d))
              y (p chat (concat a verb c in d))]
          ["r/+list" x y]))

(eco-wv chat
        [a [!verb]
         verb [verb]
         b [!verb (!w "and")]
         x0 (w "and")
         c [!verb]]
        (let [x (concat a verb b)
              y (concat a verb c)]
          ["r/+list" x y]))

(eco-wv chat
        [a [!verb]
         verb [verb]
         in [ind]
         b [!verb]]
        (let [orig (p chat a)
              rel (rel (concat verb in))
              targ (p chat b)]
          [rel orig targ]))

(eco-wv chat
        [a [!verb]
         verb [verb]
         c [!verb]]
        (let [orig (p chat a)
              rel (rel verb)
              targ (p chat c)]
          [rel orig targ]))

(eco-wv chat
        [a [!verb (!w "of")]
         x0 [(w "of")]
         b [verb]]
        (let [x (p chat a)
              y (p chat b)]
          ["r/+of" x y]))

(eco-wv chat
        [a [(!w "'s")]
         x0 [(w "'s")]
         b []]
        (let [x (p chat a)
              y (p chat b)]
          ["r/+poss" x y]))

(eco-wv chat
        [a [(!w "in")]
         x0 [(w "in")]
         b []]
        (let [x (p chat a)
              y (p chat b)]
          ["r/+in" x y]))

(eco-wv chat
        [prop [#(or (adjective %) (adverb %))]
         obj [!verb]]
        (let [x (p chat prop)
              y (p chat obj)]
          ["r/+prop" y x]))

(eco-wv chat
        [a [det]
         no-dt [!verb]]
        (p chat no-dt))

(eco-wv chat
        [x0 [(w "i")]]
        (:user env))

(eco-wv chat
        [x0 [(w "this")]]
        (:root env))

(eco-wv chat
        [obj [!verb]]
        (entity obj))
