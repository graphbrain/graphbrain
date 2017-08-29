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
import igraph
import gb.tools.json as json_tools
import gb.hypergraph.symbol as sym
import gb.hypergraph.edge as ed
import gb.nlp.parser as par


MAX_PROB = -12


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


class Meronomy(object):
    def __init__(self, parser, claims):
        self.parser = parser
        self.graph = None
        self.edges = {}
        self.vertices = set()
        self.atoms = {}
        self.syn_ids = {}
        self.synonym_sets = {}
        self.cur_syn_id = 0
        self.init_graph(claims)

    def init_graph(self, claims):
        for claim in claims:
            self.add_claim(claim)
        self.graph = igraph.Graph(directed=True)
        self.graph.add_vertices(list(self.vertices))
        self.vertices = None
        self.graph.add_edges(self.edges.keys())
        self.graph.es['weight'] = list(self.edges.values())
        self.edges = None

    # orig --[has part]--> targ
    def add_link(self, orig, targ):
        if (orig, targ) not in self.edges:
            self.edges[(orig, targ)] = 0.
        self.edges[(orig, targ)] += 1.

    def edge2str(self, edge):
        s = ed.edge2str(edge, namespaces=False)
        if sym.is_edge(edge):
            return s

        if s[0] == '+':
            s = s[1:]

        if len(s) == 0:
            return None

        word = self.parser.make_word(s)
        if word.prob < MAX_PROB:
            return s

        return None

    def add_claim(self, edge):
        orig = self.edge2str(edge)
        if not orig:
            return
        self.vertices.add(orig)
        self.atoms[orig] = ed.depth(edge)
        if sym.is_edge(edge):
            for element in edge:
                targ = self.edge2str(element)
                if targ:
                    self.vertices.add(targ)
                    self.atoms[targ] = ed.depth(element)
                    self.add_link(orig, targ)
                self.add_claim(element)

    def normalize_graph(self):
        for orig in self.graph.vs:
            edges = self.graph.incident(orig.index, mode='in')
            total = sum([self.graph.es[edge]['weight'] for edge in edges])
            for edge in edges:
                self.graph.es[edge]['weight'] = self.graph.es[edge]['weight'] / total

    def syn_id(self, atom):
        if atom in self.syn_ids:
            return self.syn_ids[atom]
        return None

    def valid_synonym_parent(self, parent):
        orig = self.graph.vs.find(parent)
        edges = self.graph.incident(orig.index, mode='out')
        edges = [self.graph.es[edge] for edge in edges]
        targets = [self.graph.vs[edge.target]['name'] for edge in edges]
        syn_ids = [self.syn_id(target) for target in targets]
        syn_ids = [syn_id for syn_id in syn_ids if syn_id]
        return len(set(syn_ids)) < 2

    def synonym_ids_in(self, edge):
        sids = set()
        atom = self.edge2str(edge)
        atom_syn_id = self.syn_id(atom)
        if atom_syn_id:
            sids.add(atom_syn_id)
        if sym.is_edge(edge):
            for element in edge:
                atom = self.edge2str(element)
                atom_syn_id = self.syn_id(atom)
                if atom_syn_id:
                    sids.add(atom_syn_id)
                sids = sids.union(self.synonym_ids_in(element))
        return sids

    def new_syn_id(self):
        syn_id = self.cur_syn_id
        self.cur_syn_id += 1
        return syn_id

    def generate_synonyms(self):
        sorted_atoms = sorted(self.atoms.items(), key=operator.itemgetter(1), reverse=False)
        for atom_pair in sorted_atoms:
            orig = self.graph.vs.find(atom_pair[0])
            edges = self.graph.incident(orig.index, mode='in')
            if len(edges) > 0:
                max_weight = max([self.graph.es[e]['weight'] for e in edges])
            else:
                max_weight = 0.
            if max_weight > .1:
                for e in edges:
                    edge = self.graph.es[e]
                    if edge['weight'] == max_weight:
                        source = self.graph.vs[edge.source]['name']
                        target = self.graph.vs[edge.target]['name']
                        source_syn_id = self.syn_id(source)
                        target_syn_id = self.syn_id(target)

                        if not (source_syn_id and target_syn_id):
                            if self.valid_synonym_parent(source):
                                if source_syn_id:
                                    self.syn_ids[target] = source_syn_id
                                elif target_syn_id:
                                    self.syn_ids[source] = target_syn_id
                                else:
                                    syn_id = self.new_syn_id()
                                    self.syn_ids[source] = syn_id
                                    self.syn_ids[target] = syn_id
                            else:
                                if not target_syn_id:
                                    syn_id = self.new_syn_id()
                                    self.syn_ids[target] = syn_id

        # filter out multiple synonyms
        delete_synonyms = set()
        for atom in self.syn_ids:
            if len(self.synonym_ids_in(ed.str2edge(atom))) > 1:
                delete_synonyms.add(self.syn_ids[atom])

        # generate synonym sets
        for atom in self.atoms:
            syn_id = self.syn_id(atom)
            if syn_id:
                if syn_id in delete_synonyms:
                    new_id = self.new_syn_id()
                    self.syn_ids[atom] = new_id
                    self.synonym_sets[new_id] = {atom}
                else:
                    if syn_id not in self.synonym_sets:
                        self.synonym_sets[syn_id] = set()
                    self.synonym_sets[syn_id].add(atom)
            else:
                new_id = self.new_syn_id()
                self.syn_ids[atom] = new_id
                self.synonym_sets[new_id] = {atom}

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


if __name__ == '__main__':
    print('creating parser...')
    par = par.Parser()
    print('parser created.')

    # read data
    # edge_data = json_tools.read('edges_similar_concepts.json')
    edge_data = json_tools.read('all.json')

    # build extra edges list
    full_edges = []
    for it in edge_data:
        full_edges.append(ed.without_namespaces(ed.str2edge(it['edge'])))

    # build meronomy
    print('creating meronomy...')
    mer = Meronomy(par, full_edges)
    mer.normalize_graph()
    print('meronomy created.')

    # generate synonyms
    mer.generate_synonyms()
    for synid in mer.synonym_sets:
        synonym_set = mer.synonym_sets[synid]
        if len(synonym_set) > 0:
            print('syn_set #%s' % synid)
            print(synonym_set)
