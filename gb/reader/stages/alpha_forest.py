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


import pickle
import pandas as pd
from sklearn.ensemble import RandomForestClassifier
import gb.nlp.constants as nlp_consts
from gb.nlp.parser import Parser
from gb.nlp.sentence import Sentence
from gb.reader.semantic_tree import Position, Tree
from gb.reader.parser_output import ParserOutput
from gb.reader.stages.common import Transformation


# CASE_FIELDS = ('transformation', 'child_pos', 'child_dep', 'child_tag',
#                'parent_pos', 'parent_dep', 'parent_tag', 'child_position')

CASE_FIELDS = ('transformation',
               'child_dep', 'child_tag', 'parent_dep', 'parent_tag',
               'child_head_dep', 'child_head_tag', 'parent_head_dep', 'parent_head_tag',
               'child_position',
               'child_is_atom', 'parent_is_atom')

# CASE_FIELDS = ('transformation', 'child_pos', 'child_dep', 'parent_pos', 'parent_dep', 'child_position')


def generate_fields(prefix, values):
    return ['%s_%s' % (prefix, value) for value in values]


def expanded_fields():
    fields = []
    for field in CASE_FIELDS:
        if field[-4:] == '_pos':
            fields += generate_fields(field, nlp_consts.POS_TAGS)
        elif field[-4:] == '_dep':
            fields += generate_fields(field, nlp_consts.DEPENDENCY_LABELS)
        elif field[-4:] == '_tag':
            fields += generate_fields(field, nlp_consts.TAGS)
        else:
            fields.append(field)
    return fields


def set_case_field_on(case, field):
    if field in case:
        case[field] = 1.


def build_case(parent, child, parent_token, child_token, position):
    case = {}
    fields = expanded_fields()
    for field in fields:
        case[field] = 0.

    # position
    if position == Position.LEFT:
        set_case_field_on(case, 'child_position')

    # token tags and dependencies
    set_case_field_on(case, 'child_pos_%s' % child_token.pos)
    set_case_field_on(case, 'child_dep_%s' % child_token.dep)
    if child_token.tag != ',':
        set_case_field_on(case, 'child_tag_%s' % child_token.tag)
    set_case_field_on(case, 'parent_pos_%s' % parent_token.pos)
    set_case_field_on(case, 'parent_dep_%s' % parent_token.dep)
    if parent_token.tag != ',':
        set_case_field_on(case, 'parent_tag_%s' % parent_token.tag)

    # head tags and dependencies
    if child.is_node():
        head = child.get_child(0)
        if head.is_leaf():
            set_case_field_on(case, 'child_head_pos_%s' % head.token.pos)
            set_case_field_on(case, 'child_head_dep_%s' % head.token.dep)
            if child_token.tag != ',':
                set_case_field_on(case, 'child_tag_%s' % head.token.tag)
    if parent.is_node():
        head = parent.get_child(0)
        if head.is_leaf():
            set_case_field_on(case, 'parent_head_pos_%s' % head.token.pos)
            set_case_field_on(case, 'parent_head_dep_%s' % head.token.dep)
            if parent_token.tag != ',':
                set_case_field_on(case, 'parent_head_tag_%s' % head.token.tag)

    # parent and node are atoms?
    if parent.is_leaf():
        set_case_field_on(case, 'parent_is_atom')
    if child.is_leaf():
        set_case_field_on(case, 'child_is_atom')

    return case


def learn(infile, outfile):
    train = pd.read_csv(infile)

    feature_cols = train.columns.values[1:]
    target_cols = [train.columns.values[0]]

    features = train.as_matrix(feature_cols)
    targets = train.as_matrix(target_cols)

    rf = RandomForestClassifier(n_estimators=100)
    rf.fit(features, targets)

    score = rf.score(features, targets)
    print('score: %s' % score)

    with open(outfile, 'wb') as f:
        pickle.dump(rf, f)


class AlphaForest(object):
    def __init__(self, model_file='alpha_forest.model'):
        self.tree = Tree()
        with open(model_file, 'rb') as f:
            self.rf = pickle.load(f)

    def predict_transformation(self, parent, child, parent_token, child_token, position):
        fields = expanded_fields()
        case = build_case(parent, child, parent_token, child_token, position)
        values = [[case[field] for field in fields[1:]]]
        data = pd.DataFrame(values, columns=fields[1:])
        data = data.as_matrix(data.columns.values)
        pred = self.rf.predict(data)
        return pred[0]

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
            if t == Transformation.NEST_R or t == Transformation.NEST_L:
                nested_left = True
        for child_token in token.right_children:
            self.process_token(child_token, token, elem_id, Position.RIGHT)

        # infer and apply transformation
        transf = -1
        if parent_token:
            parent = self.tree.get(parent_id)
            child = self.tree.get(elem_id)
            transf = self.predict_transformation(parent, child, parent_token, token, position)
            if transf == Transformation.APPLY:
                parent.apply(elem_id, position)
            elif transf == Transformation.APPLY_R:
                parent.apply(elem_id, Position.RIGHT)
            elif transf == Transformation.APPLY_L:
                parent.apply(elem_id, Position.LEFT)
            elif transf == Transformation.NEST:
                parent.nest(elem_id, position)
            elif transf == Transformation.NEST_R:
                parent.nest(elem_id, Position.RIGHT)
            elif transf == Transformation.NEST_L:
                parent.nest(elem_id, Position.LEFT)
            elif transf == Transformation.SHALLOW:
                parent.nest_shallow(elem_id)
            elif transf == Transformation.DEEP:
                parent.nest_deep(elem_id, position)
            elif transf == Transformation.DEEP_R:
                parent.nest_deep(elem_id, Position.RIGHT)
            elif transf == Transformation.DEEP_L:
                parent.nest_deep(elem_id, Position.LEFT)
            else:
                pass

        return elem_id, transf

    def process_sentence(self, sentence):
        self.tree.root_id = self.process_token(sentence.root())[0]
        return ParserOutput(sentence, self.tree)


def transform(sentence):
    alpha = AlphaForest()
    return alpha.process_sentence(sentence)


if __name__ == '__main__':
    # learn('cases.csv', 'alpha_forest.model')

    test_text = """
        Satellites from NASA and other agencies have been tracking sea ice changes since 1979.
        """
    # test_text = 'Telmo is going to the gym.'

    print('Starting parser...')
    parser = Parser()
    print('Parsing...')
    result = parser.parse_text(test_text)

    for r in result:
        s = Sentence(r[1])
        t = transform(s)
        print(t.tree.to_hyperedge_str(with_namespaces=False))
