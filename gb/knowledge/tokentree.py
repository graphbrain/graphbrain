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

    def __str__(self):
        words = [token.word for token in self.left]
        words.append(self.pivot.word)
        words += [token.word for token in self.right]
        return '_'.join(words)


class TokenEdge:
    def __init__(self):
        self.nodes = []

    def __str__(self):
        strs = [str(node) for node in self.nodes]
        return '(%s)' % ' '.join(strs)


def process_leaf(edge, root, leaf, left):
    # tokens to ignore
    if (len(leaf.dep) == 0) or (leaf.dep == 'punct'):
        return

    # add to relationship node
    if (leaf.dep == 'aux') or (leaf.dep == 'auxpass'):
        edge.nodes[0].add_token(leaf, left)
        return

    # process subtree
    child = process_token(leaf)

    # add as child
    edge.nodes.append(child)


def process_token(token):
    edge = TokenEdge()
    node = TokenNode(token)
    edge.nodes.append(node)
    for leaf in token.left_children:
        process_leaf(edge, token, leaf, True)
    for leaf in token.right_children:
        process_leaf(edge, token, leaf, False)

    if len(edge.nodes) == 1:
        return edge.nodes[0]
    else:
        return edge


class TokenTree:
    def __init__(self, sentence):
        self.root = process_token(sentence.root())

    def __str__(self):
        return str(self.root)


if __name__ == '__main__':
    test_text = u"""
    Some subspecies of mosquito might be 1st to be genetically wiped out.
    """

    print('Starting parser...')
    parser = Parser()
    print('Parsing...')
    result = parser.parse_text(test_text)

    for r in result:
        sentence = Sentence(r)
        print(sentence)
        sentence.print_tree()
        tree = TokenTree(sentence)
        print(tree)
