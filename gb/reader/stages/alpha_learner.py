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
from gb.nlp.nlp_token import Token
from gb.reader.semantic_tree import Tree
from gb.reader.semantic_tree import Position


class Transformation(object):
    IGNORE, GROW, APPLY, NEST, NEST_DEEP = range(5)


def transf2str(transf):
    if transf == Transformation.IGNORE:
        return 'ignore'
    elif transf == Transformation.GROW:
        return 'grow'
    elif transf == Transformation.APPLY:
        return 'apply'
    elif transf == Transformation.NEST:
        return 'nest'
    elif transf == Transformation.NEST_DEEP:
        return 'nest_deep'
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

    score = match_score(test_parent.children()[test_index], target_tedge[target_index]) + \
            match_score(test_parent, target_tedge, test_index + 1, target_index + 1)
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
        test_parent.apply_(child.id, position)
    elif transf == Transformation.GROW:
        test_parent.grow_(child.id, position)
    elif transf == Transformation.NEST:
        test_parent.nest_(child.id, position)
    elif transf == Transformation.NEST_DEEP:
        test_parent.nest_deep(child.id, position)
    return test_tree.root()


def score_transformation(parent, child, position, common_tedge, transf):
    test_parent = test_transformation(parent, child, position, transf)
    return match_score(test_parent, common_tedge)


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


CASE_FIELDS = ('transformation', 'child_pos', 'child_dep', 'parent_pos', 'parent_dep')


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
            if word != '+' and word != '':
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
        score = score_transformation(parent, child, position, common_tedge, Transformation.GROW)
        if score > best_score:
            best_score = score
            best_transf = Transformation.GROW
        score = score_transformation(parent, child, position, common_tedge, Transformation.NEST)
        if score > best_score:
            best_score = score
            best_transf = Transformation.NEST
        score = score_transformation(parent, child, position, common_tedge, Transformation.NEST_DEEP)
        if score > best_score:
            best_transf = Transformation.NEST_DEEP

        return best_transf

    def choose_transformation(self, parent, child, position):
        print('target sentence:')
        print(self.sentence_str)
        print('parent <- child')
        print('%s <- %s' % (parent, child))

        test_node = test_transformation(parent, child, position, Transformation.IGNORE)
        print('0) IGNORE -> %s' % test_node.tree.to_hyperedge_str(with_namespaces=False))

        test_node = test_transformation(parent, child, position, Transformation.APPLY)
        print('1) APPLY -> %s' % test_node.tree.to_hyperedge_str(with_namespaces=False))

        test_node = test_transformation(parent, child, position, Transformation.GROW)
        print('2) GROW -> %s' % test_node.tree.to_hyperedge_str(with_namespaces=False))

        test_node = test_transformation(parent, child, position, Transformation.NEST)
        print('3) NEST -> %s' % test_node.tree.to_hyperedge_str(with_namespaces=False))

        test_node = test_transformation(parent, child, position, Transformation.NEST_DEEP)
        print('4) NEST_DEEP -> %s' % test_node.tree.to_hyperedge_str(with_namespaces=False))

        choice = int(input('> '))

        if choice == 0:
            return Transformation.IGNORE
        if choice == 1:
            return Transformation.APPLY
        if choice == 2:
            return Transformation.GROW
        if choice == 3:
            return Transformation.NEST
        if choice == 4:
            return Transformation.NEST_DEEP
        else:
            return Transformation.IGNORE

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
            if t == Transformation.NEST:
                nested_left = True
        for child_token in token.right_children:
            self.process_token(child_token, token, elem_id, Position.RIGHT)

        # infer and apply transformation
        transf = -1
        if parent_token:
            parent = self.tree.get(parent_id)
            if self.interactive:
                transf = self.choose_transformation(parent, self.tree.get(elem_id), position)
            else:
                transf = self.infer_transformation(parent, self.tree.get(elem_id), position)
            print('%s <- %s' % (parent_token.word, token.word))
            print('%s <- %s' % (parent, self.tree.get(elem_id)))
            if parent.is_node():
                print('inn: %s' % str(parent.get_inner_nested_node()))
            if transf == Transformation.GROW:
                print('grow')
                parent.grow_(elem_id, position)
            elif transf == Transformation.APPLY:
                print('apply')
                parent.apply_(elem_id, position)
            elif transf == Transformation.NEST:
                print('nest')
                parent.nest_(elem_id, position)
            elif transf == Transformation.NEST_DEEP:
                print('nest_deep')
                parent.nest_deep(elem_id, position)
            else:
                print('ignore')
                pass
            print(self.tree.get(parent_id))
            print()

            # add case
            if parent_token:
                case = {'transformation': transf2str(transf),
                        'child_pos': token.pos,
                        'child_dep': token.dep,
                        'parent_pos': parent_token.pos,
                        'parent_dep': parent_token.dep}
                self.cases.append(case)

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
            values = [case[field] for field in CASE_FIELDS]
            f = open(outfile, 'a')
            f.write('%s\n' % ','.join(values))
            f.close()


def generate_cases(infile, outfile):
    f = open(outfile, 'w')
    f.write('%s\n' % ','.join(CASE_FIELDS))
    f.close()

    current_parse = []
    parses = []
    with open(infile) as f:
        for line in f:
            current_parse.append(line)
            if len(current_parse) == 2:
                parses.append(current_parse)
                current_parse = []

    total = 0
    correct = 0
    cg = CaseGenerator()
    for parse in parses:
        sentence_str = parse[0].strip()
        outcome_str = parse[1].strip()
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
        cg.generate(sentence_str)
        outcome = cg.tree.to_hyperedge_str(with_namespaces=False)
        print('outcome:')
        print(outcome)
        write = input('write [y/N]? ')
        if write == 'y':
            f = open(outfile, 'a')
            f.write('%s\n' % sentence_str)
            f.write('%s\n' % outcome)
            f.close()


if __name__ == '__main__':
    generate_cases('parses1.txt', 'dummy.txt')
