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
import itertools
import igraph
import scipy.sparse as sps
from sklearn.preprocessing import normalize
import gb.tools.json as json_tools
import gb.hypergraph.symbol as sym
import gb.hypergraph.edge as ed
import gb.nlp.parser as par


MAX_PROB = -12


class AtomGroups(object):
    def __init__(self, parser):
        self.parser = parser
        self.edge_counts = None
        self.atoms = None
        self.atom_set = None
        self.sorted_atoms = None
        self.synonym_sets = None
        self.synonym_map = None
        self.atom_groups = None
        self.atom_group_clusters = None

    def add_edges(self, edge):
        if sym.is_edge(edge):
            for item in edge:
                self.add_edges(item)

        edge_str = ed.edge2str(edge, namespaces=False)
        if not sym.is_edge(edge):
            if edge_str[0] == '+':
                edge_str = edge_str[1:]
            if len(edge_str) == 0:
                return
            if not edge_str[0].isalnum():
                return
            if self.parser.make_word(edge_str).prob > MAX_PROB:
                return
        if edge_str not in self.edge_counts:
            self.edge_counts[edge_str] = 0
        self.edge_counts[edge_str] += 1

    def set_edges(self, edges):
        # build edge counts map, with inner edges
        self.edge_counts = {}
        for edge in edges:
            self.add_edges(edge)

    def generate_atoms(self):
        # create atoms map -- edges with more than one occurrence
        self.atoms = {}
        for key in self.edge_counts:
            if self.edge_counts[key] > 1:
                self.atoms[key] = ed.depth(ed.str2edge(key))

        # build atom_set
        self.atom_set = set([atom for atom in self.atoms])

        # sorted by depth
        self.sorted_atoms = sorted(self.atoms.items(), key=operator.itemgetter(1), reverse=False)

    def generate_synonyms(self):
        # find atoms and parent-child synonym relationships
        children = {}
        for i in range(len(self.sorted_atoms)):
            parents = []
            satom1 = self.sorted_atoms[i][0]
            atom1 = ed.str2edge(satom1)
            cur_depth = self.sorted_atoms[i][1]
            start = i
            while start < len(self.sorted_atoms) and self.sorted_atoms[start][1] <= cur_depth:
                start += 1
            for j in range(start, len(self.sorted_atoms)):
                satom2 = self.sorted_atoms[j][0]
                atom2 = ed.str2edge(satom2)
                if atom1 in atom2:
                    parents.append(satom2)
            if len(parents) == 1:
                satom2 = parents[0]
                if satom2 not in children:
                    children[satom2] = []
                children[satom2].append(satom1)
                if satom1 in children:
                    children[satom2] += children[satom1]

        # build synonym sets
        self.synonym_sets = []
        for satom in children:
            synonyms = [satom] + children[satom]
            count = 0
            for synonym in synonyms:
                count += self.edge_counts[synonym]
                del self.atoms[synonym]
            self.synonym_sets.append({'edges': synonyms,
                                      'count': count,
                                      'index': len(self.synonym_sets)})
            print(synonyms)

        for atom in self.atoms:
            self.synonym_sets.append({'edges': [atom],
                                      'count': self.edge_counts[atom],
                                      'index': len(self.synonym_sets)})

        # build synonym map
        self.synonym_map = {}
        for sset in self.synonym_sets:
            for synonym in sset['edges']:
                self.synonym_map[synonym] = sset['index']
            self.synonym_map[sset['index']] = sset

    def find_co_synonyms(self, edge):
        co_syns = set()
        if sym.is_edge(edge):
            for item in edge:
                co_syns = co_syns.union(self.find_co_synonyms(item))

        edge_str = ed.edge2str(edge, namespaces=False)

        for atom in self.atom_set:
            if atom == edge_str:
                co_syns.add(self.synonym_map[atom])
                return co_syns

        return co_syns

    def generate_atom_groups(self):
        nsyns = len(self.synonym_sets)

        # build coocurrence sparse matrix
        synonym_cooc = sps.lil_matrix((nsyns, nsyns))
        for edge in extra_edges:
            co_synonyms = self.find_co_synonyms(edge)
            if len(co_synonyms) > 1:
                for pair in itertools.combinations(co_synonyms, 2):
                    synonym_cooc[pair[0], pair[1]] += 1
                    synonym_cooc[pair[1], pair[0]] += 1

        # normalize matrix
        synonym_cooc = normalize(synonym_cooc, norm='l1', axis=1, copy=False)

        # iterate matrix, build graph
        gedges = []
        weights = []
        cx = synonym_cooc.tocoo()
        for i, j, v in zip(cx.row, cx.col, cx.data):
            gedges.append((i, j))
            weights.append(v)
        g = igraph.Graph()
        g.add_vertices(nsyns)
        g.add_edges(gedges)
        g.es['weight'] = weights

        # community detection
        comms = igraph.Graph.community_multilevel(g, weights='weight', return_levels=False)

        # build atom_groups
        self.atom_groups = {}
        for i in range(len(comms)):
            comm = comms[i]
            count = 0
            syns = []
            sentences = set()
            edges = []
            for item in comm:
                edges += self.synonym_map[item]['edges']

                for atom in self.synonym_map[item]['edges']:
                    for edat in edge_data:
                        if ed.contains(ed.str2edge(ed.edge2str(ed.str2edge(edat['edge']), namespaces=False)),
                                       ed.str2edge(atom),
                                       deep=True):
                            if edat['text']:
                                sentences.add(edat['text'])
                syns.append(self.synonym_map[item])
                count += self.synonym_map[item]['count']
            label = ', '.join(edges)
            atom_group = {'label': label,
                          'syns': syns,
                          'count': count,
                          'sentences': sentences,
                          'edges': edges}
            self.atom_groups[i] = atom_group

    def print_atom_groups(self):
        n = 0
        for k in self.atom_groups:
            atom_group = self.atom_groups[k]
            size = len(atom_group['sentences'])
            if size > 3:
                n += 1
                print('ATOM_GROUP id: %s' % n)
                print('Base concepts: %s' % atom_group['label'])
                print('size: %s' % size)
                print('sentences:')
                for sentence in atom_group['sentences']:
                    print('* %s' % sentence)
                print('edges:')
                for edge in atom_group['edges']:
                    print('* %s' % ed.edge2str(ed.without_namespaces(ed.str2edge(edge))))
                print()

    def atom_groups_present_in(self, edge):
        group_indices = set()
        for i in range(len(self.atom_groups)):
            for atom_edge in self.atom_groups[i]['edges']:
                if ed.contains(edge, atom_edge, deep=True):
                    group_indices.add(i)
        return group_indices

    def find_co_atom_groups(self, edge):
        agps = [self.atom_groups_present_in(element) for element in edge]
        agps = [agp for agp in agps if len(agp) > 0]
        if len(agps) < 2:
            return []
        groups = itertools.product(*agps)
        pairs = []
        for group in groups:
            pairs += itertools.combinations(group, 2)
        return pairs

    def generate_atom_group_clusters(self, edges):
        # build atom_group coocurrence sparse matrix
        nag = len(self.atom_groups)
        ag_cooc = sps.lil_matrix((nag, nag))
        for edge in edges:
            edge = ed.without_namespaces(edge)
            co_ags = self.find_co_atom_groups(edge)
            for pair in co_ags:
                ag_cooc[pair[0], pair[1]] += 1
                ag_cooc[pair[1], pair[0]] += 1

        # normalize matrix
        ag_cooc = normalize(ag_cooc, norm='l1', axis=1, copy=False)

        # iterate matrix, build graph
        gedges = []
        weights = []
        cx = ag_cooc.tocoo()
        for i, j, v in zip(cx.row, cx.col, cx.data):
            gedges.append((i, j))
            weights.append(v)
        g = igraph.Graph()
        g.add_vertices(nag)
        g.add_edges(gedges)
        g.es['weight'] = weights

        # community detection
        comms = igraph.Graph.community_multilevel(g, weights='weight', return_levels=False)

        # build atom_group_clusters
        self.atom_group_clusters = {}
        for i in range(len(comms)):
            comm = comms[i]
            labels = []
            for item in comm:
                labels.append('[%s]{%s}' % (self.atom_groups[item]['label'], self.atom_groups[item]['count']))
            label = ' + '.join(labels)
            atom_group_cluster = {'label': label}
            self.atom_group_clusters[i] = atom_group_cluster

    def print_atom_group_clusters(self):
        for k in self.atom_group_clusters:
            print('AG_CLUSTER: %s' % self.atom_group_clusters[k]['label'])


if __name__ == '__main__':
    print('creating parser...')
    par = par.Parser()
    print('parser created.')

    # read data
    # edge_data = json_tools.read('edges_similar_concepts.json')

    # build extra edges list
    # extra_edges = []
    # full_edges = []
    # for it in edge_data:
    #     e = ed.str2edge(it['edge'])
    #     full_edges.append(e)
    #     matched = [ed.str2edge(match[1]) for match in it['matches']]
    #     for part in e[1:]:
    #         if part not in matched:
    #             extra_edges.append(part)

    edge_data = json_tools.read('all.json')
    # build full edges list
    extra_edges = []
    for it in edge_data:
        extra_edges.append(ed.without_namespaces(ed.str2edge(it['edge'])))
    full_edges = extra_edges

    ag = AtomGroups(par)
    print('set edges')
    ag.set_edges(extra_edges)
    print('generate_atoms')
    ag.generate_atoms()
    print('generate synonyms')
    ag.generate_synonyms()
    print('generate atom groups')
    ag.generate_atom_groups()
    ag.print_atom_groups()
    print('generate atom group clusters')
    ag.generate_atom_group_clusters(full_edges)
    ag.print_atom_group_clusters()
