from graphbrain import *
from graphbrain.parsers import *


if __name__ == '__main__':
    text = """
    Satellites from NASA and other agencies have been tracking sea ice changes
    since 1979.
    """

    parser = create_parser('en', lemmas=True)
    parses = parser.parse(text)

    for parse in parses:
        print_tree(parse['spacy_sentence'].root)
        print(parse['main_edge'])
        print('>> Extra edges:')
        for edge in parse['extra_edges']:
            print(edge)
