import logging
import spacy
from graphbrain import *
from .alpha_beta import AlphaBeta
from .nlp import token2str


class ParserEN(AlphaBeta):
    def __init__(self, lemmas=False):
        super().__init__(lemmas=lemmas)
        self.lang = 'en'
        self.nlp = spacy.load('en_core_web_lg')

    # ===========================================
    # Implementation of language-specific methods
    # ===========================================

    def _arg_type(self, token):
        # subject
        if token.dep_ == 'nsubj':
            return 's'
        # passive subject
        elif token.dep_ == 'nsubjpass':
            return 'p'
        # agent
        elif token.dep_ == 'agent':
            return 'a'
        # subject complement
        elif token.dep_ in {'acomp', 'attr'}:
            return 'c'
        # direct object
        elif token.dep_ in {'dobj', 'prt'}:
            return 'o'
        # indirect object
        elif token.dep_ == 'dative':
            return 'i'
        # specifier
        elif token.dep_ in {'advcl', 'prep', 'npadvmod'}:
            return 'x'
        # parataxis
        elif token.dep_ == 'parataxis':
            return 't'
        # interjection
        elif token.dep_ == 'intj':
            return 'j'
        # clausal complement
        elif token.dep_ in {'xcomp', 'ccomp'}:
            return 'r'
        else:
            return '?'

    def _token_type(self, token, head=False):
        if token.pos_ == 'PUNCT':
            return None

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
            if self._is_verb(token):
                return 'p'
            else:
                return self._concept_type_and_subtype(token)
        elif dep in {'appos', 'attr', 'compound', 'dative', 'dep', 'dobj',
                     'nsubj', 'nsubjpass', 'oprd', 'pobj', 'meta'}:
            return self._concept_type_and_subtype(token)
        elif dep in {'advcl', 'csubj', 'csubjpass', 'parataxis'}:
            return 'p'
        elif dep in {'relcl', 'ccomp'}:
            if self._is_verb(token):
                return 'pr'
            else:
                return self._concept_type_and_subtype(token)
        elif dep in {'acl', 'pcomp', 'xcomp'}:
            if token.tag_ == 'IN':
                return 'a'
            else:
                return 'pc'
        elif dep in {'amod', 'nummod', 'preconj', 'predet'}:
            return self._modifier_type_and_subtype(token)
        elif dep == 'det':
            if token.head.dep_ == 'npadvmod':
                return self._builder_type_and_subtype(token)
            else:
                return self._modifier_type_and_subtype(token)
        elif dep in {'aux', 'auxpass', 'expl', 'prt', 'quantmod'}:
            if head_type == 'c':
                return 'm'
            if token.n_lefts + token.n_rights == 0:
                return 'a'
            else:
                return 'x'
        elif dep in {'nmod', 'npadvmod'}:
            if self._is_noun(token):
                return self._concept_type_and_subtype(token)
            else:
                return self._modifier_type_and_subtype(token)
        elif dep == 'cc':
            if head_type == 'p':
                return 'pm'
            else:
                return self._builder_type_and_subtype(token)
        elif dep == 'case':
            if token.head.dep_ == 'poss':
                return 'bp'
            else:
                return self._builder_type_and_subtype(token)
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
                return self._modifier_type_and_subtype(token)
        elif dep == 'poss':
            if self._is_noun(token):
                return self._concept_type_and_subtype(token)
            else:
                return 'mp'
        elif dep == 'prep':
            if head_type == 'p':
                return 't'
            else:
                return self._builder_type_and_subtype(token)
        elif dep == 'conj':
            if head_type == 'p' and self._is_verb(token):
                return 'p'
            else:
                return self._concept_type_and_subtype(token)
        elif dep == 'mark':
            if head_type == 'p' and head_subtype != 'c':
                return 'x'
            else:
                return self._builder_type_and_subtype(token)
        elif dep == 'acomp':
            if self._is_verb(token):
                return 'x'
            else:
                return self._concept_type_and_subtype(token)
        else:
            logging.warning('Unknown dependency (token_type): token: {}'
                            .format(token2str(token)))
            return None

    def _concept_type_and_subtype(self, token):
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

    def _modifier_type_and_subtype(self, token):
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

    def _builder_type_and_subtype(self, token):
        tag = token.tag_
        if tag == 'IN':
            return 'br'  # relational (proposition)
        elif tag == 'CC':
            return 'b+'
        elif tag == 'DT':
            return 'bd'
        else:
            return 'b'

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
                if hedge('to/ai/en') not in edge[0].atoms():
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
                    1, '{}.am'.format(ct))
        return hedge((connector,) + edge[1:])

    def _is_noun(self, token):
        return token.tag_[:2] == 'NN'

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
