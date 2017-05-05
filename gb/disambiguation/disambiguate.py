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


import logging
import gb.hypergraph.hypergraph as hyperg
import gb.hypergraph.symbol as sym
import gb.hypergraph.edge as ed
import gb.knowledge.synonyms as ksyn
from gb.disambiguation.candidate_metrics import CandidateMetrics


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
    synonyms = ksyn.synonyms(hg, symbol)
    symbol_root = sym.root(symbol)
    prob = 1.
    for ngram in bag_of_words:
        ngram_symbol = sym.str2symbol(ngram)
        if not ((ngram in exclude) or is_part_of(ngram_symbol, symbol_root)):
            for synonym in synonyms:
                neighbors = connected_symbols_with_root(hg, synonym, ngram_symbol)
                for neighb in neighbors:
                    logging.info('neighbour found for %s: %s [%s]' % (symbol, neighb, neighbors[neighb]))
                    prob *= float(neighbors[neighb]) / float(total_degree)
    return prob


def candidate_metrics(hg, symbol, bag_of_words, exclude):
    cm = CandidateMetrics()
    cm.prob_meaning = probability_of_meaning(hg, symbol, bag_of_words, exclude)
    cm.degree = ksyn.degree(hg, symbol)
    return cm


def check_namespace(symbol, namespaces):
    symb_ns = sym.nspace(symbol)
    if namespaces:
        for ns in namespaces:
            if symb_ns.startswith(ns):
                return True
        return False
    else:
        return True


def disambiguate(hg, roots, bag_of_words, exclude, namespaces=None):
    candidates = set()
    for root in roots:
        candidates = candidates.union(hg.symbols_with_root(root))
    best = None
    best_cm = CandidateMetrics()
    for candidate in candidates:
        if check_namespace(candidate, namespaces):
            cm = candidate_metrics(hg, candidate, bag_of_words, exclude)
            logging.info('%s %s' % (candidate, cm))
            if cm.better_than(best_cm):
                best_cm = cm
                best = candidate

    return best, best_cm


if __name__ == '__main__':
    hgr = hyperg.HyperGraph({'backend': 'leveldb',
                             'hg': 'wikidata.hg'})
    bag_of_words1 = {'berlin', 'city'}
    print(disambiguate(hgr, 'berlin', bag_of_words1, ()))
    bag_of_words2 = {'berlin', 'car'}
    print(disambiguate(hgr, 'berlin', bag_of_words2, ()))
