from unidecode import unidecode
from itertools import combinations
import progressbar
from igraph import Graph
from graphbrain import hedge
from graphbrain.meaning.corefs import make_corefs_ops
from graphbrain.agents.agent import Agent


def clean_edge(edge):
    if not edge.is_atom():
        return edge
    catom = edge.root()
    catom = catom.replace('_', '')
    catom = unidecode(catom)
    return hedge(catom)


def belongs_to_clique(edge, clique):
    if edge.is_atom():
        return clean_edge(edge) in clique
    else:
        return all([clean_edge(x) in clique for x in edge[1:]])


def clique_number(edge, cliques):
    for i, clique in enumerate(cliques):
        if belongs_to_clique(edge, clique):
            return i
    return -1


class CorefsNames(Agent):
    def __init__(self):
        super().__init__()
        self.corefs = 0
        self.seeds = None

    def name(self):
        return 'corefs_names'

    def _corefs_from_seed(self, seed):
        hg = self.system.get_hg(self)

        concepts = []

        for edge in set(hg.edges_with_edges([seed])):
            conn = edge[0]
            if (conn.is_atom() and edge.type()[0] == 'C' and
                    edge.connector_type() == 'B' and conn.root() == '+' and
                    len(edge) > 2):
                mc = edge.main_concepts()
                if len(mc) == 1 and mc[0] == seed:
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

    def on_start(self):
        self.corefs = 0
        self.seeds = set()

    def input_edge(self, edge):
        if not edge.is_atom():
            conn = edge[0]
            ct = edge.connector_type()
            if ct[0] == 'B' and conn.is_atom() and conn.root() == '+':
                if len(edge) > 2:
                    concepts = edge.main_concepts()
                    if (len(concepts) == 1 and
                            concepts[0].type()[:2] == 'Cp'):
                        self.seeds.add(concepts[0])

    def report(self):
        return '{} coreferences were added.'.format(str(self.corefs))

    def on_end(self):
        hg = self.system.get_hg(self)

        i = 0
        print('processing seeds')
        with progressbar.ProgressBar(max_value=len(self.seeds)) as bar:
            for seed in self.seeds:
                crefs = self._corefs_from_seed(seed)

                # check if the seed should be assigned to a synonym set
                if len(crefs) > 0:
                    # find set with the highest degree and normalize set
                    # degrees by total degree
                    cref_degs = [hg.sum_deep_degree(cref) for cref in crefs]
                    total_deg = sum(cref_degs)
                    cref_ratios = [cref_deg / total_deg
                                   for cref_deg in cref_degs]
                    max_ratio = 0.
                    best_pos = -1
                    for pos, ratio in enumerate(cref_ratios):
                        if ratio > max_ratio:
                            max_ratio = ratio
                            best_pos = pos

                    dd = hg.deep_degree(seed)

                    # ensure that the seed is used by itself
                    if total_deg < dd:
                        # print('<###>')
                        # print(seed)
                        # print(crefs)
                        # print(set(hg.star(seed)))
                        # print('max_ratio: {}'.format(max_ratio))
                        # print('total coref dd: {}'.format(total_deg))
                        # print('seed dd: {}'.format(dd))

                        # add seed if coreference set is sufficiently dominant
                        if max_ratio >= .7:
                            crefs[best_pos].add(seed)

                    for cref in crefs:
                        for edge1, edge2 in combinations(cref, 2):
                            self.corefs += 1
                            for op in make_corefs_ops(hg, edge1, edge2):
                                yield op
                i += 1
                bar.update(i)
