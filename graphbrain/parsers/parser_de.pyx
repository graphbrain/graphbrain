import logging
import spacy
from graphbrain import *
from .alpha_beta import AlphaBeta
from .nlp import token2str


class ParserDE(AlphaBeta):
    def __init__(self, lemmas=False):
        super().__init__(lemmas=lemmas)
        self.lang = 'de'
        self.nlp = spacy.load('de_core_news_md')

    # ===========================================
    # Implementation of language-specific methods
    # ===========================================

    def _arg_type(self, token):
        if token.dep_ == 'sb':
            return 's'
        elif token.dep_ == 'pd':
            return 'c'
        elif token.dep_ == 'mo':
            return 'x'
        else:
            return '?'

    def _token_type(self, token, head=False):
        if token.pos_ == 'PUNCT':
            return None

        dep = token.dep_

        if dep == 'punct':
            return None

        head_type = self._token_head_type(token)
        # if len(head_type) > 1:
        #     head_subtype = head_type[1]
        # else:
        #     head_subtype = ''
        if len(head_type) > 0:
            head_type = head_type[0]

        if self._is_noun(token):
            return self._concept_type_and_subtype(token)
        elif dep == 'ROOT':
            if self._is_verb(token):
                return 'p'
            else:
                return self._concept_type_and_subtype(token)
        elif dep in {'sb', 'pd', 'ag'}:
            return self._concept_type_and_subtype(token)
        elif dep in {'cj'}:
            return self._modifier_type_and_subtype(token)
        elif dep in {'mnr', 'cd'}:
            return self._builder_type_and_subtype(token)
        elif dep == 'mo':
            if token.tag_ == 'APPR':
                return 't'
            elif head_type == 'p':
                return 'a'
            else:
                return 'x'
        elif dep == 'nk':
            if token.head.dep_ == 'ag':
                return self._builder_type_and_subtype(token)
            elif token.head.dep_ == 'mo':
                return self._concept_type_and_subtype(token)
            else:
                return self._modifier_type_and_subtype(token)
        elif dep == 'ng':
            return 'an'
        else:
            logging.warning('Unknown dependency (token_type): token: {}'
                            .format(token2str(token)))
            return None

    def _concept_type_and_subtype(self, token):
        tag = token.tag_
        if tag[:3] == 'ADJ':
            return 'ca'
        elif tag == 'NN':
            return 'cc'
        elif tag in {'NE', 'NNE'}:
            return 'cp'
        elif tag == 'CARD':
            return 'c#'
        # elif tag == 'DT':
        #     return 'cd'
        # elif tag == 'WP':
        #     return 'cw'
        elif tag == 'PPER':
            return 'ci'
        else:
            return 'c'

    def _modifier_type_and_subtype(self, token):
        tag = token.tag_
        if tag == 'ART':
            return 'md'
        elif tag == 'PPOSAT':
            return 'mp'
        elif tag == 'PIS':
            return 'mi'
        elif tag == 'CARD':
            return 'm#'
        else:
            return 'm'

    def _builder_type_and_subtype(self, token):
        if token.head:
            if token.head.dep_ == 'ag':
                return 'bp'

        tag = token.tag_
        if tag == 'KON':
            return 'b+'
        # still english below...
        elif tag == 'IN':
            return 'br'  # relational (proposition)
        elif tag == 'DT':
            return 'bd'
        else:
            return 'b'

    # TODO
    def _auxiliary_type_and_subtype(self, token):
        if token.tag_ == 'MD':
            return 'am'  # modal
        elif token.tag_ == 'TO':
            return 'ai'  # infinitive
        elif token.tag_ == 'RBR':
            return 'ac'  # comparative
        elif token.tag_ == 'RBS':
            return 'as'  # superlative
        elif token.tag_ == 'RP' or token.dep_ == 'prt':
            return 'ap'  # particle
        elif token.tag_ == 'EX':
            return 'ae'  # existential
        return 'a'

    def _predicate_post_type_and_subtype(self, edge, subparts, args_string):
        # detecte imperative
        if subparts[0] == 'pd':
            if subparts[2][1] == 'i' and 's' not in args_string:
                return 'p!'
        # keep everything else the same
        return subparts[0]

    def _concept_role(self, concept):
        if concept.is_atom():
            token = self.atom2token[concept]
            if token.dep_ == 'compound':
                return 'a'
            else:
                return 'm'
        else:
            for edge in concept[1:]:
                if self._concept_role(edge) == 'm':
                    return 'm'
            return 'a'

    def _builder_arg_roles(self, edge):
        connector = edge[0]
        if connector.is_atom():
            ct = connector.type()
            if ct == 'br':
                connector = connector.replace_atom_part(
                    1, '{}.ma'.format(ct))
            elif ct == 'bp':
                connector = connector.replace_atom_part(
                    1, '{}.ma'.format(ct))
        return hedge((connector,) + edge[1:])

    def _is_noun(self, token):
        return token.tag_[0] == 'N'

    def _is_compound(self, token):
        return token.dep_ == 'compound'

    def _is_relative_concept(self, token):
        return token.dep_ == 'appos'

    def _is_verb(self, token):
        tag = token.tag_
        if len(tag) > 0:
            return token.tag_[0] == 'V'
        else:
            return False

    def _verb_features(self, token):
        verb_form = '-'
        tense = '-'
        aspect = '-'
        mood = '-'
        person = '-'
        number = '-'
        verb_type = '-'

        if token.tag_ == 'VAFIN':
            mood = 'i'  # indicative
            verb_form = 'f'  # finite
        elif token.tag_ == 'VAIMP':
            mood = '!'  # imperative
            verb_form = 'f'  # finite
        elif token.tag_ == 'VAINF':
            verb_form = 'i'  # infinitive
        elif token.tag_ == 'VAPP':
            aspect = 'f'  # perfect
            verb_form = 'f'  # finite
        elif token.tag_ == 'VMFIN':
            mood = 'i'  # indicative
            verb_form = 'f'  # finite
            verb_type = 'm'  # modal
        elif token.tag_ == 'VMINF':
            verb_form = 'f'  # finite
            verb_type = 'm'  # modal
        elif token.tag_ == 'VMPP':
            aspect = 'f'  # perfect
            verb_form = 'p'  # participle
            verb_type = 'm'  # modal
        elif token.tag_ == 'VVFIN':
            mood = 'i'  # indicative
            verb_form = 'f'  # finite
        elif token.tag_ == 'VVIMP':
            mood = '!'  # imperative
            verb_form = 'f'  # finite
        elif token.tag_ == 'VVINF':
            verb_form = 'i'  # infinitive
        elif token.tag_ == 'VVIZU':
            verb_form = 'i'  # infinitive
        elif token.tag_ == 'VVPP':
            aspect = 'f'  # perfect
            verb_form = 'p'  # participle

        features = (tense, verb_form, aspect, mood, person, number, verb_type)
        return ''.join(features)
