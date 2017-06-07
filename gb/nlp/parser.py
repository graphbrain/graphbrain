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


import spacy
from gb.nlp.word import Word
from gb.nlp.token import Token
from gb.nlp.sentence import Sentence


class Parser:
    """Generic NLP parser."""

    def __init__(self):
        self.token_table = {}
        self.parser = spacy.load('en')

    def __spacy2token(self, stoken, depth=0):
        if stoken is None:
            return None
        elif stoken not in self.token_table:
            token = Token()
            token.word = stoken.orth_.lower().strip()
            token.lemma = stoken.lemma_
            token.shape = stoken.shape_
            token.logprob = stoken.prob
            token.pos = stoken.pos_
            token.dep = stoken.dep_
            token.tag = stoken.tag_
            token.vector = stoken.vector
            if depth < 20:
                token.left_children = [self.__spacy2token(t, depth + 1) for t in stoken.lefts]
                token.right_children = [self.__spacy2token(t, depth + 1) for t in stoken.rights]
            else:
                token.left_children = []
                token.right_children = []
            for t in token.left_children:
                t.parent = token
            for t in token.right_children:
                t.parent = token
            token.entity_type = stoken.ent_type_
            self.token_table[stoken] = token

        return self.token_table[stoken]

    def make_word(self, text):
        word = Word()
        word.text = text.lower()
        sword =  self.parser.vocab[word.text]
        word.prob = sword.prob
        word.vector = sword.vector
        word.sword = sword
        return word

    def parse_text(self, text):
        parsed_data = self.parser(text)

        self.token_table = {}

        sents = []
        for span in parsed_data.sents:
            sentence_text = text[span.start_char:span.end_char].strip()
            token_seq = [self.__spacy2token(parsed_data[i]) for i in range(span.start, span.end)]
            for i in range(len(token_seq)):
                token_seq[i].position_in_sentence = i
            sents.append((sentence_text, token_seq))

        return sents


if __name__ == '__main__':
    test_text = u"""
    Alan Mathison Turing was a pioneering English computer scientist, mathematician, logician, cryptanalyst and
    theoretical biologist. He was highly influential in the development of theoretical computer science, providing a
    formalisation of the concepts of algorithm and computation with the Turing machine, which can be considered a model
    of a general purpose computer. Turing is widely considered to be the father of theoretical computer science
    and artificial intelligence.
    The psychologist George Kelley (1955) noted that humans do not enter a world that is inherently structured; we must
    give the world a structure that we ourselves create.
    Some subspecies of mosquito might be 1st to be genetically wiped out.
    """

    print('Starting parser...')
    parser = Parser()
    print('Parsing...')
    result = parser.parse_text(test_text)

    for r in result:
        sentence = Sentence(r[1])
        print(r[0])
        sentence.print_tree()
        print('')
