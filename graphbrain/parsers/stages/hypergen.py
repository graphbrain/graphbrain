import csv
import numpy as np
import pkg_resources
import pickle
from collections import Counter
from sklearn.ensemble import RandomForestClassifier
import graphbrain.nlp.constants as nlp_consts
from graphbrain.nlp.parser import Parser
from graphbrain.nlp.sentence import Sentence
from graphbrain.parsers.semantic_tree import Position, Tree
from graphbrain.parsers.parser_output import ParserOutput


IGNORE, APPLY_HYPEREDGE, NEST_HYPEREDGE, APPLY_TOKEN, NEST_TOKEN, HEAD = range(6)


def apply_transformation(parent, root, child_id, pos, transf):
    if transf == IGNORE:
        pass
    elif transf == APPLY_HYPEREDGE:
        if pos == Position.LEFT:
            parent.apply_head(child_id)
        else:
            parent.apply_tail(child_id)
    elif transf == NEST_HYPEREDGE:
        parent.nest(child_id)
    elif transf == APPLY_TOKEN:
        root.apply_tail(child_id)
    elif transf == NEST_TOKEN:
        root.nest(child_id)
    elif transf == HEAD:
        parent.reverse_apply(child_id)


def transformation_to_string(transf):
    if transf == IGNORE:
        return 'ignore'
    elif transf == APPLY_HYPEREDGE:
        return 'apply hyperedge'
    elif transf == NEST_HYPEREDGE:
        return 'nest hyperedge'
    elif transf == APPLY_TOKEN:
        return 'apply token'
    elif transf == NEST_TOKEN:
        return 'nest token'
    elif transf == HEAD:
        return 'head'
    else:
        return '?'


CASE_FIELDS = ('transformation', 'child_pos', 'child_dep', 'child_tag', 'parent_pos', 'parent_dep', 'parent_tag',
               'child_edge_pos', 'child_edge_dep', 'child_edge_tag', 'child_edge_depth',
               'parent_edge_pos', 'parent_edge_dep', 'parent_edge_tag', 'parent_edge_depth',
               'child_position', 'child_is_atom', 'parent_is_atom')


RANDOM_FOREST_MODEL_FILE = 'hypergen_random_forest.model'
NEURAL_NETWORK_MODEL_FILE = 'hypergen_neural_network.model'


def read_parses(infile, test_set=False):
    current_parse = []
    parses = []
    with open(infile, encoding='utf-8') as f:
        i = 0
        for line in f:
            current_parse.append(line)
            if len(current_parse) == 4:
                if (i % 4 == 0 and test_set) or (i % 4 != 0 and not test_set):
                    parses.append(current_parse)
                current_parse = []
                i += 1
    return parses


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


def entity2case(entity, case, prefix):
    if entity.is_leaf():
        set_case_field_on(case, '%s_all_tag_%s' % (prefix, entity.token.tag))
        set_case_field_on(case, '%s_all_dep_%s' % (prefix, entity.token.dep))
    else:
        for child in entity.children():
            entity2case(child, case, prefix)


def set_case_fields_edge(elem, case, feature, depth=0):
    set_case_field_on(case, '%s_depth_%s' % (feature, depth))
    if elem.is_node():
        for child in elem.children():
            set_case_fields_edge(child, case, feature, depth + 1)
    else:
        set_case_field_on(case, '%s_pos_%s' % (feature, elem.token.pos))
        set_case_field_on(case, '%s_dep_%s' % (feature, elem.token.dep))
        if elem.token.tag != ',':
            set_case_field_on(case, '%s_tag_%s' % (feature, elem.token.tag))


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
                set_case_field_on(case, 'child_head_tag_%s' % head.token.tag)
    if parent.is_node():
        head = parent.get_child(0)
        if head.is_leaf():
            set_case_field_on(case, 'parent_head_pos_%s' % head.token.pos)
            set_case_field_on(case, 'parent_head_dep_%s' % head.token.dep)
            if parent_token.tag != ',':
                set_case_field_on(case, 'parent_head_tag_%s' % head.token.tag)

    # edge depths, tags and dependencies
    set_case_fields_edge(child, case, 'child_edge')
    set_case_fields_edge(parent, case, 'child_parent')

    # all tags and dependencies
    entity2case(parent, case, 'parent')
    entity2case(child, case, 'child')

    # parent and child are atoms?
    if parent.is_leaf():
        set_case_field_on(case, 'parent_is_atom')
    if child.is_leaf():
        set_case_field_on(case, 'child_is_atom')

    return case


def read_cases_file(filename):
    with open(filename, 'r') as dest_f:
        row_iter = csv.reader(dest_f, delimiter=',')
        data = [row for row in row_iter]
    features = [row[1:] for row in data[1:]]
    targets = [row[0] for row in data[1:]]
    features = np.asarray(features, dtype='float64')
    targets = np.asarray(targets, dtype='float64')
    return features, targets


def learn_rf(infile, outfile):
    features, targets = read_cases_file(infile)

    rf = RandomForestClassifier(n_estimators=50)
    rf.fit(features, targets)

    score = rf.score(features, targets)
    print('score: %s' % score)

    # save model
    if outfile is None:
        outfile = RANDOM_FOREST_MODEL_FILE
    with open(outfile, 'wb') as f:
        pickle.dump(rf, f)


