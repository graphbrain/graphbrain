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


from gb.nlp.parser import Parser
from gb.nlp.sentence import Sentence
from gb.knowledge.stages.alpha import AlphaStage
from gb.knowledge.stages.beta import BetaStage
from gb.knowledge.stages.gamma import GammaStage


class Extractor(object):
    def __init__(self, hg, alpha='default', beta='default', gamma='default'):
        self.hg = hg
        self.alpha = alpha
        self.beta = beta
        self.gamma = gamma
        self.parser = None
        self.debug = False
        self.alpha_output = None
        self.beta_output = None
        self.gamma_output = None

    def debug_msg(self, msg):
        if self.debug:
            print(msg)

    def create_alpha_stage(self):
        if self.alpha == 'default':
            return AlphaStage()
        else:
            raise RuntimeError('unknnown alpha stage type: %s' % self.alpha)

    def create_beta_stage(self, tree):
        if self.beta == 'default':
            return BetaStage(self.hg, tree)
        else:
            raise RuntimeError('unknnown beta stage type: %s' % self.beta)

    def create_gamma_stage(self, tree):
        if self.gamma == 'default':
            return GammaStage(tree)
        else:
            raise RuntimeError('unknnown gamma stage type: %s' % self.gamma)

    def read_text(self, text):
        if self.parser is None:
            self.debug_msg('creating parser...')
            self.parser = Parser()
        sents = self.parser.parse_text(text)
        return [self.read_sentence(Sentence(sent)) for sent in sents]

    def read_sentence(self, sentence):
        self.debug_msg('parsing sentence: %s' % sentence)
        if self.debug:
            sentence.print_tree()

        alpha_stage = self.create_alpha_stage()
        self.debug_msg('executing alpha stage...')
        tree = alpha_stage.process_sentence(sentence)
        self.alpha_output = str(tree)
        self.debug_msg(self.alpha_output)

        beta_stage = self.create_beta_stage(tree)
        self.debug_msg('executing beta stage...')
        tree = beta_stage.process()
        self.beta_output = str(tree)
        self.debug_msg(self.beta_output)

        gamma_stage = self.create_gamma_stage(tree)
        self.debug_msg('executing gamma stage...')
        tree = gamma_stage.process()
        self.gamma_output = str(tree)
        self.debug_msg(self.gamma_output)
        return tree


if __name__ == '__main__':
    test_text = """
    Koikuchi shoyu, best known as soy sauce, is the mother of all sauces in Japan.
    """

    print(test_text)

    hgraph = None
    extractor = Extractor(hgraph)
    extractor.debug = True
    extractor.read_text(test_text)
