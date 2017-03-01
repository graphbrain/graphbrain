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


def is_part_of(root, main_root):
    if root == main_root:
        return True
    root_parts = main_root.split('_')
    for part in root_parts:
        if root == part:
            return True
    return False


def connected_symbols_with_root(hg, symbol, root):
    symbols = {}
    edges = hg.edges_with_symbols((symbol,), root=root)
    for edge in edges:
        symbs = ed.symbols(edge)
        for symb in symbs:
            if sym.root(symb) == root:
                if symb not in symbols:
                    symbols[symb] = hg.degree(symb)
    return symbols


def probability_of_meaning(hg, symbol, bag_of_words, exclude):
    total_degree = hg.total_degree()
    symbol_root = sym.root(symbol)
    prob = 1.
    for ngram in bag_of_words:
        ngram_symbol = sym.str2symbol(ngram)
        if not ((ngram in exclude) or is_part_of(ngram_symbol, symbol_root)):
            neighbors = connected_symbols_with_root(hg, symbol, ngram_symbol)
            for neighb in neighbors:
                prob *= float(neighbors[neighb]) / float(total_degree)
    return prob


def disambiguate(hg, roots, bag_of_words, exclude):
    candidates = set()
    for root in roots:
        candidates = candidates.union(hg.symbols_with_root(root))
    min_prob = float('inf')
    best = None
    best_degree = -1
    for candidate in candidates:
        prob = probability_of_meaning(hg, candidate, bag_of_words, exclude)
        if prob < min_prob:
            min_prob = prob
            best = candidate
            best_degree = hg.degree(candidate)
        elif prob == min_prob:
            deg = hg.degree(candidate)
            if deg > best_degree:
                best = candidate
                best_degree = deg

    return best, min_prob


if __name__ == '__main__':
    hgr = hyperg.HyperGraph({'backend': 'leveldb',
                             'hg': 'wikidata.hg'})
    bag_of_words1 = {'berlin', 'city'}
    print(disambiguate(hgr, 'berlin', bag_of_words1, ()))
    bag_of_words2 = {'berlin', 'car'}
    print(disambiguate(hgr, 'berlin', bag_of_words2, ()))
