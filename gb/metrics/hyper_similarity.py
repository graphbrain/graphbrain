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


import gb.hypergraph.hypergraph as hyperg


def similarity(hg, edge1, edge2):
    neighb1 = hg.star(edge1)
    neighb2 = hg.star(edge2)


def sphere(hg, edge):
    edges = set(hg.star(edge))
    for e in edges:
        if hg.degree(e) > 0:
            edges = edges.union(sphere(hg, e))
    return edges


if __name__ == '__main__':
    hg = hyperg.HyperGraph({'backend': 'leveldb', 'hg': 'reddit-worldnews-01012013-01082017.hg'})

    # e = 'clinton/nlp.clinton.noun'

    print('starting...')

    # e = '(+/gb prime/nlp.prime.adj minister/nlp.minister.noun)'
    e = 'europe/nlp.europe.noun'

    print(len(sphere(hg, e)))

    best_item = None
    best_degree = -1
    neighb = hg.star(e)
    print(len(neighb))
    for item in neighb:
        deg = hg.degree(item)
        if deg > best_degree:
            best_item = item
            best_degree = deg

    print('%s %s' % (best_item, best_degree))
