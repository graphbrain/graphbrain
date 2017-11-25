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


import operator
import progressbar
import igraph
from unidecode import unidecode
import gb.hypergraph.symbol as sym
import gb.hypergraph.edge as ed


MAX_PROB = -12
NORM_WEIGHT_THRESHOLD = .1
WEIGHT_THRESHOLD = 10


def edge2str(edge):
    s = ed.edge2str(edge, namespaces=False)
    s = unidecode(s)
    s = s.replace('.', '')
    return s


def edge2label(edge):
    if sym.is_edge(edge):
        _edge = list(edge[:])
        if _edge[0] == '+':
            _edge = _edge[1:]
        if not sym.is_edge(_edge[0]):
            if _edge[0][0] == '+':
                _edge[0] = _edge[0][1:]
        return ' '.join([edge2label(item) for item in _edge])
    else:
        return str(edge)


def is_candidate(edge):
    if sym.is_edge(edge) and len(edge) > 1:
        # discard posessives
        if edge[1] in {"'s", 'in', 'of', 'with', 'and', 'a', 'on', 'for', 'to', 'from'}:
            return False
    return True


def next_candidate_pos(edges, pos):
    for i in range(pos + 1, len(edges)):
        if is_candidate(ed.str2edge(edges[i][0])):
            return i
    return -1


def semantic_synonyms(source, target):
    if source == '(+ the %s)' % target:
        return True
    return False


def is_concept(edge):
    if sym.is_edge(edge):
        if len(edge) > 1:
            for item in edge[1:]:
                if not is_concept(item):
                    return False
        return edge[0] == '+'
    return True


class Meronomy(object):
    def __init__(self, parser):
        self.parser = parser
        self.graph = None
        self.graph_edges = {}
        self.vertices = set()
        self.atoms = {}
        self.edge_map = {}

        self.syn_ids = None
        self.synonym_sets = None
        self.cur_syn_id = 0

    # orig --[has part]--> targ
    def add_link(self, orig, targ):
        if (orig, targ) not in self.graph_edges:
            self.graph_edges[(orig, targ)] = 0.
        self.graph_edges[(orig, targ)] += 1.

    def add_edge(self, edge_ns):
        is_edge = sym.is_edge(edge_ns)
        edge = ed.without_namespaces(edge_ns)

        # discard common words
        if not is_edge:
            word = self.parser.make_word(edge)
            if word.prob > MAX_PROB:
                return False

        orig = edge2str(edge)

        # add to edge_map
        if orig not in self.edge_map:
            self.edge_map[orig] = set()
        self.edge_map[orig].add(edge_ns)

        concept = is_concept(edge)

        self.vertices.add(orig)
        self.atoms[orig] = ed.depth(edge)

        if is_edge:
            for e in edge_ns:
                targ = edge2str(e)
                if targ:
                    if self.add_edge(e):
                        if concept:
                            self.add_link(orig, targ)
        return True

    def recover_words(self, edge):
        if sym.is_edge(edge):
            for e in edge:
                self.recover_words(e)
        else:
            term = edge2str(edge)
            if term in self.edge_map:
                if edge[-4:] == 'noun' or edge[-5:] == 'propn':
                    self.edge_map[term].add(edge)

    def generate(self):
        self.graph = igraph.Graph(directed=True)
        self.graph.add_vertices(list(self.vertices))
        self.graph.add_edges(self.graph_edges.keys())
        self.graph.es['weight'] = list(self.graph_edges.values())

    def normalize_graph(self):
        for orig in self.graph.vs:
            edges = self.graph.incident(orig.index, mode='in')
            weights = [self.graph.es[edge]['weight'] for edge in edges]
            total = sum(weights)
            for edge in edges:
                self.graph.es[edge]['norm_weight'] = self.graph.es[edge]['weight'] / total

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
            sorted_atoms = sorted(self.atoms.items(), key=operator.itemgetter(1), reverse=False)
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

                    source_edge = ed.str2edge(source)
                    if weight >= WEIGHT_THRESHOLD:
                        if semantic_synonyms(source, target):
                            is_synonym = True
                        elif not ambiguous and norm_weight >= NORM_WEIGHT_THRESHOLD and is_candidate(source_edge):
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
                edge = ed.str2edge(atom)
                if ed.size(edge) > best_size:
                    best_edge = edge
                    best_size = ed.size(edge)
            return edge2label(best_edge).replace('"', ' ')
        return '{%s}' % ', '.join([atom for atom in self.synonym_sets[syn_id]])

    def synonym_full_edges(self, syn_id):
        edges = set()
        for atom in self.synonym_sets[syn_id]:
            if atom in self.edge_map:
                edges = edges.union(self.edge_map[atom])
        return edges
