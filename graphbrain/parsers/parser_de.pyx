import logging
import spacy
from graphbrain import *
from .alpha_beta import AlphaBeta
from .nlp import token2str


deps_arg_roles = {
    'sb': 's',      # subject
    'pd': 'c',      # subject complement
    'mo': 'x',      # specifier
    # 'nsubjpass': 'p',  # passive subject
    # 'agent': 'a',      # agent
    # 'acomp': 'c',      # subject complement
    # 'attr': 'c',       # subject complement
    # 'dobj': 'o',       # direct object
    # 'prt': 'o',        # direct object
    # 'dative': 'i',     # indirect object
    # 'advcl': 'x',      # specifier
    # 'prep': 'x',       # specifier
    # 'npadvmod': 'x',   # specifier
    # 'parataxis': 't',  # parataxis
    # 'intj': 'j',       # interjection
    # 'xcomp': 'r',      # clausal complement
    # 'ccomp': 'r'       # clausal complement
}


##
def is_noun(token):
    return token.tag_[0] == 'N'


##
def is_infinitive(token):
    return token.tag_ in {'VVINF', 'VVIZU'}


##
def concept_type_and_subtype(token):
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


def modifier_type_and_subtype(token):
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


def builder_type_and_subtype(token):
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


class ParserDE(AlphaBeta):
    def __init__(self, lemmas=False):
        super().__init__(lemmas=lemmas)
        self.lang = 'de'
        self.nlp = spacy.load('de_core_news_md')

    # ===========================================
    # Implementation of language-specific methods
    # ===========================================

    def _arg_type(self, token):
        return deps_arg_roles.get(token.dep_, '?')

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

    def _token_type(self, token, head=False):
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

        if is_noun(token):
            return concept_type_and_subtype(token)
        elif dep == 'ROOT':
            if self._is_verb(token):
                return 'p'
            else:
                return concept_type_and_subtype(token)
        elif dep in {'sb', 'pd', 'ag'}:
            return concept_type_and_subtype(token)
        elif dep in {'cj'}:
            return modifier_type_and_subtype(token)
        elif dep in {'mnr', 'cd'}:
            return builder_type_and_subtype(token)
        elif dep == 'mo':
            if token.tag_ == 'APPR':
                return 't'
            elif head_type == 'p':
                return 'a'
            else:
                return 'x'
        elif dep == 'nk':
            if token.head.dep_ == 'ag':
                return builder_type_and_subtype(token)
            elif token.head.dep_ == 'mo':
                return concept_type_and_subtype(token)
            else:
                return modifier_type_and_subtype(token)
        elif dep == 'ng':
            return 'an'
        else:
            logging.warning('Unknown dependency (token_type): token: {}'
                            .format(token2str(token)))
            return None

    def _is_relative_concept(self, token):
        return token.dep_ == 'appos'

    def _is_compound(self, token):
        return token.dep_ == 'compound'

    def _build_atom_auxiliary(self, token, ent_type):
        text = token.text.lower()
        et = ent_type

        if self._is_verb(token):
            # create verb features string
            verb_features = self._verb_features(token)
            et = 'av.{}'.format(verb_features)  # verbal
        elif token.tag_ == 'MD':
            et = 'am'  # modal
        elif token.tag_ == 'TO':
            et = 'ai'  # infinitive
        elif token.tag_ == 'RBR':
            et = 'ac'  # comparative
        elif token.tag_ == 'RBS':
            et = 'as'  # superlative
        elif token.tag_ == 'RP' or token.dep_ == 'prt':
            et = 'ap'  # particle
        elif token.tag_ == 'EX':
            et = 'ae'  # existential

        return build_atom(text, et, self.lang)

    def _predicate_type(self, edge, subparts, args_string):
        # detecte imperative
        if subparts[0] == 'pd':
            if subparts[2][1] == 'i' and 's' not in args_string:
                return 'p!'
        # keep everything else the same
        return subparts[0]

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
