;   Copyright (c) 2016 CNRS - Centre national de la recherche scientifique.
;   All rights reserved.
;
;   Written by Telmo Menezes <telmo@telmomenezes.com>
;
;   This file is part of GraphBrain.
;
;   GraphBrain is free software: you can redistribute it and/or modify
;   it under the terms of the GNU Affero General Public License as published by
;   the Free Software Foundation, either version 3 of the License, or
;   (at your option) any later version.
;
;   GraphBrain is distributed in the hope that it will be useful,
;   but WITHOUT ANY WARRANTY; without even the implied warranty of
;   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;   GNU Affero General Public License for more details.
;
;   You should have received a copy of the GNU Affero General Public License
;   along with GraphBrain.  If not, see <http://www.gnu.org/licenses/>.

(ns graphbrain.eco.parsers.chat
  (:use graphbrain.eco.eco
        graphbrain.eco.ecofuns))

(ecoparser chat)

(pattern :normal chat
         "(object) [rel] (object)"
         [x0 "-lcb-", a ?, x1 "-rcb-",
          x2 "-lsb-", r ?, x3 "-rsb-"
          x4 "-lcb-", b ?, x5 "-rcb-"]
         (let [orig (! a)
               targ (! b)
               rel (rel r)]
           (edge rel orig targ)))

(pattern :top chat
         "x verb y: z"
         [x ???
          v verb
          y ???
          collon ":"
          z ???]
         (let [r (str (words->id v)
                      "_"
                      (words->id y)
                      "/eco")
               owner (! x)
               class (! y)
               thing (! z)
               e1 (edge r owner thing)
               e2 (edge "is/eco" thing class)]
           (edges e1 e2)))

(pattern :normal chat
         "have to -> task"
         [actor !verb
          have "have"
          to "to"
          task ?]
         (let [r "has_task/eco"
               actor (! actor)
               task (! task)]
           (edge r actor task)))

(pattern :normal chat
         "with a"
         [xwith "with", x1 "a", a ?, x2 ",", b ?, verb verb, c ?]
         (let [x (! a)
               y (! b)
               z (! (concat b verb c))
               k ["has/eco" y x]]
           (edge "list/eco" k z)))

(pattern :normal chat
         "something verb something AND verb something"
         [a ?, verb1 verb, b ?, xand1 "and", verb2 verb, c ?]
         (let [x (concat a verb1 b)
               y (concat a verb2 c)]
           (edge "list/eco" (! x) (! y))))

(pattern :normal chat
         "something verb something IND something AND something IND something"
         [a ?, verb verb, b ?, in1 ind, c ?, xand3 "and", d ?, in2 ind, e ?]
         (if (not (ends-with (concat in1 c) (concat in2 e)))
           (let [x (! (concat a verb b in1 c))
                 y (! (concat a verb d in2 e))]
             (edge "list/eco" x y))))

(pattern :normal chat
         "something verb something AND something IND something"
         [a ?, verb verb, b ?, xand4 "and", c ?, in ind, d ?]
         (let [x (! (concat a verb b in d))
               y (! (concat a verb c in d))]
           (edge "list/eco" x y)))

(pattern :normal chat
         "something verb something and something"
         [a ?, verb verb, b ?, xand5 "and", c ?]
         (let [x (concat a verb b)
               y (concat a verb c)]
           (edge "list/eco" x y)))

(pattern :normal chat
         "something verb IND something"
         [a ?, verb verb, in ind, b ?]
         (let [orig (! a)
               rel (rel (concat verb in))
               targ (! b)]
           (edge rel orig targ)))

(pattern :normal chat
         "something verb TO something"
         [a ?, verb verb, to to, b ?]
         (let [orig (! a)
               rel (rel (concat verb to))
               targ (! b)]
           (edge rel orig targ)))

(pattern :normal chat
         "something verb COMPAR IND something"
         [a ?, verb verb, compar compar, ind ind c ?]
         (let [orig (! a)
               rel (rel (concat verb compar ind))
               targ (! c)]
           (edge rel orig targ)))

(pattern :normal chat
         "something verb something"
         [a ?, verb verb, c ?]
         (let [orig (! a)
               rel (rel verb)
               targ (! c)]
           (edge rel orig targ)))

#_(pattern :normal chat
         "of"
         [a ?, xof "of", b verb]
         (let [x (! a)
               y (! b)]
           (eid "r/+of" (words->str a xof b) x y)))

#_(pattern :normal chat
         "possessive with 's"
         [a ?, xs "'s", b ?]
         (let [x (! a)
               y (! b)]
           (eid "r/+poss" (str (words->str a) "'s " (words->str b)) x y)))

#_(pattern :normal chat
         "action"
         [v verb, obj ?]
         (let [v-vert (! v)
               obj-vert (! obj)]
           (eid "r/+action"
                (str (words->str v) " " (words->str obj))
                v-vert obj-vert)))

(pattern :normal chat
         "something in something"
         [a ?, xin "in", b !verb]
         (let [x (! a)
               y (! b)
               r (rel xin)]
           (edge r x y)))

#_(pattern :normal chat
         "property"
         [prop (| adj adv), obj ?]
         (let [x (! prop)
               y (! obj)]
           (eid "r/+prop" (words->str prop obj) x y)))

(pattern :normal chat
         "remove stuff in parenthesis"
         [a ?,
          x0 "-lrb-", b ?, x1 "-rrb-"]
         (! a))

(pattern :normal chat
         "remove 'the' determinate article"
         [a (& det "the")
          no-dt ?]
         (! no-dt))

(pattern :normal chat
         "remove 'a' determinate article"
         [a (& det "a")
          no-dt ?]
         (! no-dt))

(pattern :normal chat
         "remove 'an' determinate article"
         [a (& det "an")
          no-dt ?]
         (! no-dt))

(pattern :normal chat
         "remove full stop"
         [no-stop ?, stop "."]
         (! no-stop))

(pattern :normal chat
         "the user -- I"
         [i "i"]
         (user env))

(pattern :normal chat
         "the user -- me"
         [i "me"]
         (user env))

(pattern :normal chat
         "the current entity -- this"
         [this "this"]
         (root env))

(pattern :normal chat
         "the current entity -- he"
         [this "he"]
         (root env))

(pattern :normal chat
         "the current entity -- she"
         [this "she"]
         (root env))

(pattern :normal chat
         "object with no verb"
         [obj !verb]
         (entity obj))

(pattern :normal chat
         "object"
         [obj ?]
         (entity obj))

(defn chat-test
  [s]
  (let [env {:root "f43806bb591e3b87/berlin", :user "u/telmo"}]
    (parse-str graphbrain.eco.parsers.chat/chat s env)))
