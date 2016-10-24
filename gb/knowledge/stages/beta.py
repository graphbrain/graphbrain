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


class BetaStage(object):
    def __init__(self, hg, tree):
        self.hg = hg
        self.tree = tree

    def is_compound(self, node):
        return False

    def process_entity(self, entity_id):
        entity = self.tree.get(entity_id)

        entity.namespace = '1'

        if entity.is_node():
            entity.compound = self.is_compound(entity)
            for child_id in entity.children_ids:
                self.process_entity(child_id)

    def process(self):
        self.process_entity(self.tree.root_id)
        return self.tree


def transform(tree):
    return BetaStage(tree).process()


if __name__ == '__main__':
    test_text = """
    My name is James Bond.
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
        print(t)
