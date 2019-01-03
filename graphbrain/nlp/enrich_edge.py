import math
from graphbrain.funs import *


def enrich_edge(parser, edge):
    if is_edge(edge):
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

    ngram = symbol2str(edge)
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
