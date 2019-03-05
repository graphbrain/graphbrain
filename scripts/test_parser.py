import argparse
from graphbrain import *
from graphbrain.parser.parser import Parser
from graphbrain.parser.vis import print_tree


if __name__ == '__main__':
    parser = argparse.ArgumentParser()

    parser.add_argument('file', type=str, help='dataset file')
    parser.add_argument('--lang', type=str, help='language', default='en')

    args = parser.parse_args()

    parser = Parser(lang=args.lang)

    total = 0
    wrong = 0

    sentence = None
    with open(args.file) as f:
        for line in f:
            if sentence:
                total += 1
                correct_edge = str2ent(line.strip())
                edge, sent = parser.parse(sentence)[0]
                if edge != correct_edge:
                    wrong += 1
                    print_tree(sent.root)
                    print('expected:')
                    print(edge2str(correct_edge))
                    print('result:')
                    print(edge2str(edge))
                sentence = None
            else:
                sentence = line

    print('%s wrong out of %s.' % (wrong, total))
