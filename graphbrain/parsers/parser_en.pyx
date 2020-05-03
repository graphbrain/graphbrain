import logging
from graphbrain import *
from .alpha_beta import AlphaBeta
from .nlp import token2str
from .text import UniqueAtom


_female = {"she/Ci/en", "her/Ci/en", "herself/Ci/en", "hers/Ci/en",
           "her/Mp/en"}
_male = {"he/Ci/en", "him/Ci/en", "himself/Ci/en", "his/Mp/en"}
_neutral = {"it/Ci/en", "itself/Ci/en", "its/Mp/en"}

_singular = {"it/Ci/en", "i/Ci/en", "she/Ci/en", "he/Ci/en", "her/Ci/en",
             "herself/Ci/en", "me/Ci/en", "him/Ci/en", "itself/Ci/en",
             "yourself/Ci/en", "myself/Ci/en", "himself/Ci/en", "one/Ci/en",
             "hers/Ci/en", "mine/Ci/en", "somebody/Ci/en", "oneself/Ci/en",
             "yours/Ci/en", "her/Mp/en", "his/Mp/en", "my/Mp/en", "its/Mp/en"}
_plural = {"they/Ci/en", "them/Ci/en", "we/Ci/en", "us/Ci/en", "'em/Ci/en",
           "themselves/Ci/en", "theirs/Ci/en", "ourselves/Ci/en",
           "their/Mp/en", "our/Mp/en"}

_animate = {"i/Ci/en", "she/Ci/en", "you/Ci/en", "he/Ci/en", "her/Ci/en",
            "herself/Ci/en", "me/Ci/en", "him/Ci/en", "we/Ci/en", "us/Ci/en",
            "yourself/Ci/en", "myself/Ci/en", "himself/Ci/en", "one/Ci/en",
            "themselves/Ci/en", "hers/Ci/en", "mine/Ci/en", "somebody/Ci/en",
            "oneself/Ci/en", "yours/Ci/en", "ourselves/Ci/en", "her/Mp/en",
            "his/Mp/en", "your/Mp/en", "my/Mp/en", "our/Mp/en", "thy/Mp/en"}
_inanimate = {"it/Ci/en", "itself/Ci/en", "its/Mp/en"}

_p1 = {"i/Ci/en", "me/Ci/en", "we/Ci/en", "us/Ci/en", "myself/Ci/en",
       "mine/Ci/en", "oneself/Ci/en", "ourselves/Ci/en", "my/Mp/en",
       "our/Mp/en"}
_p2 = {"you/Ci/en", "yourself/Ci/en", "yours/Ci/en", "your/Mp/en", "thy/Mp/en"}
_p3 = {"it/Ci/en", "she/Ci/en", "they/Ci/en", "he/Ci/en", "them/Ci/en",
       "her/Ci/en", "herself/Ci/en", "him/Ci/en", "itself/Ci/en",
       "himself/Ci/en", "one/Ci/en", "'em/Ci/en", "themselves/Ci/en",
       "hers/Ci/en", "somebody/Ci/en", "theirs/Ci/en", "her/Mp/en",
       "his/Mp/en", "its/Mp/en", "their/Mp/en", "whose/Mp/en"}


