from collections import defaultdict
import json
from termcolor import colored
from graphbrain import hedge
from graphbrain.cli import wrapper
from graphbrain.colored import indented, colored_edge
from graphbrain.parsers import create_parser


class HumanEvaluation(object):
    def __init__(self):
        self.correct = 0
        self.defect = 0
        self.wrong = 0
        self.edges = []
        self.defects = []
        self.defective_edges = []
        self.rel_defect_sizes = []

        correct = colored('c', 'green')
        minor = colored('d', 'yellow')
        wrong = colored('w', 'red')
        options_str = '{}/{}/{}'.format(correct, minor, wrong)
        self.input_msg = 'correct, defect, or wrong ({}) ? '.format(
            options_str)

    def input(self):
        answer = None
        while answer not in {'c', 'd', 'w'}:
            answer = input(self.input_msg)
        return answer

    def apply_evaluation(self, he, edge, defect):
        self.edges.append(edge)
        if he == 'c':
            self.correct += 1
        elif he == 'd':
            self.defect += 1
            self.defects.append(defect)
            self.defective_edges.append(edge)
            rds = float(len(defect.all_atoms()))
            rds = rds / float(len(edge.all_atoms()))
            self.rel_defect_sizes.append(rds)
        elif he == 'w':
            self.wrong += 1

    def __str__(self):
        n = self.correct + self.minor + self.wrong
        ratio = float(self.correct) / float(n)
        correct_str = '{} ({}/{})'.format(ratio, self.correct, n)
        ratio = float(self.minor) / float(n)
        defect_str = '{} ({}/{})'.format(ratio, self.minor, n)
        ratio = float(self.wrong) / float(n)
        wrong_str = '{} ({}/{})'.format(ratio, self.wrong, n)
        return 'correct: {}; defect: {}; wrong: {}'.format(
            correct_str, defect_str, wrong_str)


def error_msg(msg):
    print('\n{} {}\n'.format(colored('Error: ', 'red'), msg))


def input_defect(sentence, edge):
    s = colored('s', 'magenta')
    h = colored('h', 'cyan')
    i = colored('i', 'yellow')
    options_str = '{}/{}/{}/subedge'.format(s, h, i)
    input_msg = 'wrong subedge ({}) ? '.format(options_str)

    defect = None
    while not defect:
        answer = input(input_msg)
        if answer == 's':
            print('\n{}\n'.format(sentence))
        elif answer == 'h':
            print('\n{}\n'.format(colored_edge(edge)))
        elif answer == 'i':
            print('\n{}\n'.format(indented(edge)))
        else:
            subedge = hedge(answer)
            if subedge is None:
                error_msg('{} did not parse correctly.'.format(
                    subedge.to_str()))
            elif edge.contains(subedge, deep=True):
                defect = subedge
            else:
                error_msg('{} is not a subedge of {}.'.format(
                    subedge.to_str(), edge.to_str()))
    return defect


def human_test(args):
    parser = create_parser(name=args.lang)

    he = HumanEvaluation()
    sources_he = defaultdict(HumanEvaluation)

    with open(args.infile, 'r') as f:
        for line in f:
            case = json.loads(line)
            if not case['ignore']:
                sentence = case['sentence']
                source = case['source'][:-1]

                parser_output = parser.parse(sentence)
                parsed_sentence = parser_output['parses'][0]
                edge = parsed_sentence['main_edge']

                print('\n{}\n{}\n'.format(sentence, indented(edge)))

                answer = he.input()
                if answer == 'd':
                    defect = input_defect(sentence, edge)
                    defect_str = defect.to_str()
                else:
                    defect = None
                    defect_str = ''
                he.apply_evaluation(answer, defect)
                sources_he[source].apply_evaluation(answer, edge, defect)

                row_str = '\t'.join(
                    (sentence, source, edge.to_str(), answer, defect_str))
                with open(args.outfile, 'a') as of:
                    of.write('{}\n'.format(row_str))

                print('GLOBAL:')
                print(colored(str(he), 'white'))
                for source in sources_he:
                    print('{}:'.format(source))
                    print(str(sources_he[source]))


if __name__ == '__main__':
    wrapper(human_test, text='human test of parser')
