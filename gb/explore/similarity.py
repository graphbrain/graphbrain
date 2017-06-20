import math
import operator
import json
import gb.hypergraph.hypergraph as hyperg
import gb.hypergraph.symbol as sym
import gb.hypergraph.edge as ed
import gb.nlp.parser as par


EXCLUDE_RELS = ['are_synonyms/gb', 'src/gb', 'have_same_lemma/gb']


def edge_similarity(parser, edge1, edge2, best=False):
    eedge1 = enrich_edge(parser, edge1)
    eedge2 = enrich_edge(parser, edge2)
    return eedge_similarity(eedge1, eedge2, best)


def eedge_similarity(eedge1, eedge2, best=False):
    words1 = eedge1['words']
    words2 = eedge2['words']
    sims = {}
    best_sim = 0.
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
            if sim > best_sim:
                best_sim = sim
            sims[key] = sim

    if best:
        return best_sim

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
    return total_sim / total_weight


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


def exclude(edge):
    if sym.is_edge(edge):
        rel = edge[0]
        if sym.is_edge(rel):
            return False
        return rel in EXCLUDE_RELS
    else:
        return True


def write_edge_data(edge_data, file_path):
    f = open(file_path, 'w')
    for e in edge_data:
        f.write('%s\n' % json.dumps(e, separators=(',', ':')))
    f.close()


class Similarity(object):
    def __init__(self, hg, parser, sim_threshold=.7):
        self.hg = hg
        self.parser = parser
        self.sim_threshold = sim_threshold

    def similar_edges(self, targ_edge):
        edges = self.hg.all()

        targ_eedge = enrich_edge(self.parser, targ_edge)

        sims = {}
        for edge in edges:
            if edge != targ_edge and not exclude(edge):
                eedge = enrich_edge(self.parser, edge)
                total_sim = eedge_similarity(targ_eedge, eedge)
                if total_sim >= self.sim_threshold:
                    sims[ed.edge2str(edge)] = total_sim

        sorted_edges = sorted(sims.items(), key=operator.itemgetter(1), reverse=True)

        result = []
        for e in sorted_edges:
            edge_data = {'edge': e[0],
                         'sim': e[1],
                         'text': self.hg.get_str_attribute(ed.str2edge(e[0]), 'text')}
            result.append(edge_data)
        return result

    def edges_with_similar_concepts(self, targ_edge):
        edges = self.hg.all()

        targ_eedge = enrich_edge(self.parser, targ_edge)

        sims = {}
        for edge in edges:
            if edge != targ_edge and not exclude(edge):
                eedge = enrich_edge(self.parser, edge)
                total_sim, worst_sim, complete, matches = edge_concepts_similarity(targ_eedge, eedge)
                if complete and worst_sim >= self.sim_threshold:
                    sims[ed.edge2str(edge)] = (worst_sim, total_sim, matches)

        sorted_edges = sorted(sims.items(), key=operator.itemgetter(1), reverse=True)

        result = []
        for e in sorted_edges:
            edge_data = {'edge': e[0],
                         'worst_sim': e[1][0],
                         'sim': e[1][1],
                         'matches': e[1][2],
                         'text': self.hg.get_str_attribute(ed.str2edge(e[0]), 'text')}
            result.append(edge_data)
        return result

    def write_similar_edges(self, targ_edge, file_path):
        edge_data = self.similar_edges(targ_edge)
        write_edge_data(edge_data, file_path)

    def write_edges_with_similar_concepts(self, targ_edge, file_path):
        edge_data = self.edges_with_similar_concepts(targ_edge)
        write_edge_data(edge_data, file_path)


if __name__ == '__main__':
    hgr = hyperg.HyperGraph({'backend': 'leveldb', 'hg': 'reddit-politics.hg'})

    print('creating parser...')
    par = par.Parser()
    print('parser created.')

    te = '(clinches/nlp.clinch.verb clinton/nlp.clinton.noun ' \
         '(+/gb democratic/nlp.democratic.adj nomination/nlp.nomination.noun))'

    s = Similarity(hgr, par)
    # s.write_edges_with_similar_concepts(ed.str2edge(te), 'edges_similar_concepts.json')
    s.write_similar_edges(ed.str2edge(te), 'similar_edges.json')
