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


def is_nested(edge, outer, inner, parent_found=False):
    if parent_found:
        if sym.is_edge(edge):
            if edge[0] == '+':
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
        self.parent_id = -1

    def find_parent_and_child(self, edge, parent, child, positions, depth=0, index=0, parent_id=0):
        if len(positions) == 2:
            return positions
        if parent_id == 0:
            self.parent_id = 0
        if sym.is_edge(edge):
            self.parent_id += 1
            inner_parent_id = self.parent_id
            for i in range(len(edge)):
                self.find_parent_and_child(edge[i], parent, child, positions, depth + 1, i, inner_parent_id)
        else:
            if edge == parent:
                positions['parent'] = {'depth': depth, 'index': index, 'parent': parent_id}
            if edge == child:
                positions['child'] = {'depth': depth, 'index': index, 'parent': parent_id}
        return positions

    def infer_transformation(self, parent, child):
        positions = {}
        self.find_parent_and_child(self.outcome, parent.word, child.word, positions)
        if len(positions) != 2:
            return IGNORE
        parent_depth = positions['parent']['depth']
        parent_index = positions['parent']['index']
        parent_parent = positions['parent']['parent']
        child_depth = positions['child']['depth']
        child_index = positions['child']['index']
        child_parent = positions['child']['parent']

        if parent_depth == child_depth:
            if parent_parent != child_parent:
                return GROW
            elif parent_index > 0 and child_index > 0:
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
            if parent_index > 0:
                return GROW
            return APPLY

    def process_token(self, token, parent_token=None, parent_id=None, position=None):
        elem = self.tree.create_leaf(token)
        elem_id = elem.id

        # process children first
        nested_left = False
        for child_token in token.left_children:
            if nested_left:
                pos = Position.RIGHT
            else:
                pos = Position.LEFT
            _, t = self.process_token(child_token, token, elem_id, pos)
            if t == NEST:
                nested_left = True
        for child_token in token.right_children:
            self.process_token(child_token, token, elem_id, Position.RIGHT)

        # infer and apply transformation
        transf = -1
        if parent_token:
            parent = self.tree.get(parent_id)
            child = self.tree.get(elem_id)
            transf = self.infer_transformation(parent_token, token)
            print('%s <- %s' % (parent_token.word, token.word))
            print('%s <- %s' % (parent, self.tree.get(elem_id)))
            if parent.is_node():
                print('inn: %s' % str(parent.get_inner_nested_node(parent.id)))
            if transf == GROW:
                print('grow')
                parent.grow_(elem_id, position)
            elif transf == APPLY:
                print('apply')
                parent.apply_(elem_id, position)
            elif transf == NEST:
                print('nest')
                parent.nest_(elem_id, position)
            else:
                print('ignore')
                pass
            print(self.tree.get(parent_id))
            print()

        return elem_id, transf

    def generate(self, sentence_str, outcome_str):
        self.tree = Tree()
        sentence = Sentence(self.parser.parse_text(sentence_str)[0][1])
        sentence.print_tree()
        self.outcome_str = outcome_str
        self.outcome = ed.str2edge(outcome_str)
        self.tree.root_id, _ = self.process_token(sentence.root())

    def validate(self):
        return self.tree.to_hyperedge_str(with_namespaces=False) == self.outcome_str


def generate_cases(infile):
    current_case = []
    cases = []
    with open(infile) as f:
        for line in f:
            current_case.append(line)
            if len(current_case) == 2:
                cases.append(current_case)
                current_case = []

    total = 0
    correct = 0
    cg = CaseGenerator()
    for case in cases:
        sentence_str = case[0].strip()
        outcome_str = case[1].strip()
        cg.generate(sentence_str, outcome_str)
        if cg.validate():
            correct += 1
        else:
            print('could not generate correct cases for: %s' % sentence_str)
            print(outcome_str)
            print(cg.tree.to_hyperedge_str(with_namespaces=False))
        total += 1

    print('%s out of %s correct cases.' % (correct, total))


if __name__ == '__main__':
    generate_cases('parses.txt')
    # sentence_str = 'Some subspecies of mosquito might be 1st to be genetically wiped out.'
    # outcome_str = '(might (be (some (of subspecies mosquito)) (1st (to (be (genetically (out wiped)))))))'
    # cg = CaseGenerator()
    # cg.generate(sentence_str, outcome_str)
    # print(outcome_str)
    # print(cg.tree.to_hyperedge_str(with_namespaces=False))
