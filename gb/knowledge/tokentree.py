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
from gb.nlp.token import Token
from gb.nlp.sentence import Sentence


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


class TokenEdge(object):
    def __init__(self, nodes=None):
        if nodes is None:
            self.nodes = []
        else:
            self.nodes = nodes
        self.pointer = self.nodes
        self.rel_continuity = False

    def is_singleton(self):
        return len(self.nodes) == 1

    def root(self):
        if len(self.nodes) > 0:
            return self.nodes[0]
        else:
            raise IndexError('Requesting root on an empty TokenEdge')

    def rest(self):
        if len(self.nodes) > 1:
            return self.nodes[1:]
        else:
            raise IndexError('Requesting rest on a TokenEdge with %s nodes.' % len(self.nodes))

    def __str__(self):
        strs = [str(node) for node in self.nodes]
        return '(%s)' % ' '.join(strs)


def remove_singletons(edge):
    if isinstance(edge, TokenNode):
        return edge
    elif edge.is_singleton():
        return edge.nodes[0]
    else:
        edge.nodes = [remove_singletons(node) for node in edge.nodes]
        return edge


class TokenTree(object):
    def __init__(self, sentence):
        self.root = self.process_token(sentence.root())
        remove_singletons(self.root)

    def __str__(self):
        return str(self.root)

    def process_leaf(self, parent, leaf, pos):
        # ignore
        if (len(leaf.dep) == 0) or (leaf.dep == 'punct'):
            return

        # process subtree
        child = self.process_token(leaf)

        rel_node = parent.pointer[0]

        # addition to relationship
        if (leaf.dep == 'aux') \
                or (leaf.dep == 'auxpass') \
                or ((leaf.dep == 'prep') and (rel_node.pivot.pos == 'VERB')) \
                or ((leaf.dep == 'prep') and (rel_node.pivot.dep == 'prep')) \
                or (leaf.dep == 'agent') \
                or (leaf.dep == 'pcomp') \
                or (leaf.dep == 'compound') \
                or (leaf.dep == 'amod') \
                or ((leaf.dep == 'advmod') and (rel_node.pivot.pos == 'VERB')):
            if not parent.rel_continuity:
                if (pos == Position.RIGHT) or (len(rel_node.left) > 0):
                    sep = Token('_')
                    sep.separator = True
                    rel_node.add_token(sep, pos)
            rel_node.merge_node(child.root(), pos)
            if not child.is_singleton():
                parent.pointer += child.rest()
            parent.rel_continuity = True
            return

        parent.rel_continuity = False

        # modifier
        if (leaf.dep == 'det') \
                or (leaf.dep == 'advmod') \
                or (leaf.dep == 'poss') \
                or (leaf.dep == 'relcl'):
            parent.nodes = [child, TokenEdge(parent.pointer)]
            return

        # application
        if (leaf.dep == 'prep') \
                or (leaf.dep == 'cc'):
            parent.pointer.insert(0, child.root())
            if not child.is_singleton():
                parent.pointer += child.rest()
            return

        # as child
        parent.pointer.append(child)

    def process_token(self, token):
        edge = TokenEdge()
        node = TokenNode(token)
        edge.pointer.append(node)
        edge.rel_continuity = True
        for leaf in token.left_children:
            self.process_leaf(edge, leaf, Position.LEFT)
        for leaf in token.right_children:
            self.process_leaf(edge, leaf, Position.RIGHT)
        return edge


if __name__ == '__main__':
    test_text = u"""
    OpenCola is a brand of open-source cola, where the instructions for making it are freely available and modifiable.
    Koikuchi shoyu, best known as soy sauce, is the mother of all sauces in Japan.
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
