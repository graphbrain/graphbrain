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


def tedge_depth(tedge):
    if type(tedge) is Token:
        return 0
    return max([tedge_depth(t) for t in tedge]) + 1


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


def score_transformation(test_parent, target_tedge, test_index=0, target_index=0, dist=0):
    if test_parent.is_leaf():
        if type(target_tedge) is Token:
            if test_parent.token.word == target_tedge.word:
                return 1000 - dist
            else:
                return 0
        else:
            best_score = 0
            for tedge in target_tedge:
                score = score_transformation(test_parent, tedge, 0, 0, dist + 1)
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
            score = score_transformation(test_parent, tedge)
            best_score = max(score, best_score)

    score = score_transformation(test_parent.children()[test_index], target_tedge[target_index]) +\
            score_transformation(test_parent, target_tedge, test_index + 1, target_index + 1)
    best_score = max(score, best_score)
    score = score_transformation(test_parent, target_tedge, test_index, target_index + 1, dist + 1)
    best_score = max(score, best_score)

    return best_score


class Fit(object):
    def __init__(self, word_counts, outcome_word_counts):
        self.matches = 0
        self.size = 0
        for word in word_counts:
            if word in outcome_word_counts:
                self.matches += min(word_counts[word], outcome_word_counts[word])
            self.size += word_counts[word]
        self.complete_match = True
        for word in outcome_word_counts:
            if word in word_counts:
                if word_counts[word] < outcome_word_counts[word]:
                    self.complete_match = False
            else:
                self.complete_match = False

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
        self.counter = -1

    def find_word(self, edge, token, path=None):
        if not path:
            path = []
            self.counter = 0
            for i in range(token.position_in_sentence):
                if self.sentence.tokens[i].word == token.word:
                    self.counter += 1

        if sym.is_edge(edge):
            for i in range(len(edge)):
                down_path = path[:]
                down_path.append(i)
                res = self.find_word(edge[i], token, down_path)
                if res:
                    return res
        else:
            if edge == token.word:
                if self.counter == 0:
                    return path
                else:
                    self.counter -= 1

        return None

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
            return path, word_counts, tedge

        word_counts = {}
        path_wcs = []
        for i in range(len(tedge)):
            down_path = path[:]
            down_path.append(i)
            path_wc = self.best_node_subedge_fit(tedge[i], outcome_word_counts, down_path)
            path_wcs.append(path_wc)
            add_word_counts(word_counts, path_wc[1])

        best_path_wc = (path, word_counts, tedge)
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

        path_wc = self.best_node_subedge_fit(tedge, word_counts)
        path_fit = (path_wc[0], Fit(path_wc[1], word_counts), path_wc[2])
        if path_fit[1]:
            return path_fit
        else:
            return None

    def is_enclosed(self, node, tedge):
        path_fit = self.find_subedge(node, tedge)
        if path_fit:
            return path_fit[1].complete_match
        else:
            return False

    def find_parent_and_child(self, edge, parent_node, child_node, parent_token, child_token):
        res = self.find_subedge(parent_node)
        if not res:
            return None
        parent_epath, _, parent_tedge = res
        res = self.find_subedge(child_node)
        if not res:
            return None
        child_epath, _, child_tedge = res

        parent_path = self.find_word(edge, parent_token)
        if not parent_path:
            return None
        child_path = self.find_word(edge, child_token)
        if not child_path:
            return None

        parent_eindex = 0
        child_eindex = 0
        if len(parent_epath) > 0:
            parent_eindex = parent_epath[-1]
        if len(child_epath) > 0:
            child_eindex = child_epath[-1]

        parent_edge = tedge2str(parent_tedge)
        child_edge = tedge2str(child_tedge)

        parent_enclosed = False
        child_enclosed = False
        if parent_token in tedge2tokens(child_tedge):
            parent_enclosed = True
        if child_token in tedge2tokens(parent_tedge):
            child_enclosed = True

        parent_eenclosed = False
        child_eenclosed = False
        if self.is_enclosed(parent_node, child_tedge):
            parent_eenclosed = True
        if self.is_enclosed(child_node, parent_tedge):
            child_eenclosed = True

        return {'parent': {'depth': len(parent_path), 'index': parent_path[-1], 'path': parent_path[:-1],
                           'enclosed': parent_enclosed, 'max_depth': len(parent_epath) + tedge_depth(parent_tedge),
                           'edepth': len(parent_epath), 'eindex': parent_eindex, 'epath': parent_epath,
                           'edge': parent_edge, 'tedge': parent_tedge, 'eenclosed': parent_eenclosed},
                'child': {'depth': len(child_path), 'index': child_path[-1], 'path': child_path[:-1],
                          'enclosed': child_enclosed, 'max_depth': len(child_epath) + tedge_depth(child_tedge),
                          'edepth': len(child_epath), 'eindex': child_eindex, 'epath': child_epath,
                          'edge': child_edge, 'tedge': child_tedge, 'eenclosed': child_eenclosed}}

    def infer_transformation(self, parent, child, parent_token, child_token, position):
        positions = self.find_parent_and_child(self.outcome, parent, child, parent_token, child_token)
        if not positions:
            return Transformation.IGNORE
        print('[ed] %s <- %s' % (positions['parent']['edge'], positions['child']['edge']))
        print('[pt] %s <- %s' % (positions['parent']['epath'], positions['child']['epath']))

        cp = common_path(positions['parent']['epath'], positions['child']['epath'])
        common_tedge = path2tedge(cp, self.build_tedge(self.outcome))

        print('[^] %s' % tedge2str(common_tedge))

        best_score = 0
        best_transf = Transformation.IGNORE

        test_tree = Tree(parent)
        test_tree.import_element(child)
        test_parent = test_tree.root()
        test_parent.apply_(child.id, position)
        test_parent = test_tree.root()
        score = score_transformation(test_parent, common_tedge)
        if score > best_score:
            best_score = score
            best_transf = Transformation.APPLY

        test_tree = Tree(parent)
        test_tree.import_element(child)
        test_parent = test_tree.root()
        test_parent.grow_(child.id, position)
        test_parent = test_tree.root()
        score = score_transformation(test_parent, common_tedge)
        if score > best_score:
            best_score = score
            best_transf = Transformation.GROW

        test_tree = Tree(parent)
        test_tree.import_element(child)
        test_parent = test_tree.root()
        test_parent.nest_(child.id, position, child_token)
        test_parent = test_tree.root()
        score = score_transformation(test_parent, common_tedge)
        if score > best_score:
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
            transf = self.infer_transformation(parent, self.tree.get(elem_id), parent_token, token, position)
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
    # cg = CaseGenerator()
    # edge = ed.str2edge('(might (be (some (of subspecies mosquito)) (1st (to (be (genetically (out wiped)))))))')
    # sentence = cg.parser.parse_text('Some subspecies of mosquito might be 1st to be genetically wiped out.')[0][1]
    # cg.find_parent_and_child_(edge, 'be', '1st', candidates)
    # print(cg.find_word(sentence, edge, sentence[8]))

    generate_cases('parses.txt')

    # sentence_str = 'Some subspecies of mosquito might be 1st to be genetically wiped out.'
    # outcome_str = '(might (be (some (of subspecies mosquito)) (1st (to (be (genetically (out wiped)))))))'
    # cg = CaseGenerator()
    # cg.generate(sentence_str, outcome_str)
    # print(outcome_str)
    # print(cg.tree.to_hyperedge_str(with_namespaces=False))