def learn_nn(infile, outfile):
    import keras

    features, targets = read_cases_file(infile)
    n_features = features.shape[1]

    model = keras.models.Sequential()
    model.add(keras.layers.Dense(units=n_features, input_dim=n_features))
    model.add(keras.layers.Activation('relu'))
    for i in range(2):
        model.add(keras.layers.Dense(units=n_features))
        model.add(keras.layers.Activation('relu'))
    model.add(keras.layers.Dense(units=10))
    model.add(keras.layers.Activation('softmax'))
    model.compile(optimizer='rmsprop',
                  loss='categorical_crossentropy',
                  metrics=['accuracy'])

    # Convert labels to categorical one-hot encoding
    one_hot_targets = keras.utils.to_categorical(targets, num_classes=10)
    model.fit(features, one_hot_targets, epochs=100, batch_size=32)

    # save model to file
    if outfile is None:
        outfile = NEURAL_NETWORK_MODEL_FILE
    model.save(outfile)


def learn(infile, outfile=None, model_type='rf'):
    if model_type == 'nn':
        learn_nn(infile, outfile)
    elif model_type == 'rf':
        learn_rf(infile, outfile)
    else:
        raise ValueError('unknown machine learning model type: %s' % model_type)


class Hypergen(object):
    def __init__(self, model_file=None, model_type='rf'):
        self.name = 'hypergen'
        self.model_type = model_type
        self.tree = Tree()
        self.transfs = None
        self.wrong = 0
        self.test_predictions = Counter()
        self.test_true_values = Counter()

        if model_type == 'nn':
            import keras
            if model_file is None:
                model_file = pkg_resources.resource_filename('graphbrain.data', NEURAL_NETWORK_MODEL_FILE)
            self.nn = keras.models.load_model(model_file)
        elif model_type == 'rf':
            if model_file is None:
                model_file = pkg_resources.resource_filename('graphbrain.data', RANDOM_FOREST_MODEL_FILE)
            with open(model_file, 'rb') as f:
                self.rf = pickle.load(f)
        else:
            raise ValueError('unknown machine learning model type: %s' % model_type)

    def predict_transformation(self, parent, child, parent_token, child_token, position):
        fields = expanded_fields()
        case = build_case(parent, child, parent_token, child_token, position)
        values = [[case[field] for field in fields[1:]]]
        data = np.asarray(values, dtype='float64')
        if self.model_type == 'nn':
            output = self.nn.predict(data)[0]
            max_out = 0.
            transf = -1
            for i in range(10):
                if output[i] > max_out:
                    max_out = output[i]
                    transf = i
            return transf
        elif self.model_type == 'rf':
            pred = self.rf.predict(data)
            return pred[0]

    def process_token(self, token, parent_token=None, parent_id=None, position=None, testing=False):
        elem = self.tree.create_leaf(token)
        elem_id = elem.id

        # process children first
        for child_token in token.left_children:
            _, t = self.process_token(child_token, token, elem_id, Position.LEFT, testing)
        for child_token in token.right_children:
            self.process_token(child_token, token, elem_id, Position.RIGHT, testing)

        # predict and apply transformation
        transf = -1
        if parent_token:
            parent = self.tree.get(parent_id)
            root = self.tree.token2leaf(parent_token)
            child = self.tree.get(elem_id)
            transf = self.predict_transformation(parent, child, parent_token, token, position)

            if testing:
                test_tree = parent.tree.clone()
                test_parent = test_tree.get(parent.id)
                test_root = test_tree.token2leaf(parent_token)
                apply_transformation(test_parent, test_root, elem_id, position, transf)
                apply_transformation(parent, root, elem_id, position, self.transfs[0])
                self.test_predictions[transf] += 1
                if str(parent) != str(test_parent):
                    # print('%s <- %s' % (parent_token, token))
                    # print('%s <- %s' % (parent, child))
                    print('predicted: %s; should be: %s'
                          % (transformation_to_string(transf), transformation_to_string(self.transfs[0])))
                    self.wrong += 1
                    transf = self.transfs[0]
                self.transfs = self.transfs[1:]
                self.test_true_values[transf] += 1
            else:
                apply_transformation(parent, root, elem_id, position, transf)

        return elem_id, transf

    def process_sentence(self, sentence):
        self.tree.root_id = self.process_token(sentence.root())[0]
        return ParserOutput(sentence, self.tree)

    def test(self, sentence, transfs):
        self.transfs = transfs
        self.process_token(sentence.root(), testing=True)


def transform(sentence):
    hgforest = Hypergen()
    return hgforest.process_sentence(sentence)


def test(infile, model_type='rf', model_file=None):
    parses = read_parses(infile, test_set=True)

    acc_total = 0
    acc_wrong = 0
    acc_predictions = Counter()
    acc_true_values = Counter()
    for parse in parses:
        # sentence_str = parse[0].strip()
        json_str = parse[1].strip()
        # outcome_str = parse[2].strip()
        sentence = Sentence(json_str=json_str)
        transfs = [int(token) for token in parse[3].split(',')]
        total = len(transfs)
        hgforest = Hypergen(model_type=model_type, model_file=model_file)
        hgforest.test(sentence, transfs)
        wrong = hgforest.wrong
        print('%s / %s' % (wrong, total))
        acc_total += total
        acc_wrong += wrong
        acc_predictions = sum((acc_predictions, Counter(hgforest.test_predictions)), Counter())
        acc_true_values = sum((acc_true_values, Counter(hgforest.test_true_values)), Counter())

    acc_predictions = dict(acc_predictions)
    acc_true_values = dict(acc_true_values)

    print('PREDICTIONS:')
    for transf in acc_predictions:
        print('%s: %s' % (transformation_to_string(transf), acc_predictions[transf]))
    print('TRUE_VALUES:')
    for transf in acc_true_values:
        print('%s: %s' % (transformation_to_string(transf), acc_true_values[transf]))

    error_rate = (float(acc_wrong) / float(acc_total)) * 100.
    print('error rate: %.3f%%' % error_rate)


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
        tr = transform(r[1])
        print(tr.tree.to_hyperedge_str(with_namespaces=False))
