from unidecode import unidecode
from itertools import combinations
import progressbar
from igraph import *
from graphbrain import *
from graphbrain.meaning.corefs import make_corefs


def clean_edge(edge):
    if not edge.is_atom():
        return edge
    catom = edge.root()
    catom = catom.replace('_', '')
    catom = unidecode(catom)
    return hedge(catom)


def belongs_to_clique(edge, clique):
    if edge.is_atom():
        return clean_atom(edge) in clique
    else:
        return all([clean_edge(x) in clique for x in edge[1:]])


def clique_number(edge, cliques):
    for i, clique in enumerate(cliques):
        if belongs_to_clique(edge, clique):
            return i
    return -1


def corefs_from_seed(hg, seed):
    concepts = []

    for edge in set(hg.edges_with_edges([seed])):
        conn = edge[0]
        if (conn.is_atom() and edge.type()[0] == 'c' and
                edge.connector_type() == 'b' and conn.root() == '+' and
                len(edge) > 2):
            mc = edge.main_concepts()
            if len(mc) == 1 and mc[0] == seed:
                d = hg.degree(edge)
                dd = hg.deep_degree(edge)
                if d > 2 and dd > 2:
                    concepts.append(edge)

    subconcepts = set()
    graph_edges = set()
    for concept in concepts:
        edge_concepts = set()
        for item in concept[1:]:
            if item != seed:
                edge_concepts.add(clean_edge(item))
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
        cliques.append(members + (clean_edge(seed),))

    coref_sets = tuple(set() for _ in cliques)
    for concept in concepts:
        cliquen = clique_number(concept, cliques)
        coref_sets[cliquen].add(concept)

    return coref_sets


def find_seeds(hg):
    seeds = set()

    print('finding seeds')
    total_non_atoms = hg.edge_count() - hg.atom_count()
    i = 0
    with progressbar.ProgressBar(max_value=total_non_atoms) as bar:
        for edge in hg.all_non_atoms():
            conn = edge[0]
            ct = edge.connector_type()
            if ct[0] == 'b' and conn.is_atom() and conn.root() == '+':
                if len(edge) > 2:
                    concepts = edge.main_concepts()
                    if (len(concepts) == 1 and
                            concepts[0].type()[:2] == 'cp'):
                        seeds.add(concepts[0])
            i += 1
            bar.update(i)
    return seeds


def coref_degree(hg, coref):
    return sum([hg.degree(edge) for edge in coref])


def coref_deep_degree(hg, coref):
    return sum([hg.deep_degree(edge) for edge in coref])


def root_deep_degree(hg, edge):
    if edge.is_atom():
        atoms = hg.atoms_with_root(edge.root())
        return sum([hg.deep_degree(atom) for atom in atoms])
    else:
        return hg.deep_degree(edge)


def lemma_degrees(hg, edge):
    if edge.is_atom():
        roots = {edge.root()}

        # find lemma
        for lemma_edge in hg.search(hedge((const.lemma_pred, edge, '*'))):
            roots.add(lemma_edge[2].root())

        # compute degrees
        d = 0
        dd = 0
        for r in roots:
            atoms = set(hg.atoms_with_root(r))
            d += sum([hg.degree(atom) for atom in atoms])
            dd += sum([hg.deep_degree(atom) for atom in atoms])

        return d, dd
    else:
        return hg.degree(ent), hg.deep_degree(ent)


def generate(hg):
    seeds = find_seeds(hg)
    count = 0
    i = 0
    print('processing seeds')
    with progressbar.ProgressBar(max_value=len(seeds)) as bar:
        for seed in seeds:
            crefs = corefs_from_seed(hg, seed)

            # check if the seed should be assigned to a synonym set
            if len(crefs) > 0:
                # find set with the highest degree and normalize set degrees by
                # total degree
                cref_degs = [coref_degree(hg, cref) for cref in crefs]
                total_deg = sum(cref_degs)
                cref_ratios = [cref_deg / total_deg for cref_deg in cref_degs]
                max_ratio = 0.
                best_pos = -1
                for pos, ratio in enumerate(cref_ratios):
                    if ratio > max_ratio:
                        max_ratio = ratio
                        best_pos = pos

                # compute some degree-related metrics
                sdd = coref_deep_degree(hg, crefs[best_pos])
                rdd = root_deep_degree(hg, seed)
                cref_to_root_dd = 0. if rdd == 0 else float(sdd) / float(rdd)
                d = hg.degree(seed)
                dd = hg.deep_degree(seed)
                r = float(d) / float(dd)
                ld, ldd = lemma_degrees(hg, seed)
                lr = float(ld) / float(ldd)

                # use metric to decide
                if (max_ratio >= .7 and r >= .05 and lr >= .05 and
                        cref_to_root_dd >= .1 and
                        (not seed.is_atom() or len(seed.root()) > 2)):

                    crefs[best_pos].add(seed)

                for cref in crefs:
                    for edge1, edge2 in combinations(cref, 2):
                        make_corefs(hg, edge1, edge2)
                        count += 1
            i += 1
            bar.update(i)
    return count
