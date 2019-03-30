from graphbrain import *
from graphbrain.meaning.parser import Parser
from graphbrain.meaning.nlpvis import print_tree


if __name__ == '__main__':
    text = """
    More news about the situation are expected today, after the press
    conference.
    Satellites from NASA and other agencies have been tracking sea ice changes
    since 1979.
    """

    parser = Parser(lang='en', pos=True, lemmas=True)
    parses = parser.parse(text)

    for parse in parses:
        print_tree(parse['spacy_sentence'].root)
        print(ent2str(parse['main_edge']))
        print('>> Extra edges:')
        for edge in parse['extra_edges']:
            print(ent2str(edge))
