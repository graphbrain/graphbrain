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
    main_edge = sym.build((leaf.token.word, leaf.namespace))
    return {'main_edge': main_edge}


class EpsilonStage(object):
    def __init__(self, hg, tree):
        self.hg = hg
        self.tree = tree

    def node_to_hyperedges(self, node):
        main_edge = [self.build_hyperedges(child)['main_edge'] for child in node.children()]
        return {'main_edge': tuple(main_edge)}

    def compound_entity_to_leafs(self, entity):
        if entity.is_leaf():
            return [entity]
        else:
            leafs = []
            for child in entity.children():
                leafs += self.compound_entity_to_leafs(child)
            return leafs

    def compound_node_to_hyperedges(self, node):
        leafs = self.compound_entity_to_leafs(node)
        words = [leaf.token.word for leaf in leafs]
        ns = '?'
        if node.namespace:
            ns = node.namespace
        symbol = sym.build(('_'.join(words), ns))
        return {'main_edge': symbol}

    def build_hyperedges(self, entity):
        if entity.is_leaf():
            return leaf_to_hyperedges(entity)
        elif entity.compound:
            return self.compound_node_to_hyperedges(entity)
        else:
            return self.node_to_hyperedges(entity)

    def process(self):
        return self.build_hyperedges(self.tree.root())['main_edge']
