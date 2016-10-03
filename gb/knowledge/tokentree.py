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


from __future__ import print_function


class Position(object):
    LEFT, RIGHT = range(2)


class TokenNode(object):
    def __init__(self, pivot):
        self.pivot = pivot
        self.left = []
        self.right = []

    def add_token(self, token, pos):
        if pos == Position.LEFT:
            self.left.append(token)
        else:
            self.right.append(token)

    def merge_node(self, node, pos):
        if isinstance(node, TokenEdge):
            node = node.root()
        for token in node.left:
            self.add_token(token, pos)
        self.add_token(node.pivot, pos)
        for token in node.right:
            self.add_token(token, pos)

    def __str__(self):
        words = [token.word for token in self.left]
        words.append(self.pivot.word)
        words += [token.word for token in self.right]
        return '_'.join(words)


def create_placeholder():
    return TokenEdge(None, None, None, True)


def apply_layer(nodes, layer):
    for i in range(len(layer.nodes)):
        if isinstance(layer.nodes[i], TokenEdge):
            if layer.nodes[i].placeholder:
                layer.nodes[i] = TokenEdge(None, None, nodes)
                return
            apply_layer(nodes, layer.nodes[i])


class TokenEdge(object):
    def __init__(self, pos, base_token, nodes=None, placeholder=False):
        self.pos = pos
        self.base_token = base_token
        if nodes is None:
            self.nodes = []
        else:
            self.nodes = nodes
        self.placeholder = placeholder
        self.layers = []
        self.layer = None
        if not placeholder:
            self.layer = create_placeholder()

    def new_layer(self):
        if not self.layer.placeholder:
            self.layers.append(self.layer)
            self.layer = create_placeholder()

    def apply_layers(self):
        self.layers.reverse()
        nodes = self.nodes
        for layer in self.layers:
            apply_layer(nodes, layer)
            nodes = layer.nodes
        self.nodes = nodes

    def is_singleton(self):
        return len(self.nodes) == 1

    # TODO: hack
    def root(self):
        if len(self.nodes) > 0:
            node0 = self.nodes[0]
            if isinstance(node0, TokenNode):
                return node0
            else:
                return node0.root()
        else:
            raise IndexError('Requesting root on an empty TokenEdge')

    def append_to_root(self, node):
        if len(self.nodes) > 0:
            if isinstance(self.nodes[0], TokenNode):
                self.nodes[0] = TokenEdge(None, None, [self.nodes[0]])
            self.nodes[0].nodes.append(node)
        else:
            raise IndexError('Requesting root on an empty TokenEdge')

    def rest(self):
        if len(self.nodes) > 1:
            return self.nodes[1:]
        else:
            raise IndexError('Requesting rest on a TokenEdge with %s nodes.' % len(self.nodes))

    def __str__(self):
        if self.placeholder:
            return '[*]'
        else:
            strs = [str(node) for node in self.nodes]
            return '(%s)' % ' '.join(strs)


def remove_singletons(edge):
    if isinstance(edge, TokenNode):
        return edge
    elif edge.is_singleton():
        return remove_singletons(edge.nodes[0])
    else:
        edge.nodes = [remove_singletons(node) for node in edge.nodes]
        return edge
