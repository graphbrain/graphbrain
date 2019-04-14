from graphbrain import *
from graphbrain.cli import wrapper
from graphbrain.meaning.parser import Parser
from graphbrain.meaning.nlpvis import print_tree


def test_parser(args):
    parser = Parser(lang=args.lang)

    total = 0
    wrong = 0

    sentence = None
    with open(args.infile) as f:
        for line in f:
            if sentence:
                total += 1
                correct_edge = str2ent(line.strip())
                parser_output = parser.parse(sentence)
                parsed_sentence = parser_output[0]
                edge = parsed_sentence['main_edge']
                sent = parsed_sentence['spacy_sentence']
                if edge != correct_edge:
                    wrong += 1
                    print_tree(sent.root)
                    print('expected:')
                    print(ent2str(correct_edge))
                    print('result:')
                    print(ent2str(edge))
                sentence = None
            else:
                sentence = line.strip()

    print('%s wrong out of %s.' % (wrong, total))


if __name__ == '__main__':
    wrapper(test_parser)