class ParserEN(AlphaBeta):
    def __init__(self, lemmas=False, resolve_corefs=False):
        super().__init__('en_core_web_lg', lemmas=lemmas,
                         resolve_corefs=resolve_corefs)
        self.lang = 'en'

    # ===========================================
    # Implementation of language-specific methods
    # ===========================================

    def atom_gender(self, atom):
        atom_str = atom.to_str()
        if atom_str in _female:
            return 'female'
        elif atom_str in _male:
            return 'male'
        elif atom_str in _neutral:
            return 'neutral'
        else:
            return None

    def atom_number(self, atom):
        atom_str = atom.to_str()
        if atom_str in _singular:
            return 'singular'
        elif atom_str in _plural:
            return 'plural'
        else:
            return None

    def atom_person(self, atom):
        atom_str = atom.to_str()
        if atom_str in _p1:
            return 1
        elif atom_str in _p2:
            return 2
        elif atom_str in _p3:
            return 3
        else:
            return None

    def atom_animacy(self, atom):
        atom_str = atom.to_str()
        if atom_str in _animate:
            return 'animate'
        elif atom_str in _inanimate:
            return 'inanimate'
        else:
            return None

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
        elif token.dep_ in {'dobj', 'pobj', 'prt'}:
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
                return 'Pd'
            else:
                return self._concept_type_and_subtype(token)
        elif dep in {'appos', 'attr', 'dative', 'dep', 'dobj', 'nsubj',
                     'nsubjpass', 'oprd', 'pobj', 'meta'}:
            return self._concept_type_and_subtype(token)
        elif dep in {'advcl', 'csubj', 'csubjpass', 'parataxis'}:
            return 'Pd'
        elif dep in {'relcl', 'ccomp'}:
            if self._is_verb(token):
                return 'Pr'
            else:
                return self._concept_type_and_subtype(token)
        elif dep in {'acl', 'pcomp', 'xcomp'}:
            if token.tag_ == 'IN':
                return self._modifier_type_and_subtype(token)
            else:
                return 'P'
        elif dep in {'amod', 'nummod', 'preconj'}:  # , 'predet'}:
            return self._modifier_type_and_subtype(token)
        elif dep == 'det':
            if token.head.dep_ == 'npadvmod':
                return self._builder_type_and_subtype(token)
            else:
                return self._modifier_type_and_subtype(token)
        elif dep in {'aux', 'auxpass', 'expl', 'prt', 'quantmod'}:
            if head_type in {'C', 'M'}:
                return self._modifier_type_and_subtype(token)
            if token.n_lefts + token.n_rights == 0:
                return self._modifier_type_and_subtype(token)
            else:
                return 'T'
        elif dep in {'nmod', 'npadvmod'}:
            if token.head.dep_ == 'amod':
                return self._modifier_type_and_subtype(token)
            elif self._is_noun(token):
                return self._concept_type_and_subtype(token)
            else:
                return self._modifier_type_and_subtype(token)
        elif dep == 'predet':
            return self._modifier_type_and_subtype(token)
        if dep == 'compound':
            if token.tag_ == 'CD':
                return self._modifier_type_and_subtype(token)
            else:
                return self._concept_type_and_subtype(token)
        elif dep == 'cc':
            if head_type == 'P':
                return 'J'
            else:
                return self._builder_type_and_subtype(token)
        elif dep == 'case':
            if token.head.dep_ == 'poss':
                return 'Bp'
            else:
                return self._builder_type_and_subtype(token)
        elif dep == 'neg':
            return self._modifier_type_and_subtype(token)
        elif dep == 'agent':
            return 'T'
        elif dep in {'intj', 'punct'}:
            return ''
        elif dep == 'advmod':
            if token.head.dep_ == 'advcl':
                return 'T'
            else:
                return self._modifier_type_and_subtype(token)
        elif dep == 'poss':
            if self._is_noun(token):
                return self._concept_type_and_subtype(token)
            else:
                return self._modifier_type_and_subtype(token)
        elif dep == 'prep':
            if head_type == 'P':
                if token.n_lefts + token.n_rights == 0:
                    return self._modifier_type_and_subtype(token)
                else:
                    return 'T'
            elif head_type == 'T':
                return self._modifier_type_and_subtype(token)
            else:
                return self._builder_type_and_subtype(token)
        elif dep == 'conj':
            if head_type == 'P' and self._is_verb(token):
                return 'Pd'
            else:
                return self._concept_type_and_subtype(token)
        elif dep == 'mark':
            if head_type == 'P' and head_subtype != '':
                return 'T'
            else:
                return self._builder_type_and_subtype(token)
        elif dep == 'acomp':
            if self._is_verb(token):
                return 'T'
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
            return 'Cm'
        if tag[:2] == 'JJ':
            return 'Ca'
        elif tag[:2] == 'NN':
            subtype = 'p' if 'P' in tag else 'c'
            sing_plur = 'p' if tag[-1] == 'S' else 's'
            return 'C{}.{}'.format(subtype, sing_plur)
        elif tag == 'CD':
            return 'C#'
        elif tag == 'DT':
            return 'Cd'
        elif tag == 'WP':
            return 'Cw'
        elif tag == 'PRP':
            return 'Ci'
        else:
            return 'C'

    def _modifier_type_and_subtype(self, token):
        tag = token.tag_
        dep = token.dep_
        if dep == 'neg':
            return 'Mn'
        elif dep == 'poss':
            return 'Mp'
        elif dep == 'prep':
            return 'Mt'  # preposition
        elif tag == 'JJ':
            return 'Ma'
        elif tag == 'JJR':
            return 'Mc'
        elif tag == 'JJS':
            return 'Ms'
        elif tag == 'DT':
            return 'Md'
        elif tag == 'WDT':
            return 'Mw'
        elif tag == 'CD':
            return 'M#'
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
        tag = token.tag_
        if tag == 'IN':
            return 'Br'  # relational (proposition)
        # TODO: this is ugly, move this case elsewhere...
        elif tag == 'CC':
            return 'J'
        elif tag == 'DT':
            return 'Bd'
        else:
            return 'B'

    def _predicate_post_type_and_subtype(self, edge, subparts, args_string):
        # detecte imperative
        if subparts[0] == 'Pd':
            if subparts[2][1] == 'i' and 's' not in args_string:
                if hedge('to/Mi/en') not in edge[0].atoms():
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
        new_connector = connector
        if connector.is_atom():
            ct = connector.type()
            if ct == 'Br':
                new_connector = connector.replace_atom_part(
                    1, '{}.ma'.format(ct))
            elif ct == 'Bp':
                new_connector = connector.replace_atom_part(
                    1, '{}.am'.format(ct))
        if UniqueAtom(connector) in self.atom2token:
            self.atom2token[UniqueAtom(new_connector)] =\
                self.atom2token[UniqueAtom(connector)]
        return hedge((new_connector,) + edge[1:])

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
