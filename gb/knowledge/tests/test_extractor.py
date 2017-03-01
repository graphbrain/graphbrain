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
from gb.knowledge.extractor import Extractor


class TestExtractor(unittest.TestCase):

    @classmethod
    def setUpClass(cls):
        cls.extractor = Extractor(None, stages=('alpha', 'beta-simple', 'gamma', 'delta', 'epsilon'))

    def do_test(self, text, expected_stage_outputs):
        results = self.extractor.read_text(text)
        self.assertEqual(len(results), 1)
        for i in range(len(expected_stage_outputs)):
            self.assertEqual(str(self.extractor.outputs[i]), expected_stage_outputs[i])

    def test_1(self):
        text = u"Some subspecies of mosquito might be 1st to be genetically wiped out."
        alpha = u"(might (be (some (of subspecies mosquito)) (1st (to (be (genetically (wiped out)))))))"
        beta = u"(might (be (some (of subspecies mosquito)) (1st (to (be (genetically (wiped out)))))))"
        gamma = u"(might_be (some (of subspecies mosquito)) (1st (to_be_genetically wiped_out)))"
        delta = u"(might_be (some (of subspecies mosquito)) (1st (to_be_genetically wiped_out)))"
        self.do_test(text, (alpha, beta, gamma, delta))

    def test_2(self):
        text = u"Telmo is going to the gym."
        alpha = u"(is (going telmo (to (the gym))))"
        beta = u"(is (going telmo (to (the gym))))"
        gamma = u"(is_going telmo (to gym))"
        delta = u"(is_going_to telmo gym)"
        self.do_test(text, (alpha, beta, gamma, delta))

    def test_3(self):
        text = u"Due to its location in the European Plain, Berlin is influenced by a temperate seasonal climate."
        alpha = u"(is (by (influenced ((due to) (its (in location (the (european plain))))) berlin) " \
                u"(a (temperate (seasonal climate)))))"
        beta = u"(is (by (influenced ((due to) (its (in location (the european_plain)))) berlin) " \
               u"(a (temperate (seasonal climate)))))"
        gamma = u"(is_by (influenced (due_to (its (in location european_plain))) berlin) (temperate " \
                u"(seasonal climate)))"
        delta = u"((influenced_due_to is_by) (its (in location european_plain)) berlin (temperate (seasonal climate)))"
        self.do_test(text, (alpha, beta, gamma, delta))

    def test_4(self):
        text = u"Koikuchi shoyu, best known as soy sauce, is the mother of all sauces in Japan."
        alpha = u"(is ((koikuchi shoyu) (best (known (as (soy sauce))))) (the (of (in mother japan) (all sauces))))"
        beta = u"(is (koikuchi_shoyu (best (known (as soy_sauce)))) (the (of (in mother japan) (all sauces))))"
        gamma = u"(is (koikuchi_shoyu (best_known_as soy_sauce)) (of (in mother japan) (all sauces)))"
        delta = u"((is of) (koikuchi_shoyu (best_known_as soy_sauce)) (in mother japan) (all sauces))"
        self.do_test(text, (alpha, beta, gamma, delta))

    def test_5(self):
        text = u"Sweden is the third-largest country in the European Union by area."
        alpha = u"(is sweden (the (third (largest (in (by country area) (the (european union)))))))"
        beta = u"(is sweden (the (third (largest (in (by country area) (the european_union))))))"
        gamma = u"(is sweden (third (largest (in (by country area) european_union))))"
        delta = u"(is_third sweden (largest (in (by country area) european_union)))"
        self.do_test(text, (alpha, beta, gamma, delta))

    def test_6(self):
        text = u"Sweden wants to fight our disposable culture with tax breaks for repairing old stuff."
        alpha = u"(wants sweden (to (fight (our (disposable culture)) (with ((for (repairing (old stuff))) " \
                u"(tax breaks))))))"
        beta = u"(wants sweden (to (fight (our (disposable culture)) (with ((for " \
               u"(repairing (old stuff))) tax_breaks)))))"
        gamma = u"(wants sweden (to_fight (our (disposable culture)) (with ((for_repairing (old stuff)) tax_breaks))))"
        delta = u"((wants_to_fight with) sweden (our (disposable culture)) ((for_repairing (old stuff)) tax_breaks))"
        self.do_test(text, (alpha, beta, gamma, delta))

    def test_7(self):
        text = u"OpenCola is a brand of open-source cola, where the instructions for making it are freely available " \
               u"and modifiable."
        alpha = u"(is opencola (a (of brand (((open source) cola) (where (are (the ((for making) instructions)) " \
                u"it (freely (and (available modifiable)))))))))"
        beta = u"(is opencola (a (of brand (((open source) cola) (where (are (the ((for making) instructions)) " \
               u"it (freely (and (available modifiable)))))))))"
        gamma = u"(is opencola (of brand (((open source) cola) (where_are (for_making instructions) it " \
                u"(freely (and (available modifiable)))))))"
        delta = u"((is of) opencola brand ((open source cola) (where_are (for_making instructions) it " \
                u"(freely (and (available modifiable))))))"
        self.do_test(text, (alpha, beta, gamma, delta))

    def test_8(self):
        text = u"2016 Nobel Prize in Physiology or Medicine Is Awarded to Yoshinori Ohsumi."
        alpha = u"(is (awarded (2016 (in (nobel prize) (or (physiology medicine)))) (to (yoshinori ohsumi))))"
        beta = u"(is (awarded (2016 (in nobel_prize (or (physiology medicine)))) (to yoshinori_ohsumi)))"
        gamma = u"(is_awarded (2016 (in nobel_prize (or (physiology medicine)))) (to yoshinori_ohsumi))"
        delta = u"(is_awarded_to (2016 (in nobel_prize (or (physiology medicine)))) yoshinori_ohsumi)"
        self.do_test(text, (alpha, beta, gamma, delta))

if __name__ == '__main__':
    unittest.main()
