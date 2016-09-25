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
from gb.knowledge.tokentree import TokenTree


class TestTokenTree(unittest.TestCase):

    @classmethod
    def setUpClass(cls):
        cls.parser = Parser()

    def do_test(self, text, expected):
        result = self.parser.parse_text(text)
        tree = TokenTree(Sentence(result[0]))
        self.assertEqual(str(tree), expected)

    def test_1(self):
        text = u"Some subspecies of mosquito might be 1st to be genetically wiped out."
        expected = u"(might_be (some (of subspecies mosquito)) (1st (to_be_genetically_wiped out)))"
        self.do_test(text, expected)

    def test_2(self):
        text = u"Telmo is going to the gym."
        expected = u"(is_going_to telmo (the gym))"
        self.do_test(text, expected)

    def test_3(self):
        text = u"Due to its location in the European Plain, Berlin is influenced by a temperate seasonal climate."
        expected = u"(due_to___is_influenced_by (its (in location (the european_plain))) berlin " \
                   u"(a temperate_seasonal_climate))"
        self.do_test(text, expected)

    def test_4(self):
        text = u"OpenCola is a brand of open-source cola, where the instructions for making it are freely available " \
               u"and modifiable."
        expected = u"(is opencola (a (of brand ((where_are (the (for_making instructions)) it " \
                   u"(freely (and available modifiable))) open_source_cola))))"
        self.do_test(text, expected)

    def test_5(self):
        text = u"Koikuchi shoyu, best known as soy sauce, is the mother of all sauces in Japan."
        expected = u"(is (koikuchi_shoyu (best_known_as soy_sauce)) (the (of___in mother (all sauces) japan)))"
        self.do_test(text, expected)


if __name__ == '__main__':
    unittest.main()
