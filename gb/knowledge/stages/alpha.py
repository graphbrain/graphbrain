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
from gb.knowledge.semantic_tree import remove_singletons, Position, Node, Leaf


def apply_modifier(root, child):
    rel = child.nodes[0]
    rest = child.nodes[1:]
    root.layer = Node(None, [rel, root.layer] + rest)


def process_child(parent, leaf, root, pos):
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
        child = process_token(leaf, root)
        apply_modifier(root, child)
        root.new_layer()
        return

    child = process_token(leaf)

    # append to parent's root edge
    if (leaf.dep == 'pcomp') \
            or (leaf.dep == 'compound'):
        parent.append_to_root(child, pos)
        return

    # as child
    parent.nodes.append(child)


def process_token(token, root=None):
    node = Node(token)
    leaf = Leaf(token)
    node.nodes.append(leaf)
    if root is None:
        root = node
    for leaf in token.left_children:
        process_child(node, leaf, root, Position.LEFT)
    for leaf in token.right_children:
        process_child(node, leaf, root, Position.RIGHT)
    node.apply_layers()
    return node


def transform(sentence):
    tree = process_token(sentence.root())
    return remove_singletons(tree)


if __name__ == '__main__':
    test_text = u"""
    2016 Nobel Prize in Physiology or Medicine Is Awarded to Yoshinori Ohsumi.
    """

    print('Starting parser...')
    parser = Parser()
    print('Parsing...')
    result = parser.parse_text(test_text)

    for r in result:
        s = Sentence(r)
        print(s)
        s.print_tree()
        t = transform(s)
        print(t)
