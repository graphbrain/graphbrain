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


import gb.hypergraph.hypergraph as hyperg
from gb.nlp.parser import Parser
from gb.nlp.sentence import Sentence
from gb.knowledge.stages.alpha import AlphaStage
from gb.knowledge.stages.beta import BetaStage
from gb.knowledge.stages.beta_simple import BetaStageSimple
from gb.knowledge.stages.gamma import GammaStage
from gb.knowledge.stages.delta import DeltaStage
from gb.knowledge.stages.epsilon import EpsilonStage


class Extractor(object):
    def __init__(self, hg, stages=('alpha', 'beta', 'gamma', 'delta', 'epsilon')):
        self.hg = hg
        self.stages = stages
        self.parser = None
        self.debug = False
        self.outputs = []
        self.bag_of_words = None

    def create_stage(self, name, tree):
        if name == 'alpha':
            return AlphaStage()
        elif name == 'beta':
            return BetaStage(self.hg, self.bag_of_words, tree)
        elif name == 'beta-simple':
            return BetaStageSimple(tree)
        elif name == 'gamma':
            return GammaStage(tree)
        elif name == 'delta':
            return DeltaStage(tree)
        elif name == 'epsilon':
            return EpsilonStage(self.hg, tree)
        else:
            raise RuntimeError('unknnown stage name: %s' % name)

    def debug_msg(self, msg):
        if self.debug:
            print(msg)

    def generate_bag_of_words(self, sentences):
        word_list = []
        for sentence in sentences:
            for token in sentence:
                word_list += [token.word.lower(), token.lemma.lower()]
        self.bag_of_words = set(word_list)

    def read_text(self, text):
        if self.parser is None:
            self.debug_msg('creating parser...')
            self.parser = Parser()
        sents = self.parser.parse_text(text)
        self.generate_bag_of_words(sents)
        return [self.read_sentence(Sentence(sent)) for sent in sents]

    def read_sentence(self, sentence):
        self.debug_msg('parsing sentence: %s' % sentence)
        if self.debug:
            sentence.print_tree()

        self.outputs = []

        stage = self.create_stage(self.stages[0], None)
        self.debug_msg('executing %s stage...' % self.stages[0])
        last_stage_output = stage.process_sentence(sentence)
        output = str(last_stage_output)
        self.outputs.append(output)
        self.debug_msg(output)

        for name in self.stages[1:]:
            stage = self.create_stage(name, last_stage_output)
            self.debug_msg('executing %s stage...' % name)
            last_stage_output = stage.process()
            output = str(last_stage_output)
            self.outputs.append(output)
            self.debug_msg(output)

        return last_stage_output


if __name__ == '__main__':
    # test_text = "Due to its location in the European Plain, Berlin is influenced by a temperate seasonal climate."
    test_text = "Mulholland Drive is a movie by David Lynch."

    print(test_text)

    hgraph = hyperg.HyperGraph({'backend': 'leveldb',
                                'hg': 'wikidata.hg'})
    extractor = Extractor(hgraph)
    extractor.debug = True
    extractor.read_text(test_text)
