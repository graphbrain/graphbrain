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
import gb.tools.json as json_tools
import gb.hypergraph.symbol as sym
import gb.hypergraph.edge as ed
import gb.nlp.parser as par
from gb.clusters.meronomy import Meronomy


def rel_contains(full_edge, term):
    if sym.is_edge(full_edge) and len(full_edge) > 2:
        if len(full_edge) > 3 or sym.is_edge(full_edge[2]):
            rel = full_edge[0]
            if sym.is_edge(rel):
                return term in rel
            else:
                return rel == term
    return False


if __name__ == '__main__':
    print('creating parser...')
    par = par.Parser()
    print('parser created.')

    # read data
    # edge_data = json_tools.read('edges_similar_concepts.json')
    edge_data = json_tools.read('all.json')

    # build full edges list
    full_edges = []
    for it in edge_data:
        full_edges.append(ed.without_namespaces(ed.str2edge(it['edge'])))

    # build meronomy
    print('creating meronomy...')
    mer = Meronomy(par, full_edges)
    mer.normalize_graph()
    print('meronomy created.')

    # generate synonyms
    print('creating synonyms...')
    mer.generate_synonyms()
    for synid in mer.synonym_sets:
        synonym_set = mer.synonym_sets[synid]
        if len(synonym_set) > 0:
            print('syn_set #%s' % synid)
            print(synonym_set)
    print('synonyms created.')

    say_edges = []
    for full_edge in full_edges:
        if rel_contains(full_edge, 'says'):
            print(ed.edge2str(full_edge))
            say_edges.append(full_edge)

    sayers = {}
    for edge in say_edges:
        sayer = ed.edge2str(edge[1])
        if sayer not in sayers:
            sayers[sayer] = 0
        sayers[sayer] += 1

    sorted_sayers = sorted(sayers.items(), key=operator.itemgetter(1), reverse=True)
    for t in sorted_sayers[:50]:
        print('%s %s' % t)
