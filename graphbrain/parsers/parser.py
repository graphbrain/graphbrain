import re

import graphbrain.constants as const

from graphbrain import hedge
from graphbrain.hyperedge import UniqueAtom


def _edge2text(edge, parse):
    atoms = [UniqueAtom(atom) for atom in edge.all_atoms()]
    tokens = [parse['atom2token'][atom] for atom in atoms
              if atom in parse['atom2token']]
    if len(tokens) == 0:
        return ''
    tokens = sorted(tokens, key=lambda x: x.i)
    prev_txt = tokens[0].text
    txt_parts = [prev_txt]
    sentence = str(parse['spacy_sentence'])
    for token in tokens[1:]: 
        txt = token.text
        res = re.search(r'{}(.*?){}'.format(re.escape(prev_txt),
                                            re.escape(txt)),
                        sentence)
        if res:
            sep = res.group(1)
        else:
            sep = ' '
        if any(letter.isalnum() for letter in sep):
            sep = ' '
        txt_parts.append(sep)
        txt_parts.append(token.text)
        prev_txt = txt
    return ''.join(txt_parts)


def _set_edges_text(edge, hg, parse):
    text = _edge2text(edge, parse)
    hg.set_attribute(edge, 'text', text)
    if edge.not_atom:
        for subedge in edge:
            _set_edges_text(subedge, hg, parse)


class Parser(object):
    """Defines the common interface for parser objects.
    Parsers transofrm natural text into graphbrain hyperedges.
    """

    def __init__(self, lemmas=True, corefs=True, debug=False):
        self.lemmas = lemmas
        self.corefs = corefs
        self.debug = debug

        # to be created by derived classes
        self.lang = None

    def debug_msg(self, msg):
        if self.debug:
            print(msg)

    def parse(self, text):
        """Transforms the given text into hyperedges + aditional information.
        Returns a dictionary with two fields:

        -> parses: a sequence of dictionaries, with one dictionary for each
        sentence found in the text.

        -> inferred_edges: a sequence of edges inferred during by parsing
        process (e.g. genders, 'X is Y' relationships)

        Each sentence parse dictionary contains at least the following fields:

        -> main_edge: the hyperedge corresponding to the sentence.

        -> extra_edges: aditional edges, e.g. connecting atoms that appear
        in the main_edge to their lemmas.

        -> text: the string of natural language text corresponding to the
        main_edge, i.e.: the sentence itself.

        -> edges_text: a dictionary of all edges and subedges to their
        corresponding text.

        -> corefs: resolve coreferences.
        """
        parse_results = self._parse(text)
        if self.corefs:
            self._resolve_corefs(parse_results)
        else:
            for parse in parse_results['parses']:
                parse['resolved_corefs'] = parse['main_edge']
        return parse_results

    def parse_and_add(self, text, hg, sequence=None, set_text=True):
        parse_results = self.parse(text)
        for parse in parse_results['parses']:
            main_edge = parse['resolved_corefs']
            if self.corefs:
                unresolved_edge = parse['main_edge']
            else:
                unresolved_edge = None
            # add main edge
            if main_edge:
                if sequence:
                    hg.add_to_sequence(sequence, main_edge)
                else:
                    hg.add(main_edge)
                # attach text to edge and subedges
                _set_edges_text(main_edge, hg, parse)
                if self.corefs:
                    if unresolved_edge != main_edge:
                         _set_edges_text(main_edge, hg, parse)
                    coref_res_edge = hedge(
                        (const.coref_res_pred, unresolved_edge, main_edge))
                    hg.add(coref_res_edge)
                # add extra edges
                for edge in parse['extra_edges']:
                    hg.add(edge)
        for edge in parse_results['inferred_edges']:
            hg.add(edge, count=True)
        return parse_results

    def sentences(self, text):
        raise NotImplementedError()

    def atom_gender(self, atom):
        raise NotImplementedError()

    def atom_number(self, atom):
        raise NotImplementedError()

    def atom_person(self, atom):
        raise NotImplementedError()

    def atom_animacy(self, atom):
        raise NotImplementedError()

    def _post_process(self, edge):
        raise NotImplementedError()

    def _parse_token(self, token):
        raise NotImplementedError()

    def _before_parse_sentence(self):
        raise NotImplementedError()

    def _parse_sentence(self, sent):
        raise NotImplementedError()

    def _parse(self, text):
        raise NotImplementedError()

    def _resolve_corefs(self, parse_results):
        # do nothing if not implemented in derived classes
        for parse in parse_results['parses']:
            parse['resolved_corefs'] = parse['main_edge']
