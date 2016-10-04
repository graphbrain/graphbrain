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


class Leaf(object):
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
        if isinstance(node, Node):
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
    return Node(None, None, True)


def apply_layer(nodes, layer):
    for i in range(len(layer.children)):
        if isinstance(layer.children[i], Node):
            if layer.children[i].placeholder:
                layer.children[i] = Node(None, nodes)
                return
            apply_layer(nodes, layer.children[i])


class Node(object):
    def __init__(self, base_token, children=None, placeholder=False):
        self.base_token = base_token
        if children is None:
            self.children = []
        else:
            self.children = children
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
        nodes = self.children
        for layer in self.layers:
            apply_layer(nodes, layer)
            nodes = layer.children
        self.children = nodes

    def is_singleton(self):
        return len(self.children) == 1

    # TODO: hack
    def root(self):
        if len(self.children) > 0:
            node0 = self.children[0]
            if isinstance(node0, Leaf):
                return node0
            else:
                return node0.root()
        else:
            raise IndexError('Requesting root on an empty Node')

    def append_to_root(self, node, pos):
        if len(self.children) > 0:
            if isinstance(self.children[0], Leaf):
                self.children[0] = Node(None, [self.children[0]])
            if pos == Position.RIGHT:
                self.children[0].children.append(node)
            else:
                self.children[0].children.insert(0, node)
        else:
            raise IndexError('Requesting root on an empty Node')

    def rest(self):
        if len(self.children) > 1:
            return self.children[1:]
        else:
            raise IndexError('Requesting rest on a Node with %s children.' % len(self.children))

    def __str__(self):
        if self.placeholder:
            return '[*]'
        else:
            strs = [str(node) for node in self.children]
            return '(%s)' % ' '.join(strs)


def remove_singletons(node):
    if isinstance(node, Leaf):
        return node
    elif node.is_singleton():
        return remove_singletons(node.children[0])
    else:
        node.children = [remove_singletons(child) for child in node.children]
        return node
