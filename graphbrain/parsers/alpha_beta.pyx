import traceback
from collections import defaultdict, Counter
import logging
import spacy
import graphbrain.neuralcoref as neuralcoref
from graphbrain import hedge, build_atom, UniqueAtom, non_unique
import graphbrain.constants as const
from graphbrain.meaning.concepts import has_common_or_proper_concept
from .parser import Parser


class Rule:
    def __init__(self, first_type, arg_types, size, connector=None):
        self.first_type = first_type
        self.arg_types = arg_types
        self.size = size
        self.connector = connector


rules = [
    Rule('C', {'C'}, 2, '+/B/.'),
    Rule('M', {'C', 'P', 'T', 'R'}, 2),
    Rule('B', {'C', 'R'}, 3),
    Rule('T', {'C', 'R'}, 2),
    Rule('P', {'C', 'R', 'S', 'P'}, 5),
    Rule('P', {'C', 'R', 'S', 'P'}, 4),
    Rule('P', {'C', 'R', 'S', 'P'}, 3),
    Rule('P', {'C', 'R', 'S', 'P'}, 2),
    Rule('J', {'C', 'R'}, 3),
    Rule('R', {'C', 'R'}, 2, ':/J/.')]


def apply_rule(rule, sentence, pos):
    for pivot_pos in range(rule.size):
        args = []
        pivot = None
        valid = True
        for i in range(rule.size):
            edge = sentence[pos - rule.size + i + 1]
            if i == pivot_pos:
                if edge.type()[0] == rule.first_type:
                    if rule.connector:
                        args.append(edge)
                    else:
                        pivot = edge
                else:
                    valid = False
                    break
            else:
                if edge.type()[0] in rule.arg_types:
                    args.append(edge)
                else:
                    valid = False
                    break
        if valid:
            if rule.connector:
                return hedge([rule.connector] + args)
            else:
                return hedge([pivot] + args)
    return None


