# !!!NOTE!!!
# This is experimental code, not yet ready to be used.


import time
import math
import graphbrain.constants as const
from graphbrain.funs import *


MAX_PROB = -7.
SIMILARITY_THRESHOLD = 0.7
MAX_COUNT = -1
STAR_LIMIT = 1000
MAX_WORDS = 50


class CandidateMetrics(object):
    def __init__(self):
        self.score = 0.
        self.degree = 0

    def better_than(self, other):
        if not isinstance(other, CandidateMetrics):
            return NotImplemented
        if self.score != other.score:
            return self.score > other.score
        elif self.degree != other.degree:
            return self.degree > other.degree
        else:
            return False

    def __str__(self):
        return 'score: {}; degree: {}'.format(self.score, self.degree)

    def __repr__(self):
        return self.__str__()


def synonyms(hg, symbol):
    syns1 = [edge[2] for edge
             in hg.pattern2edges((const.are_synonyms, symbol, None))
             if symbol_type(edge[2]) == SymbolType.CONCEPT]
    syns2 = [edge[1] for edge
             in hg.pattern2edges((const.are_synonyms, None, symbol))
             if symbol_type(edge[1]) == SymbolType.CONCEPT]
    syns3 = [edge[2] for edge
             in hg.pattern2edges((const.have_same_lemma, symbol, None))
             if symbol_type(edge[2]) == SymbolType.CONCEPT]
    syns4 = [edge[1] for edge
             in hg.pattern2edges((const.have_same_lemma, None, symbol))
             if symbol_type(edge[1]) == SymbolType.CONCEPT]
    return {symbol}.union(syns1).union(syns2).union(syns3).union(syns4)


def degree(hg, symbol):
    syns = synonyms(hg, symbol)
    total_degree = 0
    for syn in syns:
        total_degree += hg.degree(syn)
    return total_degree


def check_namespace(symbol, namespaces):
    symb_ns = symbol_namespace(symbol)
    if namespaces:
        for ns in namespaces:
            if symb_ns.startswith(ns):
                return True
        return False
    else:
        return True


class Disambiguation(object):
    def __init__(self, hg, parser):
        self.hg = hg
        self.parser = parser

        # profiling
        self.candidates = 0
        self.words1 = 0
        self.words2 = 0
        self.words_around_symbol_t = 0
        self.words_from_text_t = 0
        self.words_similarity_t = 0
        self.best_sense_t = 0

    def profile_string(self):
        return """
        words_around_symbol: {}; words_from_text: {};
        words_similarity: {}; best_sense: {}
        """.format(self.words_around_symbol_t, self.words_from_text_t,
                   self.words_similarity_t, self.best_sense_t).strip()

    def words_around_symbol(self, symbol):
        start = time.time()
        edges = self.hg.star(symbol, limit=STAR_LIMIT)
        words = set()
        for edge in edges:
            for entity in edge:
                for symbol in edge_symbols(entity):
                    term = symbol2str(symbol)
                    for token in term.split():
                        word = self.parser.make_word(token)
                        if (word.prob < MAX_PROB and
                                np.count_nonzero(word.vector) > 0):
                            words.add(word)

        self.words_around_symbol_t += time.time() - start
        return words

    def words_from_text(self, text):
        start = time.time()
        words = set()

        tokens = text.replace(':', ' ').replace(';', ' ').replace(',', ' ')\
            .replace('.', ' ').replace('?', ' ').replace('!', ' ').split()

        if 0 < MAX_WORDS <= len(tokens):
            tokens = tokens[:MAX_WORDS]

        for token in tokens:
            word = self.parser.make_word(token)
            if word.prob < MAX_PROB and np.count_nonzero(word.vector) > 0:
                words.add(word)

        self.words_from_text_t += time.time() - start
        return words

    def words_similarity(self, words1, words2, exclude):
        start = time.time()
        # print('sizes %s %s' % (len(words1), len(words2)))
        score = 0.
        count = 0
        for word1 in words1:
            for word2 in words2:
                if (word1.text not in exclude) or (word2.text not in exclude):
                    sim = word1.similarity(word2)
                    prob1 = math.exp(word1.prob)
                    prob2 = math.exp(word2.prob)
                    local_score = 0.
                    if sim > SIMILARITY_THRESHOLD:
                        local_score = 1. / (prob1 * prob2 * sim)
                    score += local_score
                    count += 1
                    if 0 < MAX_COUNT <= count:
                        return score

        self.words_similarity_t += time.time() - start
        return score

    def best_sense(self, roots, aux_text, namespaces=None):
        start = time.time()
        # reset profiling
        self.candidates = 0
        self.words1 = 0
        self.words2 = 0

        candidates = set()
        exclude = set()
        for root in roots:
            candidates = candidates.union(self.hg.symbols_with_root(root))
            text = symbol2str(root)
            for token in text.split():
                exclude.add(token)
        self.candidates = len(candidates)
        words1 = self.words_from_text(aux_text)
        self.words1 = len(words1)
        best = None
        best_cm = CandidateMetrics()
        for candidate in candidates:
            if check_namespace(candidate, namespaces):
                words2 = self.words_around_symbol(candidate)
                self.words2 += len(words1)
                cm = CandidateMetrics()
                cm.score = self.words_similarity(words1, words2, exclude)
                cm.degree = degree(self.hg, candidate)
                logging.info('%s %s' % (candidate, cm))
                if cm.better_than(best_cm):
                    best_cm = cm
                    best = candidate

        self.best_sense_t += time.time() - start
        return best, best_cm
