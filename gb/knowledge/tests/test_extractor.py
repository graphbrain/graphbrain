#   Copyright (c) 2016 CNRS - Centre national de la recherche scientifique.
#   All rights reserved.
#
#   Written by Telmo Menezes <telmo@telmomenezes.com>
#
#   This file is part of GraphBrain.
#
#   GraphBrain is free software: you can redistribute it and/or modify
#   it under the terms of the GNU Affero General Public License as published by
#   the Free Software Foundation, either version 3 of the License, or
#   (at your option) any later version.
#
#   GraphBrain is distributed in the hope that it will be useful,
#   but WITHOUT ANY WARRANTY; without even the implied warranty of
#   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#   GNU Affero General Public License for more details.
#
#   You should have received a copy of the GNU Affero General Public License
#   along with GraphBrain.  If not, see <http://www.gnu.org/licenses/>.


import unittest
from gb.nlp.parser import Parser
from gb.nlp.sentence import Sentence
from gb.knowledge.stage_alpha import alpha_transform


class TestTokenTree(unittest.TestCase):

    @classmethod
    def setUpClass(cls):
        cls.parser = Parser()

    def do_test(self, text, alpha):
        result = self.parser.parse_text(text)
        tree_alpha = alpha_transform(Sentence(result[0]))
        self.assertEqual(str(tree_alpha), alpha)

    def test_1(self):
        text = u"Some subspecies of mosquito might be 1st to be genetically wiped out."
        alpha = u"(might (be (some (of subspecies mosquito)) (1st (to (be (genetically (wiped out)))))))"
        self.do_test(text, alpha)

    def test_2(self):
        text = u"Telmo is going to the gym."
        alpha = u"(is (going telmo (to (the gym))))"
        self.do_test(text, alpha)

    def test_3(self):
        text = u"Due to its location in the European Plain, Berlin is influenced by a temperate seasonal climate."
        alpha = u"(is (by (influenced ((due to) (its (in location (the (european plain))))) berlin) " \
                u"(a (temperate (seasonal climate)))))"
        self.do_test(text, alpha)

    def test_4(self):
        text = u"Koikuchi shoyu, best known as soy sauce, is the mother of all sauces in Japan."
        alpha = u"(is ((koikuchi shoyu) (best (known (as (soy sauce))))) (the (of (in mother japan) (all sauces))))"
        self.do_test(text, alpha)

    def test_5(self):
        text = u"Sweden is the third-largest country in the European Union by area."
        alpha = u"(is sweden (the (third (largest (in (by country area) (the (european union)))))))"
        self.do_test(text, alpha)

    def test_6(self):
        text = u"Sweden wants to fight our disposable culture with tax breaks for repairing old stuff."
        alpha = u"(wants sweden (to (fight (our (disposable culture)) (with ((for (repairing (old stuff))) (tax breaks))))))"
        self.do_test(text, alpha)

    def test_7(self):
        text = u"OpenCola is a brand of open-source cola, where the instructions for making it are freely available " \
               u"and modifiable."
        alpha = u"(is opencola (a (of brand (((open source) cola) (where (are (the ((for making) instructions)) " \
                u"it (freely (and (available modifiable)))))))))"
        self.do_test(text, alpha)


if __name__ == '__main__':
    unittest.main()
