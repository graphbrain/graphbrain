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


import click
import gb.hypergraph.edge as ed
from gb.nlp.parser import Parser
from gb.nlp.sentence import Sentence
from gb.reader.semantic_tree import Tree, Position
import gb.reader.stages.hypergen_transformation as hgtransf
from gb.reader.stages.hypergen_forest import expanded_fields, build_case


def test_transformation(parent, child, position, transf):
    test_tree = Tree(parent)
    test_tree.import_element(child)
    test_parent = test_tree.root()
    hgtransf.apply(test_parent, child.id, position, transf)
    return test_tree.to_hyperedge_str(with_namespaces=False)


class CaseGenerator(object):
    def __init__(self):
        self.tree = None
        self.parser = Parser()
        self.sentence_str = None
        self.sentence = None
        self.outcome = None
        self.outcome_str = None
        self.interactive = False
        self.cases = None
        self.transfs = None
        self.restart = False
        self.abort = False
        self.transformation_outcomes = None

    def show_option(self, key, name, parent, child, position, transf):
        res = test_transformation(parent, child, position, transf)
        if res not in self.transformation_outcomes:
            self.transformation_outcomes.append(res)
            click.echo(click.style(key, fg='cyan'), nl=False)
            click.echo(') ', nl=False)
            click.echo(click.style(name, fg='green'), nl=False)
            click.secho('   %s' % res, bold=True)

    def choose_transformation(self, parent, child, position):
        print('target sentence:')
        print(self.sentence_str)
        print('parent <- child')
        print('%s <- %s' % (parent, child))

        self.transformation_outcomes = []

        self.show_option('i', 'IGNORE', parent, child, position, hgtransf.IGNORE)
        self.show_option('a', 'APPLY', parent, child, position, hgtransf.APPLY)
        self.show_option('n', 'NEST', parent, child, position, hgtransf.NEST)
        self.show_option('f', 'FIRST', parent, child, position, hgtransf.FIRST)
        self.show_option('s', 'SHALLOW', parent, child, position, hgtransf.SHALLOW)
        self.show_option('d', 'DEEP', parent, child, position, hgtransf.DEEP)

        print('')

        if position == Position.LEFT:
            self.show_option('ar', 'APPLY_R', parent, child, position, hgtransf.APPLY_R)
            self.show_option('nr', 'NEST_R', parent, child, position, hgtransf.NEST_R)
            self.show_option('dr', 'DEEP_R', parent, child, position, hgtransf.DEEP_R)

        if position == Position.RIGHT:
            self.show_option('al', 'APPLY_L', parent, child, position, hgtransf.APPLY_L)
            self.show_option('nl', 'NEST_L', parent, child, position, hgtransf.NEST_L)
            self.show_option('dl', 'DEEP_L', parent, child, position, hgtransf.DEEP_L)

        print('\nr) RESTART    x) ABORT')

        choice = input('> ').lower()

        if choice == 'i':
            return hgtransf.IGNORE
        if choice == 'a':
            return hgtransf.with_position(hgtransf.APPLY, position)
        if choice == 'ar':
            return hgtransf.APPLY_R
        if choice == 'al':
            return hgtransf.APPLY_L
        if choice == 'n':
            return hgtransf.with_position(hgtransf.NEST, position)
        if choice == 'nr':
            return hgtransf.NEST_R
        if choice == 'nl':
            return hgtransf.NEST_L
        if choice == 's':
            return hgtransf.SHALLOW
        if choice == 'f':
            return hgtransf.FIRST
        if choice == 'd':
            return hgtransf.with_position(hgtransf.DEEP, position)
        if choice == 'dr':
            return hgtransf.DEEP_R
        if choice == 'dl':
            return hgtransf.DEEP_L
        if choice == 'r':
            self.restart = True
            return hgtransf.IGNORE
        if choice == 'x':
            self.abort = True
            return hgtransf.IGNORE
        else:
            print('unknown choice: "%s". ignoring' % choice)
            return self.choose_transformation(parent, child, position)

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
            if self.restart or self.abort:
                return -1, -1
            if t in (hgtransf.NEST, hgtransf.NEST_R, hgtransf.NEST_L):
                nested_left = True
        for child_token in token.right_children:
            self.process_token(child_token, token, elem_id, Position.RIGHT)
            if self.restart or self.abort:
                return -1, -1

        # choose and apply transformation
        transf = -1
        if parent_token:
            parent = self.tree.get(parent_id)
            child = self.tree.get(elem_id)
            if self.interactive:
                transf = self.choose_transformation(parent, self.tree.get(elem_id), position)
                if self.restart or self.abort:
                    return -1, -1
                self.transfs.append(transf)
            else:
                transf = self.transfs.pop(0)

            # add case
            if parent_token:
                case = build_case(parent, child, parent_token, token, position)
                case['transformation'] = str(transf)
                self.cases.append(case)

            print('%s <- %s' % (parent_token.word, token.word))
            print('%s <- %s' % (parent, self.tree.get(elem_id)))
            if parent.is_node():
                print('inn: %s' % str(parent.get_inner_nested_node()))
            print(hgtransf.to_string(transf))
            hgtransf.apply(parent, elem_id, position, transf)
            print(self.tree.get(parent_id))
            print()

        return elem_id, transf

    def generate(self, sentence_str, json_str=None, outcome_str=None):
        self.tree = Tree()
        self.sentence_str = sentence_str
        if json_str:
            self.sentence = Sentence(json_str=json_str)
        else:
            self.sentence = Sentence(self.parser.parse_text(sentence_str)[0][1])
        self.sentence.print_tree()
        if outcome_str:
            self.outcome_str = outcome_str
            self.outcome = ed.str2edge(outcome_str)
        self.tree.root_id, _ = self.process_token(self.sentence.root())

    def validate(self):
        return self.tree.to_hyperedge_str(with_namespaces=False) == self.outcome_str

    def write_cases(self, outfile):
        for case in self.cases:
            values = [str(case[field]) for field in expanded_fields()]
            f = open(outfile, 'a')
            f.write('%s\n' % ','.join(values))
            f.close()


