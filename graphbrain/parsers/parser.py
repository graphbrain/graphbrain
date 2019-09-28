import sys
import logging
from collections import namedtuple


logging.basicConfig(stream=sys.stderr, level=logging.WARNING)


class Parser(object):
    """Defines the common interface for parser objects.
    Parser transofrm natural text into graphbrain hyperedges.
    """

    def __init__(self, lemmas=False):
        self.lemmas = lemmas
        self.atom2token = {}
        self.cur_text = None

        # to be created by derived classes
        self.lang = None
        self.nlp = None

        # named tuple used to pass parser state internally
        self._ParseState = namedtuple('_ParseState',
                                      ['extra_edges', 'tokens', 'child_tokens',
                                       'positions', 'children', 'entities'])

    def _post_process(edge):
        raise NotImplementedError()

    def _parse_token(token):
        raise NotImplementedError()

    def _parse_sentence(self, sent):
        self.atom2token = {}
        main_edge, extra_edges = self._parse_token(sent.root)

        main_edge, _ = self._post_process(main_edge)
        return {'main_edge': main_edge,
                'extra_edges': extra_edges,
                'text': str(sent),
                'spacy_sentence': sent}

    def parse(self, text):
        """Transforms the given text into hyperedges + aditional information.
        Returns a sequence of dictionaries, with one dictionary for each
        sentence found in the text.

        Each dictionary contains the following fields:

        -> main_edge: the hyperedge corresponding to the sentence.

        -> extra_edges: aditional edges, e.g. connecting atoms that appear
        in the main_edge to their lemmas.

        -> text: the string of natural language text corresponding to the
        main_edge, i.e.: the sentence itself.

        -> spacy_sentence: the spaCy structure representing the sentence
        enriched with NLP annotations.
        """
        self.cur_text = text
        doc = self.nlp(text.strip())
        return tuple(self._parse_sentence(sent) for sent in doc.sents)
