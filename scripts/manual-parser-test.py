import statistics
from termcolor import colored
from graphbrain import hedge
from graphbrain.cli import wrapper
from graphbrain.colored import indented, colored_edge
from graphbrain.parsers import create_parser


class ManualEvaluation(object):
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
        ignore = colored('i', 'blue')
        options_str = '{}/{}/{}/{}'.format(correct, minor, wrong, ignore)
        self.input_msg = 'correct, defect, wrong or ignore ({}) ? '.format(
            options_str)

    def input(self):
        answer = None
        while answer not in {'c', 'd', 'w', 'i'}:
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
            rds = float(defect.size())
            rds = rds / float(edge.size())
            self.rel_defect_sizes.append(rds)
        elif he == 'w':
            self.wrong += 1

    def __str__(self):
        n = self.correct + self.defect + self.wrong
        ratio = float(self.correct) / float(n) if n > 0 else 0.
        correct_str = '{} ({}/{})'.format(ratio, self.correct, n)
        ratio = float(self.defect) / float(n) if n > 0 else 0.
        defect_str = '{} ({}/{})'.format(ratio, self.defect, n)
        ratio = float(self.wrong) / float(n) if n > 0 else 0.
        wrong_str = '{} ({}/{})'.format(ratio, self.wrong, n)
        line1 = 'correct: {}; defect: {}; wrong: {}'.format(
            correct_str, defect_str, wrong_str)
        if len(self.defects) > 0:
            mds = statistics.mean([edge.size() for edge in self.defects])
            mdes = statistics.mean(
                [edge.size() for edge in self.defective_edges])
            mrds = statistics.mean(self.rel_defect_sizes)
        else:
            mds = 0.
            mdes = 0.
            mrds = 0.
        line2 = 'mds: {}; mdes: {}; mrds: {}'.format(
            mds, mdes, mrds)
        return '{}\n{}'.format(line1, line2)


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
                    edge.to_str()))
            elif edge.contains(subedge, deep=True):
                defect = subedge
            else:
                error_msg('{} is not a subedge of {}.'.format(
                    subedge.to_str(), edge.to_str()))
    return defect


def manual_test(args):
    parser = create_parser(name=args.lang)

    he = ManualEvaluation()

    with open(args.infile, 'r') as f:
        for line in f:
            sentence = line.strip()

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
            he.apply_evaluation(answer, edge, defect)

            row_str = '\t'.join(
                (sentence, edge.to_str(), answer, defect_str))
            with open(args.outfile, 'a') as of:
                of.write('{}\n'.format(row_str))

            print('GLOBAL:')
            print(colored(str(he), 'white'))


if __name__ == '__main__':
    wrapper(manual_test, text='manual test of parser')
