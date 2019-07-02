from unidecode import unidecode
from itertools import combinations
import progressbar
from igraph import *
from graphbrain import *
from graphbrain.meaning.synonyms import make_synonyms


def clean_ent(ent):
    if is_edge(ent):
        return ent
    catom = root(ent)
    catom = catom.replace('_', '')
    catom = unidecode(catom)
    return catom


def belongs_to_clique(ent, clique):
    if is_atom(ent):
        return clean_atom(ent) in clique
    else:
        return all([clean_ent(x) in clique for x in ent[1:]])


def clique_number(ent, cliques):
    for i, clique in enumerate(cliques):
        if belongs_to_clique(ent, clique):
            return i
    return -1


def syns_from_seed(hg, seed):
    concepts = []

    for ent in set(hg.edges_with_ents([seed])):
        if entity_type(ent)[0] == 'c' and connector_type(ent) == 'b':
            if ent[0][0] == '+':
                if len(ent) > 2:
                    mc = main_concepts(ent)
                    if len(mc) == 1 and mc[0] == seed:
                        d = hg.degree(ent)
                        dd = hg.deep_degree(ent)
                        if d > 2 and dd > 2:
                            concepts.append(ent)

    subconcepts = set()
    graph_edges = set()
    for concept in concepts:
        edge_concepts = set()
        for item in concept[1:]:
            if item != seed:
                edge_concepts.add(clean_ent(item))
                subconcepts |= edge_concepts
                pairs = set(combinations(edge_concepts, 2))
                graph_edges |= pairs

    subconcepts = tuple(subconcepts)
    graph_edges = tuple((subconcepts.index(e[0]), subconcepts.index(e[1]))
                        for e in graph_edges)

    g = Graph()
    g.add_vertices(range(len(subconcepts)))
    g.add_edges(graph_edges)
    maxcliques = g.maximal_cliques()

    cliques = []
    for i, clique in enumerate(maxcliques):
        members = tuple(subconcepts[i] for i in clique)
        cliques.append(members + (clean_ent(seed),))

    syn_sets = tuple(set() for _ in cliques)
    for concept in concepts:
        cliquen = clique_number(concept, cliques)
        syn_sets[cliquen].add(concept)

    return syn_sets


def find_seeds(hg):
    seeds = set()

    print('finding seeds')
    ent_count = hg.atom_count() + hg.edge_count() + 1
    i = 0
    with progressbar.ProgressBar(max_value=ent_count) as bar:
        for edge in hg.all_edges():
            ct = connector_type(edge)
            if ct[0] == 'b' and edge[0][0] == '+':
                if len(edge) > 2:
                    concepts = main_concepts(edge)
                    if (len(concepts) == 1 and
                            entity_type(concepts[0])[:2] == 'cp'):
                        seeds.add(concepts[0])
            i += 1
            bar.update(i)
    return seeds


def syn_degree(hg, syn):
    return sum([hg.degree(ent) for ent in syn])


def syn_deep_degree(hg, syn):
    return sum([hg.deep_degree(ent) for ent in syn])


def root_deep_degree(hg, ent):
    if is_edge(ent):
        return hg.deep_degree(ent)

    atoms = hg.atoms_with_root(root(ent))
    return sum([hg.deep_degree(atom) for atom in atoms])


def lemma_degrees(hg, ent):
    if is_edge(ent):
        return hg.degree(ent), hg.deep_degree(ent)

    roots = {root(ent)}

    # find lemma
    for edge in hg.pat2ents((const.lemma_pred, ent, '*')):
        roots.add(root(edge[2]))

    # compute degrees
    d = 0
    dd = 0
    for r in roots:
        atoms = set(hg.atoms_with_root(r))
        d += sum([hg.degree(atom) for atom in atoms])
        dd += sum([hg.deep_degree(atom) for atom in atoms])

    return d, dd


def generate(hg):
    seeds = find_seeds(hg)
    count = 0
    i = 0
    print('processing seeds')
    with progressbar.ProgressBar(max_value=len(seeds)) as bar:
        for seed in seeds:
            syns = syns_from_seed(hg, seed)

            # check if the seed should be assigned to a synonym set
            if len(syns) > 0:
                # find set with the highest degree and normalize set degrees by
                # total degree
                syn_degs = [syn_degree(hg, syn) for syn in syns]
                total_deg = sum(syn_degs)
                syn_ratios = [syn_deg / total_deg for syn_deg in syn_degs]
                max_ratio = 0.
                best_pos = -1
                for pos, ratio in enumerate(syn_ratios):
                    if ratio > max_ratio:
                        max_ratio = ratio
                        best_pos = pos

                # compute some degree-related metrics
                sdd = syn_deep_degree(hg, syns[best_pos])
                rdd = root_deep_degree(hg, seed)
                syn_to_root_dd = 0. if rdd == 0 else float(sdd) / float(rdd)
                d = hg.degree(seed)
                dd = hg.deep_degree(seed)
                r = float(d) / float(dd)
                ld, ldd = lemma_degrees(hg, seed)
                lr = float(ld) / float(ldd)

                # use metric to decide
                if (max_ratio >= .7 and r >= .05 and lr >= .05 and
                        syn_to_root_dd >= .1 and
                        (is_edge(seed) or len(root(seed)) > 2)):

                    syns[best_pos].add(seed)

                    # print('\n++++++====== {} ======++++++'.format(seed))
                    # print('SEED SYNONYM: {}'.format(str(syns[best_pos])))
                    # print('root deep degree: {}'.format(rdd))
                    # print('syn/root ddegree: {}'.format(syn_to_root_dd))
                    # print('degree: {}; deep degree: {}; '
                    #       'ratio: {}'.format(d, dd, r))
                    # print('syn deep degree: {}'.format(sdd))
                    # print('lemma degree: {}; lemma deep degree: {};'
                    #       ' lemma ratio: {}'.format(ld, ldd, lr))
            if len(syns) > 1:
                for syn in syns:
                    for ent1, ent2 in combinations(syn, 2):
                        make_synonyms(hg, ent1, ent2)
                        count += 1
                # print(syns)
                # print(syn_ratios)
            i += 1
            bar.update(i)
    return count
