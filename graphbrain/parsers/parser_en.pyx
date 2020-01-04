import logging
import spacy
from graphbrain import *
from .alpha_beta import AlphaBeta
from .nlp import token2str


deps_arg_roles = {
    'nsubj': 's',      # subject
    'nsubjpass': 'p',  # passive subject
    'agent': 'a',      # agent
    'acomp': 'c',      # subject complement
    'attr': 'c',       # subject complement
    'dobj': 'o',       # direct object
    'prt': 'o',        # direct object
    'dative': 'i',     # indirect object
    'advcl': 'x',      # specifier
    'prep': 'x',       # specifier
    'npadvmod': 'x',   # specifier
    'parataxis': 't',  # parataxis
    'intj': 'j',       # interjection
    'xcomp': 'r',      # clausal complement
    'ccomp': 'r'       # clausal complement
}


def is_noun(token):
    return token.tag_[:2] == 'NN'


def is_verb(token):
    tag = token.tag_
    if len(tag) > 0:
        return token.tag_[0] == 'V'
    else:
        return False


def is_infinitive(token):
    return token.tag_ == 'VB'


def concept_type_and_subtype(token):
    tag = token.tag_
    dep = token.dep_
    if dep == 'nmod':
        return 'cm'
    if tag[:2] == 'JJ':
        return 'ca'
    elif tag[:2] == 'NN':
        subtype = 'p' if 'P' in tag else 'c'
        sing_plur = 'p' if tag[-1] == 'S' else 's'
        return 'c{}.{}'.format(subtype, sing_plur)
    elif tag == 'CD':
        return 'c#'
    elif tag == 'DT':
        return 'cd'
    elif tag == 'WP':
        return 'cw'
    elif tag == 'PRP':
        return 'ci'
    else:
        return 'c'


def modifier_type_and_subtype(token):
    tag = token.tag_
    if tag == 'JJ':
        return 'ma'
    elif tag == 'JJR':
        return 'mc'
    elif tag == 'JJS':
        return 'ms'
    elif tag == 'DT':
        return 'md'
    # elif tag == 'PDT':
    #     return 'mp'
    elif tag == 'WDT':
        return 'mw'
    elif tag == 'CD':
        return 'm#'
    else:
        return 'm'


def builder_type_and_subtype(token):
    tag = token.tag_
    if tag == 'IN':
        return 'br'  # relational (proposition)
    elif tag == 'CC':
        return 'b+'
    elif tag == 'DT':
        return 'bd'
    else:
        return 'b'


def arg_type(token):
    return deps_arg_roles.get(token.dep_, '?')


def _verb_features(token):
    tense = '-'
    verb_form = '-'
    aspect = '-'
    mood = '-'
    person = '-'
    number = '-'
    verb_type = '-'

    if token.tag_ == 'VB':
        verb_form = 'i'  # infinitive
    elif token.tag_ == 'VBD':
        verb_form = 'f'  # finite
        tense = '<'  # past
    elif token.tag_ == 'VBG':
        verb_form = 'p'  # participle
        tense = '|'  # present
        aspect = 'g'  # progressive
    elif token.tag_ == 'VBN':
        verb_form = 'p'  # participle
        tense = '<'  # past
        aspect = 'f'  # perfect
    elif token.tag_ == 'VBP':
        verb_form = 'f'  # finite
        tense = '|'  # present
    elif token.tag_ == 'VBZ':
        verb_form = 'f'  # finite
        tense = '|'  # present
        number = 's'  # singular
        person = '3'  # third person

    features = (tense, verb_form, aspect, mood, person, number, verb_type)
    return ''.join(features)


class ParserEN(AlphaBeta):
    def __init__(self, lemmas=False):
        super().__init__(lemmas=lemmas)
        self.lang = 'en'
        self.nlp = spacy.load('en_core_web_lg')

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
                    1, '{}.am'.format(ct))
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

        if dep in {'', 'subtok'}:
            return None

        head_type = self._token_head_type(token)
        if len(head_type) > 1:
            head_subtype = head_type[1]
        else:
            head_subtype = ''
        if len(head_type) > 0:
            head_type = head_type[0]

        if dep == 'ROOT':
            if is_verb(token):
                return 'p'
            else:
                return concept_type_and_subtype(token)
        elif dep in {'appos', 'attr', 'compound', 'dative', 'dep', 'dobj',
                     'nsubj', 'nsubjpass', 'oprd', 'pobj', 'meta'}:
            return concept_type_and_subtype(token)
        elif dep in {'advcl', 'csubj', 'csubjpass', 'parataxis'}:
            return 'p'
        elif dep in {'relcl', 'ccomp'}:
            if is_verb(token):
                return 'pr'
            else:
                return concept_type_and_subtype(token)
        elif dep in {'acl', 'pcomp', 'xcomp'}:
            if token.tag_ == 'IN':
                return 'a'
            else:
                return 'pc'
        elif dep in {'amod', 'nummod', 'preconj', 'predet'}:
            return modifier_type_and_subtype(token)
        elif dep == 'det':
            if token.head.dep_ == 'npadvmod':
                return builder_type_and_subtype(token)
            else:
                return modifier_type_and_subtype(token)
        elif dep in {'aux', 'auxpass', 'expl', 'prt', 'quantmod'}:
            if head_type == 'c':
                return 'm'
            if token.n_lefts + token.n_rights == 0:
                return 'a'
            else:
                return 'x'
        elif dep in {'nmod', 'npadvmod'}:
            if is_noun(token):
                return concept_type_and_subtype(token)
            else:
                return modifier_type_and_subtype(token)
        elif dep == 'cc':
            if head_type == 'p':
                return 'pm'
            else:
                return builder_type_and_subtype(token)
        elif dep == 'case':
            if token.head.dep_ == 'poss':
                return 'bp'
            else:
                return builder_type_and_subtype(token)
        elif dep == 'neg':
            return 'an'
        elif dep == 'agent':
            return 'x'
        elif dep in {'intj', 'punct'}:
            return ''
        elif dep == 'advmod':
            if token.head.dep_ == 'advcl':
                return 't'
            elif head_type == 'p':
                return 'a'
            elif head_type in {'m', 'x', 't', 'b'}:
                return 'w'
            else:
                return modifier_type_and_subtype(token)
        elif dep == 'poss':
            if is_noun(token):
                return concept_type_and_subtype(token)
            else:
                return 'mp'
        elif dep == 'prep':
            if head_type == 'p':
                return 't'
            else:
                return builder_type_and_subtype(token)
        elif dep == 'conj':
            if head_type == 'p' and is_verb(token):
                return 'p'
            else:
                return concept_type_and_subtype(token)
        elif dep == 'mark':
            if head_type == 'p' and head_subtype != 'c':
                return 'x'
            else:
                return builder_type_and_subtype(token)
        elif dep == 'acomp':
            if is_verb(token):
                return 'x'
            else:
                return concept_type_and_subtype(token)
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
