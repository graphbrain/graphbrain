import sys
import logging


logging.basicConfig(stream=sys.stderr, level=logging.WARNING)


class Parser(object):
    """Defines the common interface for parser objects.
    Parsers transofrm natural text into graphbrain hyperedges.
    """

    def __init__(self, lemmas=False):
        self.lemmas = lemmas

        # to be created by derived classes
        self.lang = None

    def _post_process(self, edge):
        raise NotImplementedError()

    def _parse_token(self, token):
        raise NotImplementedError()

    def _before_parse_sentence(self):
        raise NotImplementedError()

    def _parse_sentence(self, sent):
        raise NotImplementedError()

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
        raise NotImplementedError()
