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
from gb.nlp.parser import Sentence


def add_ngrams(token, ngrams, lemma):
    if token.is_word():
        if lemma:
            ngrams.add(token.lemma)
        else:
            ngrams.add(token.word)
        left = [add_ngrams(child, ngrams, lemma) for child in token.left_children]
        right = [add_ngrams(child, ngrams, lemma) for child in token.right_children]
        tokens = left
        if lemma:
            tokens.append(token.lemma)
        else:
            tokens.append(token.word)
        tokens = tokens + right
        tokens = [tok for tok in tokens if tok]
        ngram = ' '.join(tokens)
        ngrams.add(ngram)
        return ngram
    return None


def generate(text, parser):
    ngrams = set()
    results = parser.parse_text(text)
    for result in results:
        sentence = Sentence(result[1])
        add_ngrams(sentence.root(), ngrams, True)
        add_ngrams(sentence.root(), ngrams, False)
    return ngrams


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
    p = Parser()
    print('Parsing...')
    print(generate(test_text, p))
