from collections import defaultdict
import json
from termcolor import colored
from graphbrain import hedge, build_atom
from graphbrain.cli import wrapper
from graphbrain.parsers import create_parser


def simple_edge(edge):
    if edge.is_atom():
        roles = [edge.type()[0]]
        root = edge.root()
        argroles = edge.argroles()
        if argroles:
            roles.append(argroles)
        return build_atom(root, '.'.join(roles))
    else:
        return hedge([simple_edge(subedge) for subedge in edge])


class HumanEvaluation(object):
    def __init__(self):
        self.correct = 0
        self.minor = 0
        self.wrong = 0

        correct = colored('c', 'green')
        minor = colored('m', 'yellow')
        wrong = colored('w', 'red')
        options_str = '{}/{}/{}'.format(correct, minor, wrong)
        self.input_msg = 'correct, minor defect, or wrong ({}) ? '.format(
            options_str)

    def input(self):
        answer = None
        while answer not in {'c', 'm', 'w'}:
            answer = input(self.input_msg)
        return answer

    def apply_evaluation(self, he):
        if he == 'c':
            self.correct += 1
        elif he == 'm':
            self.minor += 1
        elif he == 'w':
            self.wrong += 1

    def __str__(self):
        n = self.correct + self.minor + self.wrong
        ratio = float(self.correct) / float(n)
        correct_str = '{} ({}/{})'.format(ratio, self.correct, n)
        ratio = float(self.minor) / float(n)
        minor_str = '{} ({}/{})'.format(ratio, self.minor, n)
        ratio = float(self.wrong) / float(n)
        wrong_str = '{} ({}/{})'.format(ratio, self.wrong, n)
        return 'correct: {}; minor: {}; wrong: {}'.format(
            correct_str, minor_str, wrong_str)


def test_beta(args):
    parser = create_parser(name=args.lang)

    he = HumanEvaluation()
    sources_he = defaultdict(HumanEvaluation)

    with open(args.infile) as f:
        for line in f:
            case = json.loads(line)
            if not case['ignore']:
                sentence = case['sentence']
                tedge = simple_edge(hedge(case['hyperedge']))
                source = case['source']
                correct = case['correct']

                parser_output = parser.parse(sentence)
                parsed_sentence = parser_output['parses'][0]
                pedge = simple_edge(parsed_sentence['main_edge'])

                print('\n\nsentence:')
                print(sentence)
                print('\ntrue edge:')
                print(tedge)
                print('\nresult:')
                print(pedge)
                print('\nsource: {}; correct? {}'.format(source, correct))

                answer = he.input()
                he.apply_evaluation(answer)
                sources_he[source].apply_evaluation(answer)

                print('GLOBAL:')
                print(str(he))
                for source in sources_he:
                    print('{}:'.format(source))
                    print(str(sources_he[source]))


if __name__ == '__main__':
    wrapper(test_beta, text='human test of parser')
