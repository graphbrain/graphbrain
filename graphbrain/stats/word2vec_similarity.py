from graphbrain.funs import *
from graphbrain.nlp.enrich_edge import enrich_edge


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
    return edge2str(concept['edge'])


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


def similarity(parser, edge1, edge2):
    eedge1 = enrich_edge(parser, edge1)
    eedge2 = enrich_edge(parser, edge2)
    return edge_concepts_similarity(eedge1, eedge2)