def generate_cases(infile, outfile):
    f = open(outfile, 'w')
    f.write('%s\n' % ','.join(expanded_fields()))
    f.close()

    current_parse = []
    parses = []
    with open(infile) as f:
        for line in f:
            current_parse.append(line)
            if len(current_parse) == 4:
                parses.append(current_parse)
                current_parse = []

    total = 0
    correct = 0
    cg = CaseGenerator()
    for parse in parses:
        sentence_str = parse[0].strip()
        json_str = parse[1].strip()
        outcome_str = parse[2].strip()
        cg.transfs = [int(token) for token in parse[3].split(',')]
        cg.cases = []
        cg.generate(sentence_str, json_str, outcome_str)
        if cg.validate():
            correct += 1
            cg.write_cases(outfile)
        else:
            print('could not generate correct cases for: %s' % sentence_str)
            print(outcome_str)
            print(cg.tree.to_hyperedge_str(with_namespaces=False))
        total += 1

    print('%s out of %s correct cases.' % (correct, total))


def interactive_edge_builder(outfile):
    print('writing to file: %s' % outfile)
    cg = CaseGenerator()
    cg.interactive = True
    while True:
        sentence_str = input('sentence> ').strip()
        done = False
        while not done:
            cg.cases = []
            cg.transfs = []
            cg.restart = False
            cg.abort = False
            cg.generate(sentence_str)
            if cg.restart:
                print('restarting.')
            elif cg.abort:
                print('aborting.')
                done = True
            else:
                done = True
                outcome = cg.tree.to_hyperedge_str(with_namespaces=False)
                print('outcome:')
                print(outcome)
                write = input('write [y/N]? ')
                if write == 'y':
                    f = open(outfile, 'a')
                    f.write('%s\n' % sentence_str)
                    f.write('%s\n' % cg.sentence.to_json())
                    f.write('%s\n' % outcome)
                    f.write('%s\n' % ','.join([str(transf) for transf in cg.transfs]))
                    f.close()


if __name__ == '__main__':
    generate_cases('parses1.txt', 'dummy.txt')
