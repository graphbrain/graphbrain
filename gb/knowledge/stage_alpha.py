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
from gb.nlp.parser import Parser
from gb.nlp.sentence import Sentence
from tokentree import remove_singletons, Position, TokenEdge, TokenNode


def apply_modifier(root, child):
    rel = child.nodes[0]
    rest = child.nodes[1:]
    root.layer = TokenEdge(root.pos, None, [rel, root.layer] + rest)


def process_leaf(parent, leaf, root, pos):
    # ignore
    if (len(leaf.dep) == 0) or (leaf.dep == 'punct'):
        return

    parent_token = parent.root().pivot

    # modifier
    if (leaf.dep == 'aux') \
            or (leaf.dep == 'auxpass') \
            or ((leaf.dep == 'prep') and (parent_token.pos != 'VERB')) \
            or (leaf.dep == 'cc') \
            or (leaf.dep == 'agent') \
            or (leaf.dep == 'det') \
            or (leaf.dep == 'advmod') \
            or (leaf.dep == 'amod') \
            or (leaf.dep == 'poss'):
        child = process_token(leaf, pos, root)
        apply_modifier(root, child)
        root.new_layer()
        return

    child = process_token(leaf, pos)

    # append to parent's root edge
    if (leaf.dep == 'pcomp') \
        or (leaf.dep == 'compound'):
        parent.append_to_root(child, pos)
        return

    # as child
    parent.nodes.append(child)


def process_token(token, pos=None, root=None):
    edge = TokenEdge(pos, token)
    node = TokenNode(token)
    edge.nodes.append(node)
    if root is None:
        root = edge
    for leaf in token.left_children:
        process_leaf(edge, leaf, root, Position.LEFT)
    for leaf in token.right_children:
        process_leaf(edge, leaf, root, Position.RIGHT)
    edge.apply_layers()
    return edge


def alpha_transform(sentence):
    tree = process_token(sentence.root())
    return remove_singletons(tree)

if __name__ == '__main__':
    test_text = u"""
    OpenCola is a brand of open-source cola, where the instructions for making it are freely available and modifiable.
    """

    print('Starting parser...')
    parser = Parser()
    print('Parsing...')
    result = parser.parse_text(test_text)

    for r in result:
        s = Sentence(r)
        print(s)
        s.print_tree()
        t = alpha_transform(s)
        print(t)
