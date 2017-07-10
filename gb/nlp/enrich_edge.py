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


import math
import gb.hypergraph.symbol as sym


def enrich_edge(parser, edge):
    if sym.is_edge(edge):
        eedge = [enrich_edge(parser, item) for item in edge]
        prob = 1.
        total_prob = 0.
        word_count = 0
        words = []
        for item in eedge:
            word_count += item['word_count']
            prob *= item['prob']
            total_prob += item['prob'] * item['word_count']
            words += item['words']
        mean_prob = total_prob / word_count
        return {'edge': edge, 'eedge': eedge, 'words': words, 'prob': prob, 'word_count': word_count,
                'mean_prob': mean_prob}

    ngram = sym.symbol2str(edge)
    tokens = [token for token in ngram.split(' ') if len(token) > 0]
    for i in range(len(tokens)):
        if tokens[i][0] == '+':
            tokens[i] = tokens[i][1:]
    tokens = [token for token in tokens if len(token) > 0]
    words = [parser.make_word(token) for token in tokens]
    prob = 1.
    total_prob = 0.
    for word in words:
        p = math.exp(word.prob)
        prob *= p
        total_prob += p
    word_count = len(words)
    if word_count > 0:
        mean_prob = total_prob / word_count
    else:
        mean_prob = 1.
    return {'symbol': edge, 'words': words, 'prob': prob, 'word_count': word_count, 'mean_prob': mean_prob}
