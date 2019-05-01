import operator
from collections import Counter
import progressbar
import igraph
from unidecode import unidecode
from graphbrain import *
import graphbrain.constants as const


MAX_PROB = -12
NORM_WEIGHT_THRESHOLD = .1
WEIGHT_THRESHOLD = 10


def edge2label(edge):
    if is_edge(edge):
        _edge = list(edge[:])
        if _edge[0] == '+':
            _edge = _edge[1:]
        if not is_edge(_edge[0]):
            if _edge[0][0] == '+':
                _edge[0] = _edge[0][1:]
        return ' '.join([edge2label(item) for item in _edge])
    else:
        return str(edge)


def is_candidate(edge):
    return True
    if is_edge(edge) and len(edge) > 1:
        # discard posessives
        if edge[1] in {"'s", 'in', 'of', 'with', 'and', 'a', 'on', 'for',
                       'to', 'from'}:
            return False
    return True


def next_candidate_pos(edges, pos):
    for i in range(pos + 1, len(edges)):
        if is_candidate(str2ent(edges[i][0])):
            return i
    return -1


def semantic_synonyms(source, target):
    if source == '(+ the %s)' % target:
        return True
    return False


def is_concept(edge):
    if is_edge(edge):
        if len(edge) > 1:
            for item in edge[1:]:
                if not is_concept(item):
                    return False
        return edge[0] == '+'
    return True


