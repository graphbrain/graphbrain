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


import json
import gb.hypergraph.hypergraph as hyperg
import gb.hypergraph.symbol as sym
import gb.hypergraph.edge as ed


EXCLUDE_RELS = ['are_synonyms/gb', 'src/gb', 'have_same_lemma/gb']


def exclude(edge):
    if sym.is_edge(edge):
        rel = edge[0]
        if sym.is_edge(rel):
            return False
        return rel in EXCLUDE_RELS
    else:
        return True


def write_edge_data(edge_data, file_path):
    f = open(file_path, 'w')
    for e in edge_data:
        f.write('%s\n' % json.dumps(e, separators=(',', ':')))
    f.close()


class Filter(object):
    def __init__(self, hg):
        self.hg = hg

    def write_edges(self, file_path):
        pass


class AllFilter(Filter):
    def __init__(self, hg):
        Filter.__init__(self, hg)

    def all_edges(self):
        edges = self.hg.all()

        filtered_edges = []
        for edge in edges:
            if not exclude(edge):
                filtered_edges.append(edge)

        result = []
        for e in filtered_edges:
            edge_data = {'edge': ed.edge2str(e),
                         'text': self.hg.get_str_attribute(e, 'text')}
            result.append(edge_data)
        return result

    # override
    def write_edges(self, file_path):
        edge_data = self.all_edges()
        write_edge_data(edge_data, file_path)


class TermFilter(Filter):
    def __init__(self, hg, term):
        Filter.__init__(self, hg)
        self.term = term

    def edges_with_term(self):
        edges = self.hg.all()

        filtered_edges = []
        for edge in edges:
            if not exclude(edge):
                if ed.contains(ed.without_namespaces(edge), self.term):
                    print(edge)
                    filtered_edges.append(edge)

        result = []
        for e in filtered_edges:
            edge_data = {'edge': ed.edge2str(e),
                         'text': self.hg.get_str_attribute(e, 'text')}
            result.append(edge_data)
        return result

    # override
    def write_edges(self, file_path):
        edge_data = self.edges_with_term()
        write_edge_data(edge_data, file_path)


if __name__ == '__main__':
    hgr = hyperg.HyperGraph({'backend': 'leveldb', 'hg': 'reddit-politics.hg'})
    # s = TermFilter(hgr, 'china')
    # s.write_edges('china.json')

    filt = AllFilter(hgr)
    filt.write_edges('all.json')
