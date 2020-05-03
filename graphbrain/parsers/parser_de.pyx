import logging
from graphbrain import *
from .alpha_beta import AlphaBeta
from .nlp import token2str
from .text import UniqueAtom


class ParserDE(AlphaBeta):
    def __init__(self, lemmas=False, resolve_corefs=False):
        super().__init__('de_core_news_md', lemmas=lemmas,
                         resolve_corefs=resolve_corefs)
        self.lang = 'de'

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
                return 'Pd'
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
                return 'T'
            elif head_type == 'P':
                return 'M'
            else:
                return 'T'
        elif dep == 'nk':
            if token.head.dep_ == 'ag':
                return self._builder_type_and_subtype(token)
            elif token.head.dep_ == 'mo':
                return self._concept_type_and_subtype(token)
            else:
                return self._modifier_type_and_subtype(token)
        elif dep == 'ng':
            return self._modifier_type_and_subtype(token)
        else:
            logging.warning('Unknown dependency (token_type): token: {}'
                            .format(token2str(token)))
            return None

    def _concept_type_and_subtype(self, token):
        tag = token.tag_
        if tag[:3] == 'ADJ':
            return 'Ca'
        elif tag == 'NN':
            return 'Cc'
        elif tag in {'NE', 'NNE'}:
            return 'Cp'
        elif tag == 'CARD':
            return 'C#'
        # elif tag == 'DT':
        #     return 'Cd'
        # elif tag == 'WP':
        #     return 'Cw'
        elif tag == 'PPER':
            return 'Ci'
        else:
            return 'C'

    def _modifier_type_and_subtype(self, token):
        tag = token.tag_
        dep = token.dep_
        if dep == 'ng':
            return 'Mn'
        elif tag == 'ART':
            return 'Md'
        elif tag == 'PPOSAT':
            return 'Mp'
        elif tag == 'PIS':
            return 'M_'  # indefinite pronoun
        elif tag == 'CARD':
            return 'M#'
        # TODO
        elif tag == 'MD':
            return 'Mm'  # modal
        elif tag == 'TO':
            return 'Mi'  # infinitive
        elif tag == 'RBR':
            return 'M='  # comparative
        elif tag == 'RBS':
            return 'M^'  # superlative
        elif tag == 'RP' or token.dep_ == 'prt':
            return 'Ml'  # particle
        elif tag == 'EX':
            return 'Me'  # existential
        else:
            return 'M'

    def _builder_type_and_subtype(self, token):
        if token.head:
            if token.head.dep_ == 'ag':
                return 'Bp'

        tag = token.tag_
        # TODO: this is ugly, move this case elsewhere...
        if tag == 'KON':
            return 'J'
        # still english below...
        elif tag == 'IN':
            return 'Br'  # relational (proposition)
        elif tag == 'DT':
            return 'Bd'
        else:
            return 'B'

    def _predicate_post_type_and_subtype(self, edge, subparts, args_string):
        # detecte imperative
        if subparts[0] == 'Pd':
            if subparts[2][1] == 'i' and 's' not in args_string:
                return 'P!'
        # keep everything else the same
        return subparts[0]

    def _concept_role(self, concept):
        if concept.is_atom():
            token = self.atom2token[UniqueAtom(concept)]
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
            if ct == 'Br':
                connector = connector.replace_atom_part(
                    1, '{}.ma'.format(ct))
            elif ct == 'Bp':
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
