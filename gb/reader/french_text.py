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


import io
from gb.hypergraph.hypergraph import HyperGraph
import gb.hypergraph.edge as ed
from gb.reader.reader import Reader


if __name__ == '__main__':
    filename = 'nuclear.txt'
    hg = HyperGraph({'backend': 'leveldb', 'hg': 'nuclear.hg'})

    reader = Reader(hg, lang='fr', model_file='hypergen_random_forest_fr.model')

    lines = 0
    main_edges = 0
    extra_edges = 0
    ignored = 0
    with io.open(filename, 'r', encoding='utf-8') as f:
        for line in f:
            lines += 1
            print('LINE #%s' % lines)
            parses = reader.read_text(line, None, reset_context=False)
            for p in parses:
                print('\n')
                print('sentence: %s' % p[0])
                print(ed.edge2str(p[1].main_edge))
                if len(p[1].main_edge) < 8:
                    hg.add_belief('unknown', p[1].main_edge)
                    main_edges += 1
                    for edge in p[1].edges:
                        hg.add_belief('gb', edge)
                        extra_edges += 1
                    hg.set_attribute(p[1].main_edge, 'text', line)
                else:
                    ignored += 1

    print('main edges: %s' % main_edges)
    print('extra edges: %s' % extra_edges)
    print('ignored: %s' % ignored)
    print('done.')
