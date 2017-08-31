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
import gb.hypergraph.edge as ed


class HyperSimilarity:
    def __init__(self, hg):
        self.hg = hg
        self.cs_cache = {}

    def sphere(self, edge):
        edges = set(self.hg.star(edge))
        for e in edges:
            if self.hg.degree(e) > 0:
                edges = edges.union(self.sphere(e))
        return edges

    def concept_sphere(self, edge):
        if edge in self.cs_cache:
            return self.cs_cache[edge]

        concepts = set()
        for item in self.sphere(edge):
            concepts = concepts.union(ed.subedges(item))

        self.cs_cache[edge] = concepts

        return concepts

    def similarity(self, edge1, edge2):
        cs1 = self.concept_sphere(edge1)
        cs2 = self.concept_sphere(edge2)

        # csu = cs1.union(cs2)
        csi = cs1.intersection(cs2)

        return float(len(csi)) / float(min(len(cs1), len(cs2)))

    def nsimilarity(self, edges1, edges2):
        cs1 = set()
        for edge in edges1:
            cs1 = cs1.union(self.concept_sphere(edge))
        cs2 = set()
        for edge in edges2:
            cs2 = cs2.union(self.concept_sphere(edge))

        # csu = cs1.union(cs2)
        csi = cs1.intersection(cs2)

        return float(len(csi)) / float(min(len(cs1), len(cs2)))


if __name__ == '__main__':
    hgr = hyperg.HyperGraph({'backend': 'leveldb', 'hg': 'reddit-worldnews-01012013-01082017.hg'})
    hs = HyperSimilarity(hgr)

    # e = 'clinton/nlp.clinton.noun'

    print('starting...')

    e1 = '(+/gb prime/nlp.prime.adj minister/nlp.minister.noun)'
    e2 = 'europe/nlp.europe.noun'

    print(hs.similarity(e1, e2))
