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
import gb.hypergraph.symbol as sym
import gb.hypergraph.edge as ed
from gb.nlp.parser import Parser
from gb.nlp.sentence import Sentence
from gb.nlp.nlp_token import Token
from gb.reader.semantic_tree import Tree, Position
from gb.reader.stages.common import Transformation
from gb.reader.stages.alpha_forest import expanded_fields, build_case


def transf2str(transf):
    if transf == Transformation.IGNORE:
        return 'ignore'
    elif transf == Transformation.APPLY:
        return 'apply'
    elif transf == Transformation.NEST:
        return 'nest'
    elif transf == Transformation.SHALLOW:
        return 'shallow'
    elif transf == Transformation.DEEP:
        return 'deep'
    elif transf == Transformation.FIRST:
        return 'first'
    elif transf == Transformation.APPLY_R:
        return 'apply[R]'
    elif transf == Transformation.APPLY_L:
        return 'apply[L]'
    elif transf == Transformation.NEST_R:
        return 'nest[R]'
    elif transf == Transformation.NEST_L:
        return 'nest[L]'
    elif transf == Transformation.DEEP_R:
        return 'deep[R]'
    elif transf == Transformation.DEEP_L:
        return 'deep[L]'
    else:
        return '?'


def tedge2str(tedge):
    if type(tedge) is Token:
        return tedge.word
    return '(%s)' % ' '.join([tedge2str(t) for t in tedge])


def tedge2tokens(tedge):
    if type(tedge) is Token:
        return [tedge]
    tokens = []
    for t in tedge:
        tokens = tokens + tedge2tokens(t)
    return tokens


def add_word_counts(word_counts_targ, word_counts):
    for word in word_counts.keys():
        if word in word_counts_targ:
            word_counts_targ[word] += 1
        else:
            word_counts_targ[word] = 1


def common_path(path1, path2):
    cp = []
    d = min(len(path1), len(path2))
    for i in range(d):
        if path1[i] == path2[i]:
            cp.append(path1[i])
    return cp


def path2tedge(path, tedge):
    if path:
        return path2tedge(path[1:], tedge[path[0]])
    return tedge


def match_score(test_parent, target_tedge, test_index=0, target_index=0, dist=0):
    if test_parent.is_leaf():
        if type(target_tedge) is Token:
            if test_parent.token.word == target_tedge.word:
                return 1000 - dist
            else:
                return 0
        else:
            best_score = 0
            for tedge in target_tedge:
                score = match_score(test_parent, tedge, 0, 0, dist + 1)
                best_score = max(score, best_score)
            return best_score

    if type(target_tedge) is Token:
        return 0

    if test_index >= len(test_parent.children_ids):
        return 0
    if target_index >= len(target_tedge):
        return 0

    best_score = 0

    if test_index == 0 and target_index == 0:
        for tedge in target_tedge:
            score = match_score(test_parent, tedge)
            best_score = max(score, best_score)

    score = match_score(test_parent.children()[test_index], target_tedge[target_index])
    score += match_score(test_parent, target_tedge, test_index + 1, target_index + 1)
    best_score = max(score, best_score)
    score = match_score(test_parent, target_tedge, test_index, target_index + 1, dist + 1)
    best_score = max(score, best_score)

    return best_score


def test_transformation(parent, child, position, transf):
    test_tree = Tree(parent)
    test_tree.import_element(child)
    test_parent = test_tree.root()
    if transf == Transformation.IGNORE:
        pass
    elif transf == Transformation.APPLY:
        test_parent.apply(child.id, position)
    elif transf == Transformation.NEST:
        test_parent.nest(child.id, position)
    elif transf == Transformation.SHALLOW:
        test_parent.nest_shallow(child.id)
    elif transf == Transformation.DEEP:
        test_parent.nest_deep(child.id, position)
    elif transf == Transformation.FIRST:
        test_parent.insert(child.id, Position.LEFT)
    elif transf == Transformation.APPLY_R:
        test_parent.apply(child.id, Position.RIGHT)
    elif transf == Transformation.APPLY_L:
        test_parent.apply(child.id, Position.LEFT)
    elif transf == Transformation.NEST_R:
        test_parent.nest(child.id, Position.RIGHT)
    elif transf == Transformation.NEST_L:
        test_parent.nest(child.id, Position.LEFT)
    elif transf == Transformation.DEEP_R:
        test_parent.nest_deep(child.id, Position.RIGHT)
    elif transf == Transformation.DEEP_L:
        test_parent.nest_deep(child.id, Position.LEFT)
    return test_tree.root()


def test_transformation_str(parent, child, position, transf):
    test_node = test_transformation(parent, child, position, transf)
    return test_node.tree.to_hyperedge_str(with_namespaces=False)


def score_transformation(parent, child, position, common_tedge, transf):
    test_parent = test_transformation(parent, child, position, transf)
    return match_score(test_parent, common_tedge)


