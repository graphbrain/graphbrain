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


import gb.hypergraph.symbol as sym
import gb.hypergraph.edge as ed
from gb.nlp.parser import Parser
from gb.nlp.sentence import Sentence
from gb.reader.semantic_tree import Tree
from gb.reader.semantic_tree import Position


GROW = 0
APPLY = 1
NEST = 2
IGNORE = 3


def find_parent_and_child(edge, parent, child, positions, depth=0, index=0):
    if len(positions) == 2:
        return positions
    if sym.is_edge(edge):
        for i in range(len(edge)):
            find_parent_and_child(edge[i], parent, child, positions, depth + 1, i)
    else:
        if edge == parent:
            positions['parent'] = {'depth': depth, 'index': index}
        if edge == child:
            positions['child'] = {'depth': depth, 'index': index}
    return positions


def is_nested(edge, outer, inner, parent_found=False):
    if parent_found:
        if sym.is_edge(edge):
            if (edge[0] == '+'):
                return False
            return is_nested(edge[0], outer, inner, True)
        else:
            return edge == inner
    if sym.is_edge(edge):
        if not sym.is_edge(edge[0]) and edge[0] == outer and len(edge) > 1:
            return is_nested(edge[1], outer, inner, True)
        else:
            for i in range(len(edge)):
                if is_nested(edge[i], outer, inner):
                    return True
    return False


class CaseGenerator(object):
    def __init__(self):
        self.tree = None
        self.parser = Parser()
        self.outcome = None
        self.outcome_str = None

    def infer_transformation(self, parent, child):
        positions = {}
        find_parent_and_child(self.outcome, parent.word, child.word, positions)
        if len(positions) != 2:
            return IGNORE
        parent_depth = positions['parent']['depth']
        parent_index = positions['parent']['index']
        child_depth = positions['child']['depth']
        child_index = positions['child']['index']

        if parent_depth == child_depth:
            if parent_index > 0 and child_index > 0:
                return GROW
            elif parent_index == 0:
                return APPLY
            else:
                return NEST
        if parent_depth > child_depth and is_nested(self.outcome, child.word, parent.word):
            return NEST
        elif parent_depth < child_depth and is_nested(self.outcome, parent.word, child.word):
            return NEST
        elif parent_depth > child_depth and child_index == 0:
            return NEST
        else:
            return APPLY

    def process_token(self, token, parent_token=None, parent_id=None, position=None):
        elem = self.tree.create_leaf(token)
        elem_id = elem.id

        # process children first
        for child_token in token.left_children:
            self.process_token(child_token, token, elem_id, Position.LEFT)
        for child_token in token.right_children:
            self.process_token(child_token, token, elem_id, Position.RIGHT)

        # infer and apply transformation
        if parent_token:
            parent = self.tree.get(parent_id)
            transf = self.infer_transformation(parent_token, token)
            # print('%s <- %s' % (parent_token.word, token.word))
            # print('%s <- %s' % (parent, self.tree.get(elem_id)))
            if transf == GROW:
                # print('grow')
                parent.grow_(elem_id, position)
            elif transf == APPLY:
                # print('apply')
                parent.apply_(elem_id, position)
            elif transf == NEST:
                # print('nest')
                parent.nest_(elem_id, position)
            else:
                # print('ignore')
                pass
            # print(self.tree.get(parent_id))
            # print()

        return elem_id

    def generate(self, sentence_str, outcome_str):
        self.tree = Tree()
        sentence = Sentence(self.parser.parse_text(sentence_str)[0][1])
        # sentence.print_tree()
        self.outcome_str = outcome_str
        self.outcome = ed.str2edge(outcome_str)
        self.tree.root_id = self.process_token(sentence.root())

    def validate(self):
        return self.tree.to_hyperedge_str(with_namespaces=False) == self.outcome_str

if __name__ == '__main__':
    sentence = 'donald trump may go ahead with mexican wall and registry for muslims without congress approval'
    outcome = '(may (go (ahead (+ donald trump) (with (and (+ mexican wall) (for registry muslims))) (without ' \
              '(+ congress approval)))))'
    cg = CaseGenerator()
    cg.generate(sentence, outcome)
    print(outcome)
    print(cg.tree.to_hyperedge_str(with_namespaces=False))
    print(cg.validate())
