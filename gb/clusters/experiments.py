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
import gb.explore.similarity as simil


MAX_PROB = -12


def co_weight(co_occ, eedge1, eedge2):
    weight = 0
    for word1 in eedge1['words']:
        for word2 in eedge2['words']:
            key = '%s %s' % (word1, word2)
            if key in co_occ:
                weight += co_occ[key]
    return weight


def weight2(eedge1, eedge2):
    weight = 0
    for word1 in eedge1['words']:
        for word2 in eedge2['words']:
            if word1.prob < MAX_PROB and word2.prob < MAX_PROB and word1.similarity(word2) > .8:
                weight += abs(min(word1.prob, word2.prob))
    return weight


def eedge2str(eedge):
    if 'symbol' in eedge:
        return eedge['symbol']
    else:
        return ed.edge2str(eedge['edge'], namespaces=False)


def eedge2edge(eedge):
    if 'symbol' in eedge:
        return eedge['symbol']
    else:
        return eedge['edge']


def edge_contains(edge, concept, deep=False):
    if sym.is_edge(edge):
        for x in edge:
            if x == concept:
                return True
            if deep:
                if edge_contains(x, concept, True):
                    return True
        return False
    else:
        return edge == concept


def add_edges(parser, edge_counts, edge):
    if sym.is_edge(edge):
        for item in edge:
            add_edges(parser, edge_counts, item)

    edge_str = ed.edge2str(edge, namespaces=False)
    if not sym.is_edge(edge):
        if edge_str[0] == '+':
            edge_str = edge_str[1:]
        if len(edge_str) == 0:
            return
        if not edge_str[0].isalnum():
            return
        if parser.make_word(edge_str).prob > MAX_PROB:
            return
    if edge_str not in edge_counts:
        edge_counts[edge_str] = 0
    edge_counts[edge_str] += 1


def find_co_synonyms(atom_set, synonym_map, edge):
    co_syns = set()
    if sym.is_edge(edge):
        for item in edge:
            co_syns = co_syns.union(find_co_synonyms(atom_set, synonym_map, item))

    edge_str = ed.edge2str(edge, namespaces=False)

    for atom in atom_set:
        if atom == edge_str:
            co_syns.add(synonym_map[atom])
            return co_syns

    return co_syns


if __name__ == '__main__':
    print('creating parser...')
    par = par.Parser()
    print('parser created.')

    # read data
    edge_data = json_tools.read('edges_similar_concepts.json')

    # build extra edges list
    extra_edges = []
    for item in edge_data:
        edge = ed.str2edge(item['edge'])
        matched = [ed.str2edge(match[1]) for match in item['matches']]
        for part in edge[1:]:
            if part not in matched:
                extra_edges.append(part)
    eedges = [simil.enrich_edge(par, edge) for edge in extra_edges]

    # build edge counts map, with inner edges
    edge_counts = {}
    for edge in extra_edges:
        add_edges(par, edge_counts, edge)
    print(edge_counts)
    print(len(edge_counts))

    # create atoms map -- edges with more than one occurrence
    atoms = {}
    for key in edge_counts:
        if edge_counts[key] > 1:
            edge = ed.str2edge(key)
            atoms[key] = ed.depth(ed.str2edge(key))

    # build atom_set
    atom_set = set([atom for atom in atoms])

    # sorted by depth
    sorted_atoms = sorted(atoms.items(), key=operator.itemgetter(1), reverse=False)

    # find atoms and parent-child synonym relationships
    children = {}
    for i in range(len(sorted_atoms)):
        parents = []
        satom1 = sorted_atoms[i][0]
        atom1 = ed.str2edge(satom1)
        cur_depth = sorted_atoms[i][1]
        start = i
        while start < len(sorted_atoms) and sorted_atoms[start][1] <= cur_depth:
            start += 1
        for j in range(start, len(sorted_atoms)):
            satom2 = sorted_atoms[j][0]
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
    synonym_sets = []
    for satom in children:
        synonyms = [satom] + children[satom]
        count = 0
        for synonym in synonyms:
            count += edge_counts[synonym]
            del atoms[synonym]
        synonym_sets.append({'edges': synonyms, 'count': count, 'index': len(synonym_sets)})

    for atom in atoms:
        synonym_sets.append({'edges': [atom], 'count': edge_counts[atom], 'index': len(synonym_sets)})

    nsyns = len(synonym_sets)

    print(synonym_sets)

    # build synonym map
    synonym_map = {}
    for sset in synonym_sets:
        for synonym in sset['edges']:
            synonym_map[synonym] = sset['index']
        synonym_map[sset['index']] = sset

    print(synonym_map)


    # build coocurrence sparse matrix
    synonym_cooc = sps.lil_matrix((nsyns, nsyns))
    for edge in extra_edges:
        co_synonyms = find_co_synonyms(atom_set, synonym_map, edge)
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
    g.add_vertices(len(synonym_sets))
    g.add_edges(gedges)
    g.es['weight'] = weights

    # community detection
    comms = igraph.Graph.community_multilevel(g, weights='weight', return_levels=False)

    # build atom_groups
    atom_groups = {}
    for i in range(len(comms)):
        comm = comms[i]
        count = 0
        syns = []
        sentences = set()
        edges = []
        for item in comm:
            edges += synonym_map[item]['edges']

            for atom in synonym_map[item]['edges']:
                for edat in edge_data:
                    if edge_contains(ed.str2edge(ed.edge2str(ed.str2edge(edat['edge']), namespaces=False)),
                                     ed.str2edge(atom),
                                     deep=True):
                        if edat['text']:
                            sentences.add(edat['text'])
            syns.append(synonym_map[item])
            count += synonym_map[item]['count']
        label = ', '.join(edges)
        atom_group = {'label': label,
                      'syns': syns,
                      'count': count,
                      'sentences': sentences}
        atom_groups[i] = atom_group

    # print groups
    n = 0
    for k in atom_groups:
        ag = atom_groups[k]
        size = len(ag['sentences'])
        if size > 3:
            n += 1
            print('CLUSTER id: %s' % n)
            print('Base concepts: %s' % ag['label'])
            print('size: %s' % size)
            print('sentences:')
            for sentence in ag['sentences']:
                print('* %s' % sentence)
            print()


    # build atom_group coocurrence sparse matrix
    # nag = len(atom_groups)
    # ag_cooc = sps.lil_matrix((nag, nag))
    # for edat in edge_data:
    #     edge = ed.str2edge(ed.edge2str(ed.str2edge(edat['edge']), namespaces=False))
    #     co_ags = find_co_atom_groups(atom_groups, edge)
    #     if len(co_ags) > 1:
    #         for pair in itertools.combinations(co_synonyms, 2):
    #             synonym_cooc[pair[0], pair[1]] += 1
    #             synonym_cooc[pair[1], pair[0]] += 1
