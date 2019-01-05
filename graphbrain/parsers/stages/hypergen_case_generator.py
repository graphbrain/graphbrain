import traceback
from termcolor import colored
from graphbrain.funs import *
from graphbrain.parsers.stages.hypergen import *


def test_transformation(parent, parent_token, child, position, transf):
    test_tree = Tree(parent)
    test_tree.import_element(child)
    test_parent = test_tree.root()
    test_root = test_tree.token2leaf(parent_token)
    apply_transformation(test_parent, test_root, child.id, position, transf)
    return test_tree.to_hyperedge_str(with_namespaces=False)


class CaseGenerator(object):
    def __init__(self, lang='en'):
        self.tree = None
        self.parser = Parser(lang=lang)
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

    def show_option(self, key, name, parent, parent_token, child, position, transf):
        res = test_transformation(parent, parent_token, child, position, transf)
        if res not in self.transformation_outcomes:
            self.transformation_outcomes.append(res)
            print(colored(key, 'cyan'), end='')
            print(') ', end='')
            print(colored(name, 'green'), end='')
            print(colored('   %s' % res, 'white', attrs=['bold']))

    def choose_transformation(self, parent, parent_token, child, position):
        print('target sentence:')
        print(self.sentence_str)
        print('parent <- child')
        print('%s <- %s' % (parent, child))

        self.transformation_outcomes = []

        self.show_option('i', 'IGNORE', parent, parent_token, child, position, IGNORE)
        self.show_option('a', 'APPLY NODE', parent, parent_token, child, position, APPLY_HYPEREDGE)
        self.show_option('n', 'NEST NODE', parent, parent_token, child, position, NEST_HYPEREDGE)
        self.show_option('p', 'APPEND', parent, parent_token, child, position, HEAD)
        self.show_option('ar', 'APPLY ROOT', parent, parent_token, child, position, APPLY_TOKEN)
        self.show_option('nr', 'NEST ROOT', parent, parent_token, child, position, NEST_TOKEN)

        print('\n0) RESTART    x) ABORT')

        choice = input('> ').lower()

        if choice == 'i':
            return IGNORE
        if choice == 'a':
            return APPLY_HYPEREDGE
        if choice == 'n':
            return NEST_HYPEREDGE
        if choice == 'ar':
            return APPLY_TOKEN
        if choice == 'nr':
            return NEST_TOKEN
        if choice == 'p':
            return HEAD
        if choice == '0':
            self.restart = True
            return IGNORE
        if choice == 'x':
            self.abort = True
            return IGNORE
        else:
            print('unknown choice: "%s". ignoring' % choice)
            return self.choose_transformation(parent, parent_token, child, position)

    def process_token(self, token, parent_token=None, parent_id=None, position=None):
        elem = self.tree.create_leaf(token)
        elem_id = elem.id

        # process children first
        for child_token in token.left_children:
            _, t = self.process_token(child_token, token, elem_id, Position.LEFT)
            if self.restart or self.abort:
                return -1, -1
        for child_token in token.right_children:
            self.process_token(child_token, token, elem_id, Position.RIGHT)
            if self.restart or self.abort:
                return -1, -1

        # choose and apply transformation
        transf = -1
        if parent_token:
            parent = self.tree.get(parent_id)
            root = self.tree.token2leaf(parent_token)
            child = self.tree.get(elem_id)
            if self.interactive:
                transf = self.choose_transformation(parent, parent_token, self.tree.get(elem_id), position)
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
            print(root)
            print(transformation_to_string(transf))
            apply_transformation(parent, root, elem_id, position, transf)
            print(self.tree.get(parent_id))
            print()

        return elem_id, transf

    def generate(self, sentence_str, json_str=None, outcome_str=None):
        self.tree = Tree()
        self.sentence_str = sentence_str
        if json_str:
            self.sentence = Sentence(json_str=json_str)
        else:
            self.sentence = self.parser.parse_text(sentence_str)[0][1]
        self.sentence.print_tree()
        if outcome_str:
            self.outcome_str = outcome_str
            self.outcome = str2edge(outcome_str)
        self.tree.root_id, _ = self.process_token(self.sentence.root())

    def validate(self):
        return self.tree.to_hyperedge_str(with_namespaces=False) == self.outcome_str

    def write_cases(self, outfile):
        for case in self.cases:
            values = [str(case[field]) for field in expanded_fields()]
            f = open(outfile, 'a')
            f.write('%s\n' % ','.join(values))
            f.close()


def generate_cases(infile, outfile, lang='en'):
    f = open(outfile, 'w')
    f.write('%s\n' % ','.join(expanded_fields()))
    f.close()

    parses = read_parses(infile, test_set=False)

    total = 0
    correct = 0
    cg = CaseGenerator(lang=lang)
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


def interactive_edge_builder(outfile, lang='en'):
    print('writing to file: %s' % outfile)
    cg = CaseGenerator(lang=lang)
    cg.interactive = True
    while True:
        sentence_str = input('sentence> ').strip()
        done = False
        while not done:
            try:
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
                        f = open(outfile, 'a', encoding='utf-8')
                        f.write('%s\n' % sentence_str)
                        f.write('%s\n' % cg.sentence.to_json())
                        f.write('%s\n' % outcome)
                        f.write('%s\n' % ','.join([str(transf) for transf in cg.transfs]))
                        f.close()
            except UnicodeEncodeError as e:
                print(e)
                traceback.print_exc()
                done = True
