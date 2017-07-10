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
import gb.hypergraph.edge as ed
from gb.nlp.enrich_edge import enrich_edge


def edge_min_prob(parser, edge):
    eedge = enrich_edge(parser, edge)
    probs = [word.prob for word in eedge['words'] if word.prob < -7]
    if len(probs) == 0:
        return 1.
    else:
        return min(probs)


def edge_x_similarity(parser, edge1, edge2):
    eedge1 = enrich_edge(parser, edge1)
    eedge2 = enrich_edge(parser, edge2)
    return eedge_x_similarity(eedge1, eedge2)


def eedge_x_similarity(eedge1, eedge2):
    words1 = eedge1['words']
    words2 = eedge2['words']
    words1 = [word for word in words1 if word.prob < -8]
    word_count = len(words1)
    if word_count == 0:
        return 0.
    words2 = [word for word in words2 if word.prob < -8]
    sims = {}
    total_weight = 0.
    for word1 in words1:
        weight = abs(word1.prob)
        total_weight += weight
        for word2 in words2:
            key = '%s_%s' % (word1, word2)
            sim = 1.
            if word1.similarity(word2) > .7:
                sim = math.exp(word1.prob) * math.exp(word2.prob)
            sims[key] = sim

    words1 = set([str(word) for word in words1])
    words2 = set([str(word) for word in words2])

    total_sim = 1.
    best_sim = 0.
    while len(words1) > 0 and len(words2) > 0:
        search = True
        while search:
            best_key = max(sims.keys(), key=(lambda k: sims[k]))
            best_pair = best_key.split('_')
            word1 = best_pair[0]
            word2 = best_pair[1]
            if word1 in words1 and word2 in words2:
                sim = sims[best_key]
                total_sim *= sim
                if sim > best_sim:
                    best_sim = sim
                search = False
                words1.remove(word1)
                words2.remove(word2)
            del sims[best_key]
    # return total_sim / total_weight
    return total_sim
    # return best_sim


def edge_similarity(parser, edge1, edge2):
    eedge1 = enrich_edge(parser, edge1)
    eedge2 = enrich_edge(parser, edge2)
    return eedge_similarity(eedge1, eedge2)


def edge_bi_similarity(parser, edge1, edge2):
    eedge1 = enrich_edge(parser, edge1)
    eedge2 = enrich_edge(parser, edge2)
    sim1, weight1 = eedge_similarity_with_weight(eedge1, eedge2)
    sim2, weight2 = eedge_similarity_with_weight(eedge2, eedge1)
    return (sim1 + sim2) / (weight1 + weight2)


def eedge_similarity(eedge1, eedge2):
    sim, weight = eedge_similarity_with_weight(eedge1, eedge2)
    return sim / weight


def eedge_similarity_with_weight(eedge1, eedge2):
    words1 = eedge1['words']
    words2 = eedge2['words']
    sims = {}
    total_weight = 0.
    for word1 in words1:
        total_weight += abs(word1.prob)
        for word2 in words2:
            key = '%s_%s' % (word1, word2)
            weight = abs(word1.prob)
            sim = word1.similarity(word2)
            if sim < 0.:
                sim = 0.
            sim = weight * sim
            sims[key] = sim

    words1 = set([str(word) for word in words1])
    words2 = set([str(word) for word in words2])

    total_sim = 0.
    while len(words1) > 0 and len(words2) > 0:
        search = True
        while search:
            best_key = max(sims.keys(), key=(lambda k: sims[k]))
            best_pair = best_key.split('_')
            word1 = best_pair[0]
            word2 = best_pair[1]
            if word1 in words1 and word2 in words2:
                total_sim += sims[best_key]
                search = False
                words1.remove(word1)
                words2.remove(word2)
            del sims[best_key]
    return total_sim, total_weight


def is_concept(eedge):
    first = eedge['eedge'][0]
    if 'symbol' in first:
        return first['symbol'][0] == '+'
    return False


def get_concepts(eedge):
    if 'symbol' in eedge:
        return [eedge]
    if is_concept(eedge):
        return [eedge]
    return eedge['eedge'][1:]


def concept2str(concept):
    if 'symbol' in concept:
        return concept['symbol']
    return ed.edge2str(concept['edge'])


def edge_concepts_similarity(eedge1, eedge2):
    concepts1 = get_concepts(eedge1)
    concepts2 = get_concepts(eedge2)

    sims = {}
    for concept1 in concepts1:
        for concept2 in concepts2:
            concept1_str = concept2str(concept1)
            concept2_str = concept2str(concept2)
            key = '%s_%s' % (concept1_str, concept2_str)
            sims[key] = {'sim': eedge_similarity(concept1, concept2),
                         'concept1': concept1_str,
                         'concept2': concept2_str}

    concepts1 = [concept2str(concept) for concept in concepts1]
    concepts2 = [concept2str(concept) for concept in concepts2]

    total_sim = 0.
    worst_sim = float('inf')
    matches = []
    while len(concepts1) > 0 and len(concepts2) > 0:
        search = True
        while search:
            best_key = max(sims.keys(), key=(lambda k: sims[k]['sim']))
            concept1 = sims[best_key]['concept1']
            concept2 = sims[best_key]['concept2']
            if concept1 in concepts1 and concept2 in concepts2:
                search = False
                matches.append((concept1, concept2))
                concepts1.remove(concept1)
                concepts2.remove(concept2)
                sim = sims[best_key]['sim']
                total_sim += sim
                if sim < worst_sim:
                    worst_sim = sim
            del sims[best_key]
    complete = len(concepts1) == 0
    return total_sim, worst_sim, complete, matches
