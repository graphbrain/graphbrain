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
import gb.hypergraph.symbol as sym
import gb.hypergraph.edge as ed


def term_neighborhood(hg, symbol, neighb, max_depth=1, depth=0):
    if hg.degree(symbol) > 2500:
        return

    if depth > max_depth:
        return

    edges = hg.star(symbol)
    for edge in edges:
        symbs = ed.symbols(edge)
        for symb in symbs:
            term_neighborhood(hg, symb, neighb, max_depth, depth + 1)
            if (symb not in neighb) or (neighb[symb]['depth'] > depth):
                neighb[symb] = {'degree': hg.degree(symb), 'depth': depth}


def degree_by_depth(neighb):
    degrees = {}
    for symb in neighb:
        depth = neighb[symb]['depth']
        if depth in degrees:
            degrees[depth] += 1
        else:
            degrees[depth] = 1
    return degrees


def assign_probabilities(neighb, total_degree, degrees):
    for symb in neighb:
        degree = float(neighb[symb]['degree'])
        prob = (degree / float(total_degree)) * float(degrees[neighb[symb]['depth']])
        neighb[symb]['prob'] = prob


def probability_of_meaning(symbol, text):
    neighb = {}
    term_neighborhood(hg, symbol, neighb)
    degrees = degree_by_depth(neighb)
    assign_probabilities(neighb, hg.total_degree(), degrees)

    prob = 1.
    for symb in neighb:
        term = sym.symbol2str(sym.root(symb))
        if term in text:
            prob *= neighb[symb]['prob']
    return prob


def disambiguate(hg, root, text):
    candidates = hg.symbols_with_root(root)
    min_prob = float('inf')
    best = None
    for candidate in candidates:
        prob = probability_of_meaning(candidate, text)
        if prob < min_prob:
            min_prob = prob
            best = candidate

    if min_prob >= 1.:
        max_degree = -1
        best = None
        for candidate in candidates:
            degree = hg.degree(candidate)
            if degree > max_degree:
                max_degree = degree
                best = candidate

    return best


if __name__ == '__main__':
    hg = hyperg.HyperGraph({'backend': 'leveldb',
                            'hg': 'wordnet.hg'})
    print(disambiguate(hg, 'space', 'space is the place'))
    print(disambiguate(hg, 'space', 'going to outer space'))
