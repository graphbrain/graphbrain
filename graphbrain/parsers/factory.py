from graphbrain import *
from graphbrain.parsers.nlp import print_tree
from graphbrain.parsers.parser_en import ParserEN


def create_parser(name, lemmas=False):
    if name == 'en':
        return ParserEN(lemmas=lemmas)
    else:
        raise RuntimeError('Unknown parser: {}'.format(name))


if __name__ == '__main__':
    sentence = """
    That seems both super dangerous, and super awesome.
    """

    parser = ParserEN()
    parser_output = parser.parse(sentence)
    parsed_sentence = parser_output[0]
    edge = parsed_sentence['main_edge']
    sent = parsed_sentence['spacy_sentence']
    print_tree(sent.root)
    print(ent2str(edge))
