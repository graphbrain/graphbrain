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


from gb.nlp.parser import Parser
from gb.nlp.sentence import Sentence


class TokenNode:
    def __init__(self, pivot):
        self.pivot = pivot
        self.left = []
        self.right = []

    def add_token(self, token, left):
        if left:
            self.left.append(token)
        else:
            self.right.append(token)

    def merge_node(self, node, left):
        for token in node.left:
            self.add_token(token, left)
        self.add_token(node.pivot, left)
        for token in node.right:
            self.add_token(token, left)

    def __str__(self):
        words = [token.word for token in self.left]
        words.append(self.pivot.word)
        words += [token.word for token in self.right]
        return '_'.join(words)


class TokenEdge:
    def __init__(self, nodes=None):
        if nodes is None:
            self.nodes = []
        else:
            self.nodes = nodes
        self.pointer = self.nodes

    def is_singleton(self):
        return len(self.nodes) == 1

    def root(self):
        if len(self.nodes) > 0:
            return self.nodes[0]
        else:
            return None

    def __str__(self):
        strs = [str(node) for node in self.nodes]
        return '(%s)' % ' '.join(strs)


def process_leaf(edge, root, leaf, left):
    # ignore
    if (len(leaf.dep) == 0) or (leaf.dep == 'punct'):
        return

    # process subtree
    child = process_token(leaf)

    # add to relationship node
    if (leaf.dep == 'aux') \
            or (leaf.dep == 'auxpass') \
            or ((leaf.dep == 'prep') and (root.pos == 'VERB'))\
            or (leaf.dep == 'agent') \
            or (leaf.dep == 'pcomp') \
            or (leaf.dep == 'compound') \
            or (leaf.dep == 'amod'):
        edge.pointer[0].merge_node(child.root(), left)
        if not child.is_singleton():
            edge.pointer += child.nodes[1:]
        return

    # modifier
    if (leaf.dep == 'det') \
            or (leaf.dep == 'advmod') \
            or (leaf.dep == 'poss'):
        edge.nodes = [child, TokenEdge(edge.pointer)]
        return

    # preposition
    if leaf.dep == 'prep':
        edge.pointer.insert(0, child.nodes[0])
        if not child.is_singleton():
            edge.pointer += child.nodes[1:]
        return

    # add as child
    edge.pointer.append(child)


def process_token(token):
    edge = TokenEdge()
    node = TokenNode(token)
    edge.pointer.append(node)
    for leaf in token.left_children:
        process_leaf(edge, token, leaf, True)
    for leaf in token.right_children:
        process_leaf(edge, token, leaf, False)
    return edge


def remove_singletons(edge):
    if isinstance(edge, TokenNode):
        return edge
    elif edge.is_singleton():
        return edge.nodes[0]
    else:
        edge.nodes = [remove_singletons(node) for node in edge.nodes]
        return edge


class TokenTree:
    def __init__(self, sentence):
        self.root = process_token(sentence.root())
        remove_singletons(self.root)

    def __str__(self):
        return str(self.root)


if __name__ == '__main__':
    test_text = u"""
    Some subspecies of mosquito might be 1st to be genetically wiped out.
    Telmo is going to the gym.
    Due to its location in the European Plain, Berlin is influenced by a temperate seasonal climate.
    """

    print('Starting parser...')
    parser = Parser()
    print('Parsing...')
    result = parser.parse_text(test_text)

    for r in result:
        s = Sentence(r)
        print(s)
        s.print_tree()
        tree = TokenTree(s)
        print(tree)
