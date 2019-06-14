from collections import namedtuple


class Parser(object):
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

    def post_process(edge):
        raise NotImplementedError()

    def parse_token(token):
        raise NotImplementedError()

    def parse_sentence(self, sent):
        self.atom2token = {}
        main_edge, extra_edges = self.parse_token(sent.root)
        main_edge, _ = self.post_process(main_edge)
        return {'main_edge': main_edge,
                'extra_edges': extra_edges,
                'text': str(sent),
                'spacy_sentence': sent}

    def parse(self, text):
        self.cur_text = text
        doc = self.nlp(text.strip())
        return tuple(self.parse_sentence(sent) for sent in doc.sents)