# coref functions
def _concept_scores(edge, scores=None):
    if scores is None:
        scores = {'proper': 0, 'common': 0, 'misc': 0}
    if edge is None:
        return scores
    if edge.is_atom():
        et = edge.type()
        if et == 'Cp':
            scores['proper'] += 1
        elif et == 'Cc':
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
        self.token2atom = None
        self.depths = None
        self.connections = None
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

    def _token_type(self, token):
        raise NotImplementedError()

    def _concept_type_and_subtype(self, token):
        raise NotImplementedError()

    def _modifier_type_and_subtype(self, token):
        raise NotImplementedError()

    def _builder_type_and_subtype(self, token):
        raise NotImplementedError()

    def _predicate_post_type_and_subtype(self, edge, subparts, args_string):
        raise NotImplementedError()

    def _relation_arg_role(self, token):
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

    def _head_token(self, edge):
        atoms = [UniqueAtom(atom) for atom in edge.all_atoms()
                 if UniqueAtom(atom) in self.atom2token]
        min_depth = 9999999
        main_atom = None
        for atom in atoms:
            depth = self.depths[atom]
            if depth < min_depth:
                min_depth = depth
                main_atom = atom
        return self.atom2token[main_atom]

    def _token_head_type(self, token):
        head = token.head
        if head and head != token:
            return self._token_type(head)
        else:
            return ''

    def _build_atom(self, token, ent_type, last_token):
        text = token.text.lower()
        et = ent_type

        if ent_type[0] == 'P':
            atom = self._build_atom_predicate(token, ent_type, last_token)
        elif ent_type[0] == 'T':
            atom = self._build_atom_trigger(token, ent_type)
        elif ent_type[0] == 'M':
            atom = self._build_atom_modifier(token, ent_type)
        else:
            atom = build_atom(text, et, self.lang)
        return atom

    def _build_atom_predicate(self, token, ent_type, last_token):
        text = token.text.lower()
        et = ent_type

        # create verb features string
        verb_features = self._verb_features(token)

        # first naive assignment of predicate subtype
        # (can be revised at post-processing stage)
        if ent_type == 'Pd':
            # interrogative cases
            if (last_token and
                    last_token.tag_ == '.' and
                    last_token.dep_ == 'punct' and
                    last_token.lemma_.strip() == '?'):
                ent_type = 'P?'
            # declarative (by default)
            else:
                ent_type = 'Pd'

        et = '{}..{}'.format(ent_type, verb_features)

        return build_atom(text, et, self.lang)

    def _build_atom_trigger(self, token, ent_type):
        text = token.text.lower()
        et = ent_type

        if self._is_verb(token):
            # create verb features string
            verb_features = self._verb_features(token)
            et = 'Tv.{}'.format(verb_features)

        return build_atom(text, et, self.lang)

    def _build_atom_modifier(self, token, ent_type):
        text = token.text.lower()

        if self._is_verb(token):
            # create verb features string
            verb_features = self._verb_features(token)
            et = 'Mv.{}'.format(verb_features)  # verbal subtype
        else:
            et = self._modifier_type_and_subtype(token)

        if et == 'A':
            et = ent_type

        return build_atom(text, et, self.lang)

    def _is_temporal(self, edge):
        if edge.is_atom():
            token = self.atom2token.get(UniqueAtom(edge))
            if token:
                ent_type = self.atom2token[UniqueAtom(edge)].ent_type_
                if ent_type in {'DATE', 'TIME'}:
                    return True
            return False
        else:
            for subedge in edge:
                if self._is_temporal(subedge):
                    return True
            return False

    def _post_process(self, edge):
        if not edge.is_atom():
            edge = hedge([self._post_process(subedge) for subedge in edge])

            if edge.connector_type()[0] == 'M':
                # If a modifier is found to be aplied to a relation, then
                # apply it to the predicate of the relation instead.
                # (*/M (*/P ...)) -> ((*/M */P) ...))
                if len(edge) == 2 and edge[1].type()[0] == 'R':
                    edge = hedge(((edge[0], edge[1][0]),) + edge[1][1:])

            if edge.connector_type()[0] == 'P':
                # If a predicate appears outside of the connector position of
                # a relation, then transform it into the equivalent verbal
                # concept.
                # (*/P ... */P ...) -> (*/P ... */C ...)
                for subedge in edge[1:]:
                    if subedge.type()[0] == 'P':
                        pred_atom = subedge.atom_with_type('P')
                        parts = pred_atom.parts()
                        role = pred_atom.role()
                        role = ['Cv'] + role[1:]
                        newparts = (parts[0], '.'.join(role))
                        if len(parts) > 2:
                            newparts += tuple(parts[2:])
                        new_pred = hedge('/'.join(newparts))
                        if UniqueAtom(pred_atom) in self.atom2token:
                            self.atom2token[UniqueAtom(new_pred)] =\
                                self.atom2token[UniqueAtom(pred_atom)]
                        edge = edge.replace_atom(pred_atom, new_pred)

            if edge.connector_type()[0] == 'T':
                # Make triggers temporal, if appropriate.
                # e.g.: (in/T 1976) -> (in/Tt 1976)
                if self._is_temporal(edge):
                    trigger_atom = edge[0].atom_with_type('T')
                    triparts = trigger_atom.parts()
                    newparts = (triparts[0], 'Tt')
                    if len(triparts) > 2:
                        newparts += tuple(triparts[2:])
                    new_trigger = hedge('/'.join(newparts))
                    if UniqueAtom(trigger_atom) in self.atom2token:
                        self.atom2token[UniqueAtom(new_trigger)] =\
                            self.atom2token[UniqueAtom(trigger_atom)]
                    edge = edge.replace_atom(trigger_atom, new_trigger)

        return edge

    def _add_lemmas(self, token, entity, ent_type):
        text = token.lemma_.lower()
        lemma = build_atom(text, ent_type[0], self.lang)
        lemma_edge = hedge((const.lemma_pred, entity, lemma))
        self.extra_edges.add(lemma_edge)

    def _apply_arg_roles(self, edge):
        if edge.is_atom():
            return edge

        new_entity = edge

        # Extend predicate connectors with argument types
        if edge.connector_type()[0] == 'P':
            conn = edge.atom_with_type('P')
            subparts = conn.parts()[1].split('.')
            if subparts[1] == '':
                args = [self._relation_arg_role(param) for param in edge[1:]]
                args_string = ''.join(args)
                # TODO: this is done to detect imperative, to refactor
                pt = self._predicate_post_type_and_subtype(
                    edge, subparts, args_string)
                new_part = '{}.{}.{}'.format(pt, args_string, subparts[2])
                new_pred = conn.replace_atom_part(1, new_part)
                self.atom2token[UniqueAtom(new_pred)] =\
                    self.atom2token[UniqueAtom(conn)]
                new_entity = edge.replace_atom(conn, new_pred)

        # Extend builder connectors with argument types
        elif edge.connector_type()[0] == 'B':
            builder = edge.atom_with_type('B')
            subparts = builder.parts()[1].split('.')
            arg_roles = self._builder_arg_roles(edge)
            if len(arg_roles) > 0:
                if len(subparts) > 1:
                    subparts[1] = arg_roles
                else:
                    subparts.append(arg_roles)
                new_part = '.'.join(subparts)
                new_builder = builder.replace_atom_part(1, new_part)
                ubuilder = UniqueAtom(builder)
                if ubuilder in self.atom2token:
                    self.atom2token[UniqueAtom(new_builder)] =\
                        self.atom2token[ubuilder]
                new_entity = edge.replace_atom(builder, new_builder)

        new_args = [self._apply_arg_roles(subentity)
                    for subentity in new_entity[1:]]
        new_entity = hedge([new_entity[0]] + new_args)

        return new_entity

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

    def _parse_token(self, token):
        # check what type token maps to, return None if if maps to nothing
        ent_type = self._token_type(token)
        if ent_type == '' or ent_type is None:
            return None

        # last token is useful to determine predicate subtype
        tokens = list(token.lefts) + list(token.rights)
        last_token = tokens[-1] if len(tokens) > 0 else None

        atom = self._build_atom(token, ent_type, last_token)
        logging.debug('ATOM: {}'.format(atom))

        # lemmas
        if self.lemmas:
            self._add_lemmas(token, atom, ent_type)

        return atom

    # from the notebook: build_sentence(parse)
    def _build_atom_sequence(self, sentence):
        self.token2atom = {}

        atomseq = []
        for token in sentence:
            atom = self._parse_token(token)
            if atom:
                uatom = UniqueAtom(atom)
                self.atom2token[uatom] = token
                self.token2atom[token] = uatom
                atomseq.append(uatom)
        return atomseq

    def _compute_depths_and_connections(self, root, depth=0):
        if depth == 0:
            self.depths = {}
            self.connections = set()

        parent_atom = self.token2atom[root]
        self.depths[parent_atom] = depth
        for child in root.children:
            if child in self.token2atom:
                child_atom = self.token2atom[child]
                self.connections.add((parent_atom, child_atom))
                self.connections.add((child_atom, parent_atom))
                new_depth = depth + 1
                self._compute_depths_and_connections(child, depth=new_depth)

    def _is_pair_connected(self, atoms1, atoms2):
        for atom1 in atoms1:
            for atom2 in atoms2:
                if (atom1, atom2) in self.connections:
                    return True
        return False

    def _are_connected(self, atom_sets, connector_pos):
        conn = True
        for pos, arg in enumerate(atom_sets):
            if pos != connector_pos:
                if not self._is_pair_connected(atom_sets[connector_pos], arg):
                    conn = False
                    break
        return conn

    def _score(self, edges):
        atom_sets = [edge.all_atoms() for edge in edges]

        conn = False
        for pos in range(len(edges)):
            if self._are_connected(atom_sets, pos):
                conn = True
                break

        mdepth = 99999999
        for atom_set in atom_sets:
            for atom in atom_set:
                if atom in self.depths:
                    depth = self.depths[atom]
                    if depth < mdepth:
                        mdepth = depth

        return (10000000 if conn else 0) + (mdepth * 100)

    def _parse_atom_sequence(self, atom_sequence):
        if len(atom_sequence) < 2:
            yield atom_sequence
        else:
            result = atom_sequence

            actions = []
            for rule_number, rule in enumerate(rules):
                window_start = rule.size - 1
                for pos in range(window_start, len(result)):
                    new_edge = apply_rule(rule, result, pos)
                    if new_edge:
                        score = self._score(result[pos - window_start:pos + 1])
                        score -= rule_number
                        actions.append(
                            (rule, score, new_edge, window_start, pos))

            # sort by descending score
            actions.sort(key=lambda tup: tup[1], reverse=True) 

            for action in actions:
                rule, s, new_edge, window_start, pos = action
                new_sequence = (result[:pos - window_start] +
                                [new_edge] +
                                result[pos + 1:])

                logging.debug('rule: {}'.format(rule))
                logging.debug('score: {}'.format(score))
                logging.debug('new_edge: {}'.format(new_edge))
                logging.debug('new_sequence: {}'.format(new_sequence))

                if new_sequence != result:
                    for r in self._parse_atom_sequence(new_sequence):
                        if r and len(r) < 2:
                            yield r

    def parse_spacy_sentence(self, sent, atom_sequence=None):
        try:
            self.extra_edges = set()

            if atom_sequence is None:
                atom_sequence = self._build_atom_sequence(sent)

            self._compute_depths_and_connections(sent.root)

            main_edge = None
            for result in self._parse_atom_sequence(atom_sequence):
                if result and len(result) == 1:
                    main_edge = non_unique(result[0])
                    break

            if main_edge:
                main_edge = self._apply_arg_roles(main_edge)
                main_edge = self._post_process(main_edge)
                atom2word = self._generate_atom2word(main_edge)
            else:
                atom2word = {}

            return {'main_edge': main_edge,
                    'extra_edges': self.extra_edges,
                    'text': str(sent).strip(),
                    'atom2word': atom2word,
                    # TODO: HACK TEMPORARY
                    'atom2token': self.atom2token,
                    'spacy_sentence': sent}
        except Exception as e:
            if hasattr(e, 'message'):
                msg = e.message
            else:
                msg = str(e)
            logging.error('Caught exception: {} while parsing: "{}"'.format(
                msg, str(sent)))
            traceback.print_exc()
            return {'main_edge': None,
                    'extra_edges': [],
                    'text': str(sent).strip(),
                    'atom2word': {},
                    'spacy_sentence': sent}

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
        parses = tuple(self.parse_spacy_sentence(sent) for sent in doc.sents)
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
        # (her/Mp dog/Cc) -> (poss/Bp.am/. mary/Cp dog/Cc)
        elif (edge[0].type() == 'Mp' and
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
