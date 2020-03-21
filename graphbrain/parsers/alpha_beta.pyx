from itertools import repeat
from collections import defaultdict, Counter
import logging
import spacy
import neuralcoref
from graphbrain import *
import graphbrain.constants as const
from graphbrain.meaning.concepts import has_common_or_proper_concept
from .parser import Parser
from .text import UniqueAtom


def insert_after_predicate(targ, orig):
    targ_type = targ.type()
    if targ_type[0] == 'p':
        return hedge((targ, orig))
    elif targ_type[0] == 'r':
        if targ_type == 'rm':
            inner_rel = insert_after_predicate(targ[1], orig)
            if inner_rel:
                return hedge((targ[0], inner_rel) + tuple(targ[2:]))
            else:
                return None
        else:
            return targ.insert_first_argument(orig)
    else:
        return targ.insert_first_argument(orig)
    # else:
    #     logging.warning(('Wrong target type (insert_after_predicate).'
    #                      ' orig: {}; targ: {}').format(targ, orig))
    #     return None


def nest_predicate(inner, outer, before):
    if inner.type() == 'rm':
        first_rel = nest_predicate(inner[1], outer, before)
        return hedge((inner[0], first_rel) + tuple(inner[2:]))
    elif inner.is_atom() or inner.type()[0] == 'p':
        return hedge((outer, inner))
    else:
        return hedge(((outer, inner[0]),) + inner[1:])


def enclose(connector, edge):
    if edge.is_atom() or edge[0].type() != 'b+':
        return hedge((connector, edge))
    else:
        return hedge((edge[0], enclose(connector, edge[1])) + edge[2:])


# example:
# applying (and/b+ bank/cm (credit/cn.s card/cn.s)) to records/cn.p
# yields:
# (and/b+
#     (+/b.am bank/c records/cn.p)
#     (+/b.am (credit/cn.s card/cn.s) records/cn.p))
def _apply_aux_concept_list_to_concept(con_list, concept):
    concepts = tuple([('+/b.am', item, concept) for item in con_list[1:]])
    return hedge((con_list[0],) + concepts)


def _concept_scores(edge, scores=None):
    if scores is None:
        scores = {'proper': 0, 'common': 0, 'misc': 0}
    if edge is None:
        return scores
    if edge.is_atom():
        et = edge.type()
        if et == 'cp':
            scores['proper'] += 1
        elif et == 'cc':
            scores['common'] += 1
        else:
            scores['misc'] += 1
    else:
        for subedge in edge:
            _concept_scores(subedge, scores)
    return scores


def _is_second_concept_better(edge1, edge2):
    score1 = _concept_scores(edge1)
    score2 = _concept_scores(edge2)
    if score1['proper'] < score2['proper']:
        return True
    elif score1['proper'] == score2['proper']:
        if score1['common'] < score2['common']:
            return True
        elif score1['common'] == score2['common']:
            if score1['misc'] < score2['misc']:
                if score2['proper'] > 0 or score2['common'] > 0:
                    return True
    return False


