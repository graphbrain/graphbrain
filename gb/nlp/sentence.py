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


from asciitree import LeftAligned
from collections import OrderedDict


def node_label(token, prefix):
    return '[%s]%s' % (prefix, token)


def token2label_tree(token, prefix='*'):
    children = [token2label_tree(leaf, 'L') for leaf in token.left_children] +\
               [token2label_tree(leaf, 'R') for leaf in token.right_children]

    return node_label(token, prefix), OrderedDict(children)


def assign_depths(token, depth):
    token.depth = depth
    for leaf in token.left_children:
        assign_depths(leaf, depth + 1)
    for leaf in token.right_children:
        assign_depths(leaf, depth + 1)


class Sentence:
    def __init__(self, tokens):
        self.tokens = tokens
        assign_depths(self.root(), 0)

    def root(self):
        for token in self.tokens:
            if token.dep == 'ROOT':
                return token
        return None

    def label_tree(self):
        r = self.root()
        if r is None:
            return {}
        label, children = token2label_tree(r)
        return {label: children}

    def print_tree(self):
        tr = LeftAligned()
        print(tr(self.label_tree()))

    def __str__(self):
        return ' '.join([token.word.strip() for token in self.tokens])
