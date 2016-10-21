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
from gb.knowledge.semantic_tree import Position, Tree


def process_child_token(elems, parent_elem_id, token, root_id, pos):
    # ignore
    if (len(token.dep) == 0) or (token.dep == 'punct'):
        return

    parent_elem = elems.get(parent_elem_id)
    parent_token = parent_elem.root_token()

    # modifier
    if (token.dep == 'aux') \
            or (token.dep == 'auxpass') \
            or ((token.dep == 'prep') and (parent_token.pos != 'VERB')) \
            or (token.dep == 'cc') \
            or (token.dep == 'agent') \
            or (token.dep == 'det') \
            or (token.dep == 'advmod') \
            or (token.dep == 'amod') \
            or (token.dep == 'poss') \
            or (token.dep == 'nummod'):
        child_elem_id = process_token(elems, token, root_id)
        elems.get(root_id).apply_modifier(child_elem_id)
        elems.get(root_id).new_layer()
        return

    child_elem_id = process_token(elems, token)

    # append to parent's root element
    if (token.dep == 'pcomp') \
            or (token.dep == 'compound'):
        parent_elem.append_to_root(child_elem_id, pos)
        return

    # as child
    parent_elem.append_child(child_elem_id)


def process_token(elems, token, root_id=None):
    elem_id = elems.create_leaf(token)
    if root_id is None:
        root_id = elem_id
    for child_token in token.left_children:
        process_child_token(elems, elem_id, child_token, root_id, Position.LEFT)
    for child_token in token.right_children:
        process_child_token(elems, elem_id, child_token, root_id, Position.RIGHT)
    elems.get(elem_id).apply_layers()
    return elem_id


def transform(sentence):
    elems = Tree()
    tree_id = process_token(elems, sentence.root())
    tree = elems.get(tree_id)
    tree.remove_redundant_nesting()
    return tree


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
        t.print_tree()
        print(t)
