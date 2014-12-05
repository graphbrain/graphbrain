(ns graphbrain.eco.parsers.chat
  (:use graphbrain.eco.eco
        graphbrain.eco.ecofuns
        [graphbrain.utils :only [dbg]]))

(ecoparser chat)

(pattern chat
         "(object) [rel] (object)"
         [x0 "-lcb-", a ?, x1 "-rcb-",
          x2 "-lsb-", r ?, x3 "-rsb-"
          x4 "-lcb-", b ?, x5 "-rcb-"]
         (let [orig (! a)
               targ (! b)
               rel (rel r)]
           (edge rel orig targ)))

(pattern chat
         "have to -> task"
         [actor !verb
          have "have"
          to "to"
          task ?]
         (let [r (id->vert "r/has_task")
               actor (! actor)
               task (! task)]
           (edge r actor task)))

#_(pattern chat
           "with a"
           [xwith "with", x1 "a", a ?, x2 ",", b ?, verb verb, c ?]
           (let [x (! a)
                 y (! b)
                 z (! (concat b verb c))
                 k ["r/has" y x]]
             (edge "r/+list" k z)))

#_(pattern chat
           "something verb something AND verb something"
           [a ?, verb1 verb, b ?, xand1 "and", verb2 verb, c ?]
           (let [x (concat a verb1 b)
                 y (concat a verb2 c)]
             (edge "r/+list" (! x) (! y))))

#_(pattern chat
           "something verb something IND something AND something IND something"
           [a ?, verb verb, b ?, in1 ind, c ?, xand3 "and", d ?, in2 ind, e ?]
           (if (not (ends-with (concat in1 c) (concat in2 e)))
             (let [x (! (concat a verb b in1 c))
                   y (! (concat a verb d in2 e))]
               (edge "r/+list" x y))))

#_(pattern chat
           "something verb something AND something IND something"
           [a ?, verb verb, b ?, xand4 "and", c ?, in ind, d ?]
           (let [x (! (concat a verb b in d))
                 y (! (concat a verb c in d))]
             (edge "r/+list" x y)))

#_(pattern chat
           "something verb something and something"
           [a ?, verb verb, b ?, xand5 "and", c ?]
           (let [x (concat a verb b)
                 y (concat a verb c)]
             (edge "r/+list" x y)))

(pattern chat
         "something verb IND something"
         [a ?, verb verb, in ind, b ?]
         (let [orig (! a)
               rel (rel (concat verb in))
               targ (! b)]
           (edge rel orig targ)))

(pattern chat
         "something verb TO something"
         [a ?, verb verb, to to, b ?]
         (let [orig (! a)
               rel (rel (concat verb to))
               targ (! b)]
           (edge rel orig targ)))

(pattern chat
         "something verb COMPAR IND something"
         [a ?, verb verb, compar compar, ind ind c ?]
         (let [orig (! a)
               rel (rel (concat verb compar ind))
               targ (! c)]
           (edge rel orig targ)))

(pattern chat
         "something verb something"
         [a ?, verb verb, c ?]
         (let [orig (! a)
               rel (rel verb)
               targ (! c)]
           (edge rel orig targ)))

(pattern chat
         "of"
         [a ?, xof "of", b verb]
         (let [x (! a)
               y (! b)]
           (eid "r/+of" (words->str a xof b) x y)))

(pattern chat
         "possessive with 's"
         [a ?, xs "'s", b ?]
         (let [x (! a)
               y (! b)]
           (eid "r/+poss" (str (words->str a) "'s " (words->str b)) x y)))

(pattern chat
         "action"
         [v verb, obj ?]
         (let [v-vert (! v)
               obj-vert (! obj)]
           (eid "r/+action"
                (str (words->str v) " " (words->str obj))
                v-vert obj-vert)))

(pattern chat
         "something in something"
         [a ?, xin "in", b !verb]
         (let [x (! a)
               y (! b)
               r (rel xin)]
           (edge r x y)))

(pattern chat
         "property"
         [prop (| adj adv), obj ?]
         (let [x (! prop)
               y (! obj)]
           (eid "r/+prop" (words->str prop obj) x y)))

(pattern chat
         "remove stuff in parenthesis"
         [a ?,
          x0 "-lrb-", b ?, x1 "-rrb-"]
         (! a))

(pattern chat
         "remove determinate article"
         [a det, no-dt ?]
         (! no-dt))

(pattern chat
         "remove full stop"
         [no-stop ?, stop "."]
         (! no-stop))

(pattern chat
         "the user"
         [i "i"]
         (user env))

(pattern chat
         "the current entity"
         [this "this"]
         (root env))

(pattern chat
         "object with no verb"
         [obj !verb]
         (entity obj))

(pattern chat
         "object"
         [obj ?]
         (entity obj))

(defn chat-test
  [s]
  (let [env {:root "f43806bb591e3b87/berlin", :user "u/telmo"}]
    (parse-str graphbrain.eco.parsers.chat/chat s env)))
