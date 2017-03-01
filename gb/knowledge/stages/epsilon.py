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


import gb.hypergraph.symbol as sym


def leaf_to_hyperedges(leaf):
    return sym.build((leaf.token.word, leaf.namespace))


class EpsilonStage(object):
    def __init__(self, hg, output):
        self.hg = hg
        self.output = output

    def node_to_hyperedge(self, node):
        main_edge = [self.build_hyperedge(child) for child in node.children()]
        return tuple(main_edge)

    def compound_entity_to_leafs(self, entity):
        if entity.is_leaf():
            return [entity]
        else:
            leafs = []
            for child in entity.children():
                leafs += self.compound_entity_to_leafs(child)
            return leafs

    def compound_node_to_hyperedge(self, node):
        leafs = self.compound_entity_to_leafs(node)
        words = [leaf.token.word for leaf in leafs]
        ns = '?'
        if node.namespace:
            ns = node.namespace
        symbol = sym.build(('_'.join(words), ns))
        return symbol

    def build_hyperedge(self, entity):
        if entity.is_leaf():
            return leaf_to_hyperedges(entity)
        elif entity.compound:
            return self.compound_node_to_hyperedge(entity)
        else:
            return self.node_to_hyperedge(entity)

    def process(self):
        self.output.main_edge = self.build_hyperedge(self.output.tree.root())
        return self.output
