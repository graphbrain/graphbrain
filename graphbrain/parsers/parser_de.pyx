import logging
import spacy
from graphbrain import *
from .alpha_beta import AlphaBeta
from .nlp import token2str


deps_arg_roles = {
    'sb': 's',      # subject
    'pd': 'c',      # subject complement
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
def is_verb(token):
    tag = token.tag_
    if len(tag) > 0:
        return token.tag_[0] == 'V'
    else:
        return False


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


def arg_type(token):
    return deps_arg_roles.get(token.dep_, '?')


# done
def _verb_features(token):
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


class ParserDE(AlphaBeta):
    def __init__(self, lemmas=False):
        super().__init__(lemmas=lemmas)
        self.lang = 'de'
        self.nlp = spacy.load('de_core_news_md')

    # ===========================================
    # Implementation of language-specific methods
    # ===========================================

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

    def _build_atom(self, token, ent_type, ps):
        text = token.text.lower()
        et = ent_type

        if ent_type[0] == 'p' and ent_type != 'pm':
            atom = self._build_atom_predicate(token, ent_type, ps)
        elif ent_type[0] == 'x':
            atom = self._build_atom_subpredicate(token, ent_type)
        elif ent_type[0] == 'a':
            atom = self._build_atom_auxiliary(token, ent_type)
        else:
            atom = build_atom(text, et, self.lang)

        self.atom2token[atom] = token
        return atom

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
            if is_verb(token):
                return 'p'
            else:
                return concept_type_and_subtype(token)
        elif dep in {'sb', 'pd', 'ag'}:
            return concept_type_and_subtype(token)
        elif dep in {'cj'}:
            return modifier_type_and_subtype(token)
        elif dep in {'mo'}:
            return 'x'
        elif dep == 'nk':
            if token.head.dep_ == 'ag':
                return builder_type_and_subtype(token)
            else:
                return modifier_type_and_subtype(token)
        elif dep == 'cd':
            return builder_type_and_subtype(token)
        else:
            logging.warning('Unknown dependency (token_type): token: {}'
                            .format(token2str(token)))
            return None

    def _is_relative_concept(self, token):
        return token.dep_ == 'appos'

    def _is_compound(self, token):
        return token.dep_ == 'compound'

    # ===============
    # Private methods
    # ===============

    # move this to parent class
    def _token_head_type(self, token):
        head = token.head
        if head and head != token:
            return self._token_type(head)
        else:
            return ''

    def _build_atom_predicate(self, token, ent_type, ps):
        text = token.text.lower()
        et = ent_type

        # create verb features string
        verb_features = _verb_features(token)

        # create arguments string
        args = [arg_type(ps.tokens[entity]) for entity in ps.entities]
        args_string = ''.join([arg for arg in args if arg != '?'])

        # assign predicate subtype
        # (declarative, imperative, interrogative, ...)
        if len(ps.child_tokens) > 0:
            last_token = ps.child_tokens[-1][0]
        else:
            last_token = None
        if len(ent_type) == 1:
            # interrogative cases
            if (last_token and
                    last_token.tag_ == '.' and
                    last_token.dep_ == 'punct' and
                    last_token.lemma_.strip() == '?'):
                ent_type = 'p?'
            # imperative cases
            elif (is_infinitive(token) and 's' not in args_string and
                    'TO' not in [child[0].tag_
                                 for child in ps.child_tokens]):
                ent_type = 'p!'
            # declarative (by default)
            else:
                ent_type = 'pd'

        et = '{}.{}.{}'.format(ent_type, args_string, verb_features)

        return build_atom(text, et, self.lang)

    def _build_atom_subpredicate(self, token, ent_type):
        text = token.text.lower()
        et = ent_type

        if is_verb(token):
            # create verb features string
            verb_features = _verb_features(token)
            et = 'xv.{}'.format(verb_features)

        return build_atom(text, et, self.lang)

    def _build_atom_auxiliary(self, token, ent_type):
        text = token.text.lower()
        et = ent_type

        if is_verb(token):
            # create verb features string
            verb_features = _verb_features(token)
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
