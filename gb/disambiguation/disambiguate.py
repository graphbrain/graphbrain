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


def term_neighborhood(hg, symbol, neighb, max_depth=0, depth=0):
    if len(sym.root(symbol)) <= 3:
        return

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


def is_part_of(root, main_root):
    if root == main_root:
        return True
    root_parts = main_root.split('_')
    for part in root_parts:
        if root == part:
            return True
    return False


def probability_of_meaning(hg, symbol, bag_of_words, exclude):
    print('probability_of_meaning: %s' % symbol)
    neighb = {}
    term_neighborhood(hg, symbol, neighb)
    degrees = degree_by_depth(neighb)
    assign_probabilities(neighb, hg.total_degree(), degrees)

    symbol_root = sym.root(symbol)
    prob = 1.
    for symb in neighb:
        symb_root = sym.root(symb)
        if not is_part_of(symb_root, symbol_root):
            term = sym.symbol2str(symb_root)
            if (term in bag_of_words) and (term not in exclude):
                print('+ %s %s' % (term, symb))
                prob *= neighb[symb]['prob']
    return prob


def disambiguate(hg, root, bag_of_words, exclude):
    candidates = hg.symbols_with_root(root)
    min_prob = float('inf')
    best = None
    for candidate in candidates:
        prob = probability_of_meaning(hg, candidate, bag_of_words, exclude)
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

    return best, min_prob


if __name__ == '__main__':
    hgr = hyperg.HyperGraph({'backend': 'leveldb',
                             'hg': 'wikidata.hg'})
    bag_of_words1 = {'berlin', 'city'}
    print(disambiguate(hgr, 'berlin', bag_of_words1, ()))
    bag_of_words2 = {'berlin', 'car'}
    print(disambiguate(hgr, 'berlin', bag_of_words2, ()))
