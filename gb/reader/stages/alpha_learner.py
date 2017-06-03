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
    GROW, APPLY, NEST, IGNORE = range(4)


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


def score_transformation(parent, child, position, common_tedge, child_token, transf):
    test_tree = Tree(parent)
    test_tree.import_element(child)
    test_parent = test_tree.root()
    if transf == Transformation.APPLY:
        test_parent.apply_(child.id, position)
    elif transf == Transformation.GROW:
        test_parent.grow_(child.id, position)
    elif transf == Transformation.NEST:
        test_parent.nest_(child.id, position, child_token)
    test_parent = test_tree.root()
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


class CaseGenerator(object):
    def __init__(self):
        self.tree = None
        self.parser = Parser()
        self.sentence = None
        self.outcome = None
        self.outcome_str = None

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
                if token.word == edge:
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

    def infer_transformation(self, parent, child, child_token, position):
        common_tedge = self.common_path(parent, child)
        if common_tedge is None:
            return Transformation.IGNORE

        print('[^] %s' % tedge2str(common_tedge))

        best_score = 0
        best_transf = Transformation.IGNORE

        score = score_transformation(parent, child, position, common_tedge, child_token, Transformation.APPLY)
        if score > best_score:
            best_score = score
            best_transf = Transformation.APPLY
        score = score_transformation(parent, child, position, common_tedge, child_token, Transformation.GROW)
        if score > best_score:
            best_score = score
            best_transf = Transformation.GROW
        score = score_transformation(parent, child, position, common_tedge, child_token, Transformation.NEST)
        if score > best_score:
            best_score = score
            best_transf = Transformation.NEST

        return best_transf

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
            transf = self.infer_transformation(parent, self.tree.get(elem_id), token, position)
            print('%s <- %s' % (parent_token.word, token.word))
            print('%s <- %s' % (parent, self.tree.get(elem_id)))
            if parent.is_node():
                print('inn: %s' % str(parent.get_inner_nested_node(parent.id)))
            if transf == Transformation.GROW:
                print('grow')
                parent.grow_(elem_id, position)
            elif transf == Transformation.APPLY:
                print('apply')
                parent.apply_(elem_id, position)
            elif transf == Transformation.NEST:
                print('nest')
                parent.nest_(elem_id, position, token)
            else:
                print('ignore')
                pass
            print(self.tree.get(parent_id))
            print()

        return elem_id, transf

    def generate(self, sentence_str, outcome_str):
        self.tree = Tree()
        self.sentence = Sentence(self.parser.parse_text(sentence_str)[0][1])
        self.sentence.print_tree()
        self.outcome_str = outcome_str
        self.outcome = ed.str2edge(outcome_str)
        self.toutcome = self.build_tedge(self.outcome)
        self.tree.root_id, _ = self.process_token(self.sentence.root())

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
