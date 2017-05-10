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
import numpy as np
import gb.hypergraph.hypergraph as hyperg
import gb.hypergraph.symbol as sym
import gb.hypergraph.edge as ed
import gb.nlp.parser as par
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


def disambiguate2(hg, roots, bag_of_words, exclude, namespaces=None):
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


def words_around_symbol(hg, parser, symbol):
    edges = hg.star(symbol)
    words = set()
    for edge in edges:
        for entity in edge[1:]:
            for symbol in ed.symbols(entity):
                term = sym.symbol2str(symbol)
                for token in term.split():
                    words.add(parser.make_word(token))
    return words


def words_from_text(parser, text):
    words = set()

    tokens = text.replace(':', ' ').replace(';', ' ').replace(',', ' ').replace('.', ' ').replace('?', ' ')\
        .replace('!', ' ').split()

    for token in tokens:
        words.add(parser.make_word(token))
    return words


def words_similarity(words1, words2):
    MAX_PROB = -5.
    THRES = 0.5
    total_sim = 0.
    max_sim = -1.
    count = 0
    above = 0
    for word1 in words1:
        if word1.prob < MAX_PROB and np.count_nonzero(word1.vector) > 0:
            for word2 in words2:
                if word2.prob < MAX_PROB and np.count_nonzero(word2.vector) > 0:
                    sim = word1.similarity(word2)
                    if sim > max_sim:
                        max_sim = sim
                    total_sim += sim
                    count += 1
                    if sim > THRES:
                        above += sim
    if count == 0:
        return 0, 0, 0, 0, 0
    mean_sim = total_sim / float(count)
    return mean_sim, max_sim, total_sim, count, above


def disambiguate(hg, parser, roots, aux_text, namespaces=None):
    candidates = set()
    exclude = set()
    for root in roots:
        candidates = candidates.union(hg.symbols_with_root(root))
        text = sym.symbol2str(root)
        for token in text.split():
            exclude.add(token)
    words1 = [word for word in words_from_text(parser, aux_text) if word.text not in exclude]
    best = None
    best_score = CandidateMetrics()
    for candidate in candidates:
        if check_namespace(candidate, namespaces):
            words2 = [word for word in words_around_symbol(hg, parser, candidate) if word.text not in exclude]
            mean_sim, max_sim, total_sim, count, above = words_similarity(words1, words2)
            # score = above
            score = CandidateMetrics()
            score.prob_meaning = -above
            score.degree = ksyn.degree(hg, candidate)
            logging.info('%s %s' % (candidate, score))
            # print('%s %s' % (candidate, score))
            if score.better_than(best_score):
                best_score = score
                best = candidate

    return best, best_score


if __name__ == '__main__':
    hgr = hyperg.HyperGraph({'backend': 'leveldb',
                             'hg': 'wordnet_wikidata.hg'})
    p = par.Parser()

    r = "cambridge"
    text1 = "Cambridge in England."
    text2 = "Cambridge near Boston."

    print(disambiguate(hgr, p, [r], text1))
    print(disambiguate(hgr, p, [r], text2))