class AlphaBeta(Parser):
    def __init__(self, model_name, lemmas=False, resolve_corefs=False):
        super().__init__(lemmas=lemmas, resolve_corefs=resolve_corefs)
        self.atom2token = None
        self.edge2text = None
        self.coref_clusters = None
        self.edge2coref = None
        self.cur_text = None
        self.extra_edges = set()
        self.nlp = spacy.load(model_name)
        if resolve_corefs:
            coref = neuralcoref.NeuralCoref(self.nlp.vocab)
            self.nlp.add_pipe(coref, name='neuralcoref')

    # ========================================================================
    # Language-specific abstract methods, to be implemented in derived classes
    # ========================================================================

    def _arg_type(self, token):
        raise NotImplementedError()

    def _token_type(self, token):
        raise NotImplementedError()

    def _concept_type_and_subtype(self, token):
        raise NotImplementedError()

    def _modifier_type_and_subtype(self, token):
        raise NotImplementedError()

    def _builder_type_and_subtype(self, token):
        raise NotImplementedError()

    def _auxiliary_type_and_subtype(self, token):
        raise NotImplementedError()

    def _predicate_post_type_and_subtype(self, edge, subparts, args_string):
        raise NotImplementedError()

    def _concept_role(self, concept):
        raise NotImplementedError()

    def _builder_arg_roles(self, edge):
        raise NotImplementedError()

    def _is_noun(token):
        raise NotImplementedError()

    def _is_compound(self, token):
        raise NotImplementedError()

    def _is_relative_concept(self, token):
        raise NotImplementedError()

    def _is_verb(self, token):
        raise NotImplementedError()

    def _verb_features(self, token):
        raise NotImplementedError()

    # =========================
    # Language-agnostic methods
    # =========================

    def _token_head_type(self, token):
        head = token.head
        if head and head != token:
            return self._token_type(head)
        else:
            return ''

    def _build_atom(self, token, ent_type, last_token):
        text = token.text.lower()
        et = ent_type

        if ent_type[0] == 'p' and ent_type != 'pm':
            atom = self._build_atom_predicate(token, ent_type, last_token)
        elif ent_type[0] == 'x':
            atom = self._build_atom_subpredicate(token, ent_type)
        elif ent_type[0] == 'a':
            atom = self._build_atom_auxiliary(token, ent_type)
        else:
            atom = build_atom(text, et, self.lang)

        self.atom2token[UniqueAtom(atom)] = token
        return atom

    def _build_atom_predicate(self, token, ent_type, last_token):
        text = token.text.lower()
        et = ent_type

        # create verb features string
        verb_features = self._verb_features(token)

        # first naive assignment of predicate subtype
        # (can be revised at post-processing stage)
        if len(ent_type) == 1:
            # interrogative cases
            if (last_token and
                    last_token.tag_ == '.' and
                    last_token.dep_ == 'punct' and
                    last_token.lemma_.strip() == '?'):
                ent_type = 'p?'
            # declarative (by default)
            else:
                ent_type = 'pd'

        et = '{}..{}'.format(ent_type, verb_features)

        return build_atom(text, et, self.lang)

    def _build_atom_subpredicate(self, token, ent_type):
        text = token.text.lower()
        et = ent_type

        if self._is_verb(token):
            # create verb features string
            verb_features = self._verb_features(token)
            et = 'xv.{}'.format(verb_features)

        return build_atom(text, et, self.lang)

    def _build_atom_auxiliary(self, token, ent_type):
        text = token.text.lower()

        if self._is_verb(token):
            # create verb features string
            verb_features = self._verb_features(token)
            et = 'av.{}'.format(verb_features)  # verbal subtype
        else:
            et = self._auxiliary_type_and_subtype(token)

        if et == 'a':
            et = ent_type

        return build_atom(text, et, self.lang)

    def _compose_concepts(self, concepts):
        first = concepts[0]
        if first.is_atom() or first[0].type()[0] != 'm':
            concept_roles = [self._concept_role(concept)
                             for concept in concepts]
            builder = '+/b.{}/.'.format(''.join(concept_roles))
            return hedge(builder).connect(concepts)
        else:
            return hedge((first[0],
                          self._compose_concepts(first[1:] + concepts[1:])))

    def _post_process(self, entity):
        if entity.is_atom():
            token = self.atom2token.get(UniqueAtom(entity))
            if token:
                ent_type = self.atom2token[UniqueAtom(entity)].ent_type_
                temporal = ent_type in {'DATE', 'TIME'}
            else:
                temporal = False
            return entity, temporal
        else:
            entity, temps = zip(*[self._post_process(item) for item in entity])
            entity = hedge(entity)
            temporal = True in temps
            ct = entity.connector_type()

            # Multi-noun concept, e.g.: (south america) -> (+ south america)
            if ct[0] == 'c':
                return self._compose_concepts(entity), temporal

            # Assign concept roles where possible
            # e.g. (on/br referendum/c (gradual/m (nuclear/m phaseout/c))) ->
            # (on/br.ma referendum/c (gradual/m (nuclear/m phaseout/c)))
            elif ct[0] == 'b' and len(entity) == 3:
                return self._builder_arg_roles(entity), temporal

            # Builders with one argument become modifiers
            # e.g. (on/b ice) -> (on/m ice)
            elif ct[0] == 'b' and entity[0].is_atom() and len(entity) == 2:
                ps = entity[0].parts()
                ps[1] = 'm' + ct[1:]
                return hedge(('/'.join(ps),) + entity[1:]), temporal

            # A meta-modifier applied to a concept defined my a modifier
            # should apply to the modifier instead.
            # e.g.: (stricking/w (red/m dress)) -> ((stricking/w red/m) dress)
            elif (ct[0] == 'w' and
                    entity[0].is_atom() and
                    len(entity) == 2 and
                    not entity[1].is_atom() and
                    entity[1].connector_type()[0] == 'm'):
                return (hedge(((entity[0], entity[1][0]),) + entity[1][1:]),
                        temporal)

            # Make sure that specifier arguments are of the specifier type,
            # entities are surrounded by an edge with a trigger connector
            # if necessary. E.g.: today -> {t/t/. today}
            elif ct[0] == 'p':
                pred = entity.predicate()
                if pred:
                    role = pred.role()
                    if len(role) > 2:
                        arg_roles = role[2]
                        if 'x' in arg_roles:
                            proc_edge = list(entity)
                            trigger = 't/tt/.' if temporal else 't/t/.'
                            for i, arg_role in enumerate(arg_roles):
                                arg_pos = i + 1
                                if (arg_role == 'x' and
                                        arg_pos < len(proc_edge) and
                                        proc_edge[arg_pos].is_atom()):
                                    tedge = (hedge(trigger),
                                             proc_edge[arg_pos])
                                    proc_edge[arg_pos] = hedge(tedge)
                            return hedge(proc_edge), False
                return entity, temporal

            # Make triggers temporal, if appropriate.
            # e.g.: (in/t 1976) -> (in/tt 1976)
            elif ct[0] == 't':
                if temporal:
                    trigger_atom = entity[0].atom_with_type('t')
                    triparts = trigger_atom.parts()
                    newparts = (triparts[0], 'tt')
                    if len(triparts) > 2:
                        newparts += tuple(triparts[2:])
                    new_trigger = hedge('/'.join(newparts))
                    if UniqueAtom(trigger_atom) in self.atom2token:
                        self.atom2token[UniqueAtom(new_trigger)] =\
                            self.atom2token[UniqueAtom(trigger_atom)]
                    entity = entity.replace_atom(trigger_atom, new_trigger)
                return entity, False
            else:
                return entity, temporal

    def _before_parse_sentence(self):
        self.extra_edges = set()

    def _parse_token_children(self, token):
        children = []
        token_dict = {}
        pos_dict = {}

        child_tokens = (tuple(zip(token.lefts, repeat(True))) +
                        tuple(zip(token.rights, repeat(False))))

        for child_token, pos in child_tokens:
            child, _ = self._parse_token(child_token)
            if child:
                child_type = child.type()
                if child_type:
                    children.append(child)
                    token_dict[child] = child_token
                    pos_dict[child] = pos

        children.reverse()

        if len(child_tokens) > 0:
            last_token = child_tokens[-1][0]
        else:
            last_token = None

        return children, token_dict, pos_dict, last_token

    def _add_lemmas(self, token, entity, ent_type):
        text = token.lemma_.lower()
        if text != token.text.lower():
            lemma = build_atom(text, ent_type[0], self.lang)
            lemma_edge = hedge((const.lemma_pred,
                                entity.simplify_role(),
                                lemma))
            self.extra_edges.add(lemma_edge)

    def _is_post_parse_token_necessary(self, entity):
        if entity.is_atom():
            return False
        else:
            ct = entity.connector_type()
            if ct[0] == 'p':
                # Extend predicate atom with argument types
                if len(ct) < 2 or ct[1] != 'm':
                    pred = entity.atom_with_type('p')
                    subparts = pred.parts()[1].split('.')

                    if subparts[1] == '':
                        return True

            return any([self._is_post_parse_token_necessary(subentity)
                        for subentity in entity])

    def _post_parse_token(self, entity, token_dict):
        new_entity = entity

        if self._is_post_parse_token_necessary(entity):
            ct = entity.connector_type()
            if ct[0] == 'p':
                # Extend predicate atom with argument types
                if len(ct) < 2 or ct[1] != 'm':
                    pred = entity.atom_with_type('p')
                    subparts = pred.parts()[1].split('.')

                    if subparts[1] == '':
                        args = [self._arg_type(token_dict[param])
                                for param in entity[1:]]
                        args_string = ''.join(args)
                        pt = self._predicate_post_type_and_subtype(
                            entity, subparts, args_string)
                        new_part = '{}.{}.{}'.format(pt,
                                                     args_string,
                                                     subparts[2])
                        new_pred = pred.replace_atom_part(1, new_part)
                        self.atom2token[UniqueAtom(new_pred)] =\
                            self.atom2token[UniqueAtom(pred)]
                        new_entity = entity.replace_atom(pred, new_pred)

            new_args = [self._post_parse_token(subentity, token_dict)
                        for subentity in new_entity[1:]]
            new_entity = hedge([new_entity[0]] + new_args)

        return new_entity

    def _parse_token(self, token):
        # check what type token maps to, return None if if maps to nothing
        ent_type = self._token_type(token)
        if ent_type == '' or ent_type is None:
            return None, None

        # parse token children
        children, token_dict, pos_dict, last_token =\
            self._parse_token_children(token)

        atom = self._build_atom(token, ent_type, last_token)
        entity = atom
        logging.debug('ATOM: {}'.format(atom))

        # lemmas
        if self.lemmas:
            self._add_lemmas(token, entity, ent_type)

        # process children
        relative_to_concept = []
        next_child = None
        for i in range(len(children)):
            child = children[i]
            child_token = token_dict[child]
            pos = pos_dict[child]
            if next_child is not None:
                child = next_child
                next_child = None

            if i < len(children) - 1:
                child_up = children[i + 1]
            else:
                child_up = None

            child_type = child.type()

            logging.debug('entity: [%s] %s', ent_type, entity)
            logging.debug('child: [%s] %s', child_type, child)

            if child_type[0] in {'c', 'r', 'd', 's'}:
                if ent_type[0] == 'c':
                    if (child.connector_type() in {'pc', 'pr'} or
                            self._is_relative_concept(child_token)):
                        logging.debug('choice: 1')
                        # RELATIVE TO CONCEPT
                        relative_to_concept.append(child)
                    elif child.connector_type()[0] == 'b':
                        if (child.connector_type() == 'br' and
                                len(child) >= 3 and
                                'b+' not in [c.type() for c in children]):
                            logging.debug('choice: 2a')
                            # RELATIVE TO CONCEPT
                            relative_to_concept.append(child)
                        elif (child.connector_type() == 'b+' and
                                child.contains_atom_type('cm')):
                            logging.debug('choice: 2b')
                            # CONCEPT LIST
                            entity = _apply_aux_concept_list_to_concept(child,
                                                                        entity)
                        elif (child_up and
                              child_up.type()[0] == 'c' and
                              'b+' in [c.type() for c in children[i + 2:]]):
                            logging.debug('choice: 2c')
                            next_child = child_up.nest(child)
                        elif entity.connector_type()[0] == 'c':
                            # NEST
                            if len(child) == 2:
                                logging.debug('choice: 3a')
                                new_child = child
                                if len(child) > 2:
                                    new_child = hedge((child[0], child[1:]))
                                entity = entity.nest(new_child, pos)
                            # SEQUENCE
                            else:
                                logging.debug('choice: 3b')
                                entity = entity.sequence(child, pos,
                                                         flat=False)
                        else:
                            logging.debug('choice: 4a')
                            # NEST AROUND ORIGINAL ATOM
                            if atom.type()[0] == 'c' and len(child) > 2:
                                new_child = hedge((child[0], child[1:]))
                                entity = entity.replace_atom(
                                    atom,
                                    atom.nest(new_child, pos))
                            else:
                                logging.debug('choice: 4b')
                                # NEST AROUND ORIGINAL ATOM
                                entity = entity.replace_atom(
                                    atom,
                                    atom.nest(child, pos))
                    elif child.connector_type()[0] in {'x', 't'}:
                        logging.debug('choice: 5')
                        # NEST
                        entity = entity.nest(child, pos)
                    else:
                        if ((atom.type()[0] == 'c' and
                                child.connector_type()[0] == 'c') or
                                self._is_compound(child_token)):
                            if entity.connector_type()[0] == 'c':
                                if (child.connector_type()[0] == 'c' and
                                        entity.connector_type() != 'cm'):
                                    logging.debug('choice: 6')
                                    # SEQUENCE
                                    entity = entity.sequence(child, pos)
                                else:
                                    logging.debug('choice: 7')
                                    # FLAT SEQUENCE
                                    entity = entity.sequence(
                                        child, pos, flat=False)
                            elif (entity.connector_type() == 'br' and
                                  not self._is_compound(child_token)):
                                logging.debug('choice: 7b')
                                # NEST
                                entity = child.nest(entity, before=pos)
                            else:
                                logging.debug('choice: 8')
                                # SEQUENCE IN ORIGINAL ATOM
                                entity = entity.replace_atom(
                                    atom,
                                    atom.sequence(child, pos))
                        else:
                            logging.debug('choice: 9')
                            entity = entity.replace_atom(
                                atom, atom.connect((child,)))
                elif ent_type[0] in {'p', 'r', 'd', 's'}:
                    logging.debug('choice: 10')
                    # INSERT AFTER PREDICATE
                    result = insert_after_predicate(entity, child)
                    if result:
                        entity = result
                    else:
                        logging.warning(('insert_after_predicate failed'
                                         'with: {}').format(self.cur_text))
                else:
                    logging.debug('choice: 11')
                    # INSERT FIRST ARGUMENT
                    entity = entity.insert_first_argument(child)
            elif child_type[0] == 'b':
                if entity.connector_type()[0] == 'c':
                    logging.debug('choice: 12')
                    # CONNECT
                    entity = child.connect(entity)
                else:
                    logging.debug('choice: 13')
                    entity = entity.nest(child, pos)
            elif child_type[0] == 'p':
                # TODO: Pathological case
                # e.g. "Some subspecies of mosquito might be 1s..."
                if child_type == 'pm':
                    logging.debug('choice: 14')
                    # ?
                    entity = child + entity
                else:
                    logging.debug('choice: 15')
                    # CONNECT
                    entity = entity.connect((child,))
            elif child_type[0] in {'m', 'x', 't'}:
                logging.debug('choice: 16')
                # ?
                entity = enclose(child, entity)
            elif child_type[0] == 'a':
                logging.debug('choice: 17')
                # NEST PREDICATE
                entity = nest_predicate(entity, child, pos)
            elif child_type == 'w':
                if ent_type[0] in {'d', 's'}:
                    logging.debug('choice: 18')
                    # NEST PREDICATE
                    entity = nest_predicate(entity, child, pos)
                else:
                    logging.debug('choice: 19')
                    # NEST
                    entity = entity.nest(child, pos)
            else:
                logging.warning('Failed to parse token (_parse_token): {}'
                                .format(token))
                logging.debug('choice: 20')
                # IGNORE
                pass

            ent_type = entity.type()
            logging.debug('result: [%s] %s\n', ent_type, entity)

        if len(relative_to_concept) > 0:
            relative_to_concept.reverse()
            entity = hedge((':/b/.', entity) + tuple(relative_to_concept))

        entity = self._post_parse_token(entity, token_dict)

        return entity, self.extra_edges

    def _generate_atom2word(self, edge):
        atom2word = {}
        atoms = edge.all_atoms()
        for atom in atoms:
            uatom = UniqueAtom(atom)
            if uatom in self.atom2token:
                token = self.atom2token[uatom]
                word = (token.text, token.i)
                atom2word[uatom] = word
        return atom2word

    def _parse_sentence(self, sent):
        try:
            self._before_parse_sentence()
            main_edge, extra_edges = self._parse_token(sent.root)
            if main_edge:
                main_edge, _ = self._post_process(main_edge)
                atom2word = self._generate_atom2word(main_edge)
            else:
                atom2word = {}

            return {'main_edge': main_edge,
                    'extra_edges': extra_edges,
                    'text': str(sent).strip(),
                    'atom2word': atom2word,
                    'spacy_sentence': sent}
        except Exception as e:
            logging.error('Caught exception: {} while parsing: "{}"'.format(
                str(e), str(sent)))
            return {'main_edge': None,
                    'extra_edges': [],
                    'text': '',
                    'atom2word': {},
                    'spacy_sentence': None}

    def _parse(self, text):
        """Transforms the given text into hyperedges + aditional information.
        Returns a sequence of dictionaries, with one dictionary for each
        sentence found in the text.

        Each dictionary contains the following fields:

        -> main_edge: the hyperedge corresponding to the sentence.

        -> extra_edges: aditional edges, e.g. connecting atoms that appear
        in the main_edge to their lemmas.

        -> text: the string of natural language text corresponding to the
        main_edge, i.e.: the sentence itself.

        -> atom2word: TODO

        -> spacy_sentence: the spaCy structure representing the sentence
        enriched with NLP annotations.
        """
        self.atom2token = {}
        self.coref_clusters = defaultdict(set)
        self.edge2coref = {}
        self.cur_text = text
        doc = self.nlp(text.strip())
        parses = tuple(self._parse_sentence(sent) for sent in doc.sents)
        return {'parses': parses, 'inferred_edges': []}

    def _find_coref_clusters(self, edge):
        clusters = set()
        if edge.is_atom():
            parts = edge.parts()
            if len(parts) > 2 and parts[2] == '.':
                return clusters
            if UniqueAtom(edge) in self.atom2token:
                token = self.atom2token[UniqueAtom(edge)]
                clusters = set(token._.coref_clusters)
            if len(clusters) == 0:
                return {None}
            else:
                return clusters
        else:
            for subedge in edge:
                clusters |= self._find_coref_clusters(subedge)
                if len(clusters) > 1:
                    return clusters
            return clusters

    def _assign_to_coref(self, edge):
        clusters = self._find_coref_clusters(edge)
        if len(clusters) > 1:
            if not edge.is_atom():
                for subedge in edge:
                    self._assign_to_coref(subedge)
        else:
            for cluster in clusters:
                if cluster is not None:
                    self.coref_clusters[cluster].add(edge)

    def _coref_inferences(self, main_edge, edges):
        results = []

        gender_cnt = Counter()
        number_cnt = Counter()
        animacy_cnt = Counter()
        for edge in edges:
            if edge.is_atom():
                gender = self.atom_gender(edge)
                if gender is not None:
                    gender_cnt[gender] += 1
                number = self.atom_number(edge)
                if number is not None:
                    number_cnt[number] += 1
                animacy = self.atom_animacy(edge)
                if animacy is not None:
                    animacy_cnt[animacy] += 1
            if edge != main_edge and has_common_or_proper_concept(edge):
                is_edge = hedge((const.is_pred, main_edge, edge))
                results.append(is_edge)

        gender_top = gender_cnt.most_common(2)
        if len(gender_top) == 1 or (len(gender_top) == 2 and
                                    gender_top[0][1] > gender_top[1][1]):
            gender = gender_top[0][0]
            gender_edge = hedge((const.gender_pred, main_edge, gender))
            results.append(gender_edge)
        number_top = number_cnt.most_common(2)
        if len(number_top) == 1 or (len(number_top) == 2 and
                                    number_top[0][1] > number_top[1][1]):
            number = number_top[0][0]
            number_edge = hedge((const.number_pred, main_edge, number))
            results.append(number_edge)
        animacy_top = animacy_cnt.most_common(2)
        if len(animacy_top) == 1 or (len(animacy_top) == 2 and
                                     animacy_top[0][1] > animacy_top[1][1]):
            animacy = animacy_top[0][0]
            animacy_edge = hedge((const.animacy_pred, main_edge, animacy))
            results.append(animacy_edge)
        return results

    def _resolve_corefs_edge(self, edge):
        if edge is None:
            return None
        elif edge in self.edge2coref:
            return self.edge2coref[edge]
        elif edge.is_atom():
            return edge
        # e.g. "ihr Hund", "son chien", "her dog", ...
        # (her/mp dog/cc) -> (poss/bp.am/. mary/cp dog/cc)
        elif (edge[0].type() == 'mp' and
              len(edge) == 2 and
              edge[0] in self.edge2coref):
            return hedge(
                (const.possessive_builder, self.edge2coref[edge[0]], edge[1]))
        else:
            return hedge([self._resolve_corefs_edge(subedge)
                          for subedge in edge])

    def _resolve_corefs(self, parse_results):
        for parse in parse_results['parses']:
            if parse['main_edge'] is not None:
                self._assign_to_coref(parse['main_edge'])

        inferred_edges = []

        for cluster in self.coref_clusters:
            best_concept = None
            for edge in self.coref_clusters[cluster]:
                if _is_second_concept_better(best_concept, edge):
                    best_concept = edge
            if best_concept is not None:
                for edge in self.coref_clusters[cluster]:
                    self.edge2coref[edge] = best_concept
                inferred_edges += self._coref_inferences(
                    best_concept, self.coref_clusters[cluster])

        for parse in parse_results['parses']:
            parse['resolved_corefs'] = self._resolve_corefs_edge(
                parse['main_edge'])

        parse_results['inferred_edges'] = inferred_edges
