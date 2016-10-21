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


class AlphaStage(object):
    def __init__(self):
        self.tree = Tree()

    def process_child_token(self, parent_elem_id, token, root_id, pos):
        # ignore
        if (len(token.dep) == 0) or (token.dep == 'punct'):
            return

        parent_elem = self.tree.get(parent_elem_id)
        parent_token = parent_elem.root_token()

        # nest
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
            child_elem_id = self.process_token(token, root_id)
            self.tree.get(root_id).nest(child_elem_id)
            self.tree.get(root_id).new_layer()
            return

        child_elem_id = self.process_token(token)

        # append to parent's first child
        if (token.dep == 'pcomp') \
                or (token.dep == 'compound'):
            parent_elem.add_to_first_child(child_elem_id, pos)
            return

        # add child
        parent_elem.add_child(child_elem_id)

    def process_token(self, token, root_id=None):
        elem_id = self.tree.create_leaf(token)
        if self.tree.root_id is None:
            self.tree.root_id = elem_id
        if root_id is None:
            root_id = elem_id
        for child_token in token.left_children:
            self.process_child_token(elem_id, child_token, root_id, Position.LEFT)
        for child_token in token.right_children:
            self.process_child_token(elem_id, child_token, root_id, Position.RIGHT)
        self.tree.get(elem_id).apply_layers()
        return elem_id


def transform(sentence):
    alpha = AlphaStage()
    elem_id = alpha.process_token(sentence.root())
    alpha.tree.remove_redundant_nesting()
    return alpha.tree.get(elem_id)


if __name__ == '__main__':
    test_text = """
    Telmo is going to the gym.
    """

    print('Starting parser...')
    parser = Parser()
    print('Parsing...')
    result = parser.parse_text(test_text)

    print(result)

    for r in result:
        s = Sentence(r)
        print(s)
        s.print_tree()
        t = transform(s)
        t.print_tree()
        print(t)
