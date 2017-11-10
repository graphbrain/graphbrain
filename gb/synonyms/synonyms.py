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


import progressbar
import gb.constants as cons
import gb.hypergraph.symbol as sym
import gb.hypergraph.edge as ed
import gb.nlp.parser as par
from gb.synonyms.meronomy import Meronomy


def generate(hg):
    print('starting parser...')
    parser = par.Parser()

    mer = Meronomy(parser)

    print('reading edges...')
    total_verts = hg.symbol_count() + hg.edge_count()
    i = 0
    with progressbar.ProgressBar(max_value=total_verts) as bar:
        for vertex in hg.all():
            if sym.is_edge(vertex):
                edge = vertex
                if (not sym.is_edge(edge[0])) and (sym.nspace(edge[0]) != 'gb'):
                    mer.add_edge(edge)
            i += 1
            bar.update(i)

    print('generating meronomy...')
    mer.generate()

    print('generating synonyms...')
    mer.generate_synonyms()

    print('writing synonyms...')
    i = 0
    with progressbar.ProgressBar(max_value=len(mer.synonym_sets)) as bar:
        for syn_id in mer.synonym_sets:
            edges = set()
            for atom in mer.synonym_sets[syn_id]:
                edge = ed.str2edge(atom)
                if edge in mer.edge_map:
                    edges = edges.union(mer.edge_map[edge])
            labels = [hg.get_label(edge) for edge in edges]
            label = min(labels, key=len)
            syn_symbol = sym.build(label, 'syn%s' % syn_id)
            for edge in edges:
                syn_edge = (cons.are_synonyms, edge, syn_symbol)
                hg.add(syn_edge)
            label_symbol = sym.build(label, cons.label_namespace)
            label_edge = (cons.has_label, syn_symbol, label_symbol)
            hg.add(label_edge)
            i += 1
            bar.update(i)

    print('%s synonym sets created' % len(mer.synonym_sets))
    print('done.')


def main_synonym(hg, edge):
    edges = hg.pattern2edges([cons.are_synonyms, edge, None])
    if len(edges) > 0:
        return edges.pop()[2]
    return edge