class Meronomy(object):
    def __init__(self, hg, parser):
        self.hg = hg
        self.parser = parser
        self.graph = None
        self.graph_edges = {}
        self.entities = set()
        self.atoms = {}
        self.edge_map = {}
        self.edge_counts = Counter()

        self.syn_ids = None
        self.synonym_sets = None
        self.cur_syn_id = 0

        self.lemmas = {}
        self.edge_strings = {}

    def lemmatize(self, edge):
        if edge in self.lemmas:
            return self.lemmas[edge]

        lemma = edge
        if is_edge(edge):
            lemma = tuple([self.lemmatize(item) for item in edge])
        else:
            edges = set(self.hg.pat2ents((const.lemma_pred, edge, '@')))
            if len(edges) > 0:
                lemma = edges.pop()[2]

        self.lemmas[edge] = lemma
        return lemma

    def edge2str(self, edge):
        if edge in self.edge_strings:
            return self.edge_strings[edge]

        s = ent2str(self.lemmatize(edge), roots_only=True)
        s = unidecode(s)
        s = s.replace('.', '')

        self.edge_strings[edge] = s

        return s

    # orig --[has part]--> targ
    def add_link(self, orig, targ):
        if (orig, targ) not in self.graph_edges:
            self.graph_edges[(orig, targ)] = 0.
        self.graph_edges[(orig, targ)] += 1.

    def add_edge(self, edge):
        if entity_type(edge)[0] == 'c':
            redge = roots(edge)

            # discard common words
            if is_atom(redge):
                word = self.parser.nlp.vocab[redge]
                if word.prob > MAX_PROB:
                    return False

            orig = self.edge2str(edge)

            # add to edge_map
            if orig not in self.edge_map:
                self.edge_map[orig] = set()
            self.edge_map[orig].add(edge)

            self.entities.add(orig)
            self.atoms[orig] = depth(edge)

            added = True
        else:
            orig = False
            added = False

        if is_edge(edge):
            # discard connector
            for entity in edge[1:]:
                targ = self.edge2str(entity)
                if targ:
                    if self.add_edge(entity):
                        if orig:
                            self.add_link(orig, targ)
                            self.edge_counts[entity] += 1
        return added

    # def post_assignments(self, edge):
    #     if is_edge(edge):
    #         for e in edge:
    #             self.post_assignments(e)
    #     else:
    #         term = self.edge2str(edge)
    #         if term in self.edge_map:
    #             if edge[-4:] == 'noun' or edge[-5:] == 'propn':
    #                 self.edge_map[term].add(edge)

    def generate(self):
        self.graph = igraph.Graph(directed=True)
        self.graph.add_vertices(list(self.entities))
        self.graph.add_edges(self.graph_edges.keys())
        self.graph.es['weight'] = list(self.graph_edges.values())

    def normalize_graph(self):
        for orig in self.graph.vs:
            edges = self.graph.incident(orig.index, mode='in')
            weights = [self.graph.es[edge]['weight'] for edge in edges]
            total = sum(weights)
            for edge in edges:
                self.graph.es[edge]['norm_weight'] = \
                    self.graph.es[edge]['weight'] / total

    def syn_id(self, atom):
        if atom in self.syn_ids:
            return self.syn_ids[atom]
        return None

    def new_syn_id(self):
        syn_id = self.cur_syn_id
        self.cur_syn_id += 1
        return syn_id

    def generate_synonyms(self):
        # init synonym data
        self.syn_ids = {}
        self.synonym_sets = {}
        self.cur_syn_id = 0

        total_atoms = len(self.atoms)

        # generate synonyms
        print('generating synonyms')
        i = 0
        with progressbar.ProgressBar(max_value=total_atoms) as bar:
            sorted_atoms = sorted(self.atoms.items(),
                                  key=operator.itemgetter(1),
                                  reverse=False)
            for atom_pair in sorted_atoms:
                orig = self.graph.vs.find(atom_pair[0])
                edges = self.graph.incident(orig.index, mode='in')
                edges = [self.graph.es[edge] for edge in edges]
                edges = [(self.graph.vs[edge.source]['name'],
                          self.graph.vs[edge.target]['name'],
                          edge['weight'],
                          edge['norm_weight']) for edge in edges]
                edges = sorted(edges, key=operator.itemgetter(3), reverse=True)

                ambiguous = False

                for pos in range(len(edges)):
                    is_synonym = False

                    edge = edges[pos]
                    source = edge[0]
                    target = edge[1]
                    weight = edge[2]
                    norm_weight = edge[3]

                    source_edge = str2ent(source)
                    if weight > WEIGHT_THRESHOLD:
                        if semantic_synonyms(source, target):
                            is_synonym = True
                        elif (not ambiguous and
                                norm_weight >= NORM_WEIGHT_THRESHOLD and
                                is_candidate(source_edge)):
                            pos_next = next_candidate_pos(edges, pos)
                            if pos_next < 0:
                                is_synonym = True
                            else:
                                next_weight = edges[pos_next][3]
                                if next_weight < NORM_WEIGHT_THRESHOLD:
                                    is_synonym = True
                                else:
                                    ambiguous = True

                    if is_synonym:
                        source_syn_id = self.syn_id(source)
                        target_syn_id = self.syn_id(target)

                        if target_syn_id:
                            self.syn_ids[source] = target_syn_id
                        elif source_syn_id:
                            self.syn_ids[target] = source_syn_id
                        else:
                            syn_id = self.new_syn_id()
                            self.syn_ids[source] = syn_id
                            self.syn_ids[target] = syn_id

                i += 1
                if (i % 1000) == 0:
                    bar.update(i)
            bar.update(i)

        # generate synonym sets
        print('generating synonym sets')
        i = 0
        with progressbar.ProgressBar(max_value=total_atoms) as bar:
            for atom in self.atoms:
                syn_id = self.syn_id(atom)
                if syn_id:
                    if syn_id not in self.synonym_sets:
                        self.synonym_sets[syn_id] = set()
                    self.synonym_sets[syn_id].add(atom)
                else:
                    new_id = self.new_syn_id()
                    self.syn_ids[atom] = new_id
                    self.synonym_sets[new_id] = {atom}
                i += 1
                if (i % 1000) == 0:
                    bar.update(i)
            bar.update(i)

    def synonym_label(self, syn_id, short=False):
        if short:
            best_size = 0
            best_edge = None
            for atom in self.synonym_sets[syn_id]:
                edge = str2edge(atom)
                if edge_size(edge) > best_size:
                    best_edge = edge
                    best_size = edge_size(edge)
            return edge2label(best_edge).replace('"', ' ')
        return '{%s}' % ', '.join([atom for atom in self.synonym_sets[syn_id]])

    def synonym_full_edges(self, syn_id):
        edges = set()
        for atom in self.synonym_sets[syn_id]:
            if atom in self.edge_map:
                edges = edges.union(self.edge_map[atom])
        return edges