def with_position(transf, position):
    if transf == Transformation.APPLY:
        if position == Position.LEFT:
            return Transformation.APPLY_L
        else:
            return Transformation.APPLY_R

    elif transf == Transformation.NEST:
        if position == Position.LEFT:
            return Transformation.NEST_L
        else:
            return Transformation.NEST_R

    elif transf == Transformation.DEEP:
        if position == Position.LEFT:
            return Transformation.DEEP_L
        else:
            return Transformation.DEEP_R


class Fit(object):
    def __init__(self, word_counts, outcome_word_counts):
        self.matches = 0
        self.size = 0
        for word in word_counts:
            if word in outcome_word_counts:
                self.matches += min(word_counts[word], outcome_word_counts[word])
            self.size += word_counts[word]

    def better_than(self, fit):
        if self.matches == fit.matches:
            return self.size < fit.size
        else:
            return self.matches > fit.matches


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

    def build_tedge(self, edge, counts=None):
        if not counts:
            counts = {}
        if sym.is_edge(edge):
            return [self.build_tedge(e, counts) for e in edge]
        else:
            if edge in counts:
                count = counts[edge]
                counts[edge] += 1
            else:
                count = 0
                counts[edge] = 1

            tcount = 0
            for token in self.sentence.tokens:
                if edge == '+':
                    return Token('+')
                if token.word == str(edge):
                    if count == tcount:
                        return token
                    else:
                        tcount += 1

    def best_node_subedge_fit(self, tedge, outcome_word_counts, path=None):
        if not path:
            path = []

        if type(tedge) is Token:
            word_counts = {tedge.word: 1}
            return path, word_counts

        word_counts = {}
        path_wcs = []
        for i in range(len(tedge)):
            down_path = path[:]
            down_path.append(i)
            path_wc = self.best_node_subedge_fit(tedge[i], outcome_word_counts, down_path)
            path_wcs.append(path_wc)
            add_word_counts(word_counts, path_wc[1])

        best_path_wc = (path, word_counts)
        best_fit = Fit(word_counts, outcome_word_counts)
        for path_wc in path_wcs:
            fit = Fit(path_wc[1], outcome_word_counts)
            if fit.better_than(best_fit):
                best_fit = fit
                best_path_wc = path_wc

        return best_path_wc

    def find_subedge(self, node, tedge=None):
        if not tedge:
            tedge = self.build_tedge(self.outcome)

        tokens = node.all_tokens()
        word_counts = {}
        for token in tokens:
            if token.word in word_counts:
                word_counts[token.word] += 1
            else:
                word_counts[token.word] = 1

        path, _ = self.best_node_subedge_fit(tedge, word_counts)
        return path

    def find_path(self, node):
        epath = self.find_subedge(node)
        if epath is None:
            return None

        words_path = [token.word for token in tedge2tokens(path2tedge(epath, self.build_tedge(self.outcome)))]
        words_edge = [token.word for token in node.all_tokens()]
        for word in words_edge:
            if words_edge.count(word) > words_path.count(word):
                return None

        return epath

    def common_path(self, parent_node, child_node):
        parent_path = self.find_path(parent_node)
        if parent_path is None:
            return None
        child_path = self.find_path(child_node)
        if child_path is None:
            return None

        cp = common_path(parent_path, child_path)
        return path2tedge(cp, self.build_tedge(self.outcome))

    def infer_transformation(self, parent, child, position):
        common_tedge = self.common_path(parent, child)
        if common_tedge is None:
            return Transformation.IGNORE

        print('[^] %s' % tedge2str(common_tedge))

        best_score = 0
        best_transf = Transformation.IGNORE

        score = score_transformation(parent, child, position, common_tedge, Transformation.APPLY)
        if score > best_score:
            best_score = score
            best_transf = Transformation.APPLY
        score = score_transformation(parent, child, position, common_tedge, Transformation.NEST)
        if score > best_score:
            best_score = score
            best_transf = Transformation.NEST
        score = score_transformation(parent, child, position, common_tedge, Transformation.SHALLOW)
        if score > best_score:
            best_score = score
            best_transf = Transformation.SHALLOW
        score = score_transformation(parent, child, position, common_tedge, Transformation.DEEP)
        if score > best_score:
            best_score = score
            best_transf = Transformation.DEEP
        score = score_transformation(parent, child, position, common_tedge, Transformation.FIRST)
        if score > best_score:
            best_score = score
            best_transf = Transformation.FIRST
        score = score_transformation(parent, child, position, common_tedge, Transformation.APPLY_R)
        if score > best_score:
            best_score = score
            best_transf = Transformation.APPLY_R
        score = score_transformation(parent, child, position, common_tedge, Transformation.APPLY_L)
        if score > best_score:
            best_score = score
            best_transf = Transformation.APPLY_L
        score = score_transformation(parent, child, position, common_tedge, Transformation.NEST_R)
        if score > best_score:
            best_score = score
            best_transf = Transformation.NEST_R
        score = score_transformation(parent, child, position, common_tedge, Transformation.NEST_L)
        if score > best_score:
            best_score = score
            best_transf = Transformation.NEST_L
        score = score_transformation(parent, child, position, common_tedge, Transformation.DEEP_R)
        if score > best_score:
            best_score = score
            best_transf = Transformation.DEEP_R
        score = score_transformation(parent, child, position, common_tedge, Transformation.DEEP_L)
        if score > best_score:
            best_transf = Transformation.DEEP_L

        return best_transf

    def show_option(self, key, name, parent, child, position, transf):
        res = test_transformation_str(parent, child, position, transf)
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

        self.show_option('i', 'IGNORE', parent, child, position, Transformation.IGNORE)
        self.show_option('a', 'APPLY', parent, child, position, Transformation.APPLY)
        self.show_option('n', 'NEST', parent, child, position, Transformation.NEST)
        self.show_option('f', 'FIRST', parent, child, position, Transformation.FIRST)
        self.show_option('s', 'SHALLOW', parent, child, position, Transformation.SHALLOW)
        self.show_option('d', 'DEEP', parent, child, position, Transformation.DEEP)

        print('')

        if position == Position.LEFT:
            self.show_option('ar', 'APPLY_R', parent, child, position, Transformation.APPLY_R)
            self.show_option('nr', 'NEST_R', parent, child, position, Transformation.NEST_R)
            self.show_option('dr', 'DEEP_R', parent, child, position, Transformation.DEEP_R)

        if position == Position.RIGHT:
            self.show_option('al', 'APPLY_L', parent, child, position, Transformation.APPLY_L)
            self.show_option('nl', 'NEST_L', parent, child, position, Transformation.NEST_L)
            self.show_option('dl', 'DEEP_L', parent, child, position, Transformation.DEEP_L)

        print('\nr) RESTART    x) ABORT')

        choice = input('> ').lower()

        if choice == 'i':
            return Transformation.IGNORE
        if choice == 'a':
            return with_position(Transformation.APPLY, position)
        if choice == 'ar':
            return Transformation.APPLY_R
        if choice == 'al':
            return Transformation.APPLY_L
        if choice == 'n':
            return with_position(Transformation.NEST, position)
        if choice == 'nr':
            return Transformation.NEST_R
        if choice == 'nl':
            return Transformation.NEST_L
        if choice == 's':
            return Transformation.SHALLOW
        if choice == 'f':
            return Transformation.FIRST
        if choice == 'd':
            return with_position(Transformation.DEEP, position)
        if choice == 'dr':
            return Transformation.DEEP_R
        if choice == 'dl':
            return Transformation.DEEP_L
        if choice == 'r':
            self.restart = True
            return Transformation.IGNORE
        if choice == 'x':
            self.abort = True
            return Transformation.IGNORE
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
            if t == Transformation.NEST or t == Transformation.NEST_R or t == Transformation.NEST_L:
                nested_left = True
        for child_token in token.right_children:
            self.process_token(child_token, token, elem_id, Position.RIGHT)
            if self.restart or self.abort:
                return -1, -1

        # infer and apply transformation
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
                # transf = self.infer_transformation(parent, self.tree.get(elem_id), position)
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
            if transf == Transformation.APPLY:
                print('apply')
                parent.apply(elem_id, position)
            elif transf == Transformation.APPLY_R:
                print('apply [R]')
                parent.apply(elem_id, Position.RIGHT)
            elif transf == Transformation.APPLY_L:
                print('apply [L]')
                parent.apply(elem_id, Position.LEFT)
            elif transf == Transformation.NEST:
                print('nest')
                parent.nest(elem_id, position)
            elif transf == Transformation.NEST_R:
                print('nest [R]')
                parent.nest(elem_id, Position.RIGHT)
            elif transf == Transformation.NEST_L:
                print('nest [L]')
                parent.nest(elem_id, Position.LEFT)
            elif transf == Transformation.SHALLOW:
                print('shallow')
                parent.nest_shallow(elem_id)
            elif transf == Transformation.FIRST:
                print('first')
                parent.insert(elem_id, Position.LEFT)
            elif transf == Transformation.DEEP:
                print('deep')
                parent.nest_deep(elem_id, position)
            elif transf == Transformation.DEEP_R:
                print('deep [R]')
                parent.nest_deep(elem_id, Position.RIGHT)
            elif transf == Transformation.DEEP_L:
                print('deep [L]')
                parent.nest_deep(elem_id, Position.LEFT)
            elif transf == Transformation.SHALLOW:
                print('shallow')
                parent.nest_shallow(elem_id, position)
            else:
                print('ignore')
            print(self.tree.get(parent_id))
            print()

        return elem_id, transf

    def generate(self, sentence_str, outcome_str=None):
        self.tree = Tree()
        self.sentence_str = sentence_str
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
            if len(current_parse) == 3:
                parses.append(current_parse)
                current_parse = []

    total = 0
    correct = 0
    cg = CaseGenerator()
    for parse in parses:
        sentence_str = parse[0].strip()
        outcome_str = parse[1].strip()
        cg.transfs = [int(token) for token in parse[2].split(',')]
        cg.cases = []
        cg.generate(sentence_str, outcome_str)
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
                    f.write('%s\n' % outcome)
                    f.write('%s\n' % ','.join([str(transf) for transf in cg.transfs]))
                    f.close()


if __name__ == '__main__':
    generate_cases('parses1.txt', 'dummy.txt')
