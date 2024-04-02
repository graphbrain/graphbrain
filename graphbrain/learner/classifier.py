import json
import logging

from graphbrain.hyperedge import hedge
from graphbrain.patterns import (is_wildcard, common_pattern, is_variable, contains_variable, remove_variables,
                                 all_variables, merge_patterns, apply_variables)
from graphbrain.learner.rule import Rule
from graphbrain.learner.rule import from_json as rule_from_json
from graphbrain.utils.lemmas import lemma


def edge_at(edge, pos):
    if len(pos) == 0:
        return edge
    subedge = edge[pos[0]]
    return edge_at(subedge, pos[1:])


def replace_at(edge, replace, pos):
    if len(pos) == 0:
        return replace
    subedges = []
    for i, subedge in enumerate(edge):
        if i == pos[0]:
            subedges.append(replace_at(subedge, replace, pos[1:]))
        else:
            subedges.append(subedge)
    return hedge(subedges)


def role(edge):
    if edge.atom:
        parts = str(edge).split('/')
        if len(parts) > 1:
            return parts[1].split('.')
    elif edge[0].mtype() == 'M':
        return role(edge[1])
    return []


def remove_arguments(edge, removals):
    pred = edge[0]
    argroles = pred.argroles()[1:-1]
    new_argroles = '{'
    for i, argrole in enumerate(argroles):
        if i not in removals:
            new_argroles += argrole
    new_argroles += '}'
    new_pred = pred.replace_argroles(new_argroles)
    new_edge = [new_pred]
    for i, arg in enumerate(edge[1:]):
        if i not in removals:
            new_edge.append(arg)
    return hedge(new_edge)


def apply_curly_brackets(edge):
    if edge.atom:
        if edge.mtype() in {'P', 'B'}:
            argroles = edge.argroles()
            if len(argroles) > 0 and argroles[0] != '{':
                return edge.replace_argroles('{' + argroles + '}')
        return edge
    else:
        return hedge([apply_curly_brackets(subedge) for subedge in edge])


def _atom_mappings2atom_fun(atom_mappings):
    atoms = [atom for atom in atom_mappings.values() if atom]
    if len(atoms) > 0:
        return hedge(['atoms'] + atoms)
    else:
        return None


class Classifier:
    def __init__(self, file_path=None, hg=None):
        self.file_path = file_path
        self.hg = hg
        self.rules = []
        self.cases = []
        self.false_positive_penalty = -1

    def classify(self, edge):
        for rule in [rule for rule in self.rules if not rule.positive]:
            if rule.matches(edge):
                return []
        for rule in [rule for rule in self.rules if rule.positive]:
            matches = rule.matches(edge)
            if len(matches) > 0:
                return matches
        return []

    def rule_triggered(self, edge):
        for i, rule in enumerate([rule for rule in self.rules if not rule.positive]):
            if rule.matches(edge):
                return -(i + 1)
        for i, rule in enumerate([rule for rule in self.rules if rule.positive]):
            if rule.matches(edge):
                return i + 1
        return 0

    def rules_triggered(self, edge):
        return [rule.index for rule in self.rules if rule.matches(edge)]

    def add_case(self, edge, positive, variables=None):
        if variables is None:
            variables = {}
        logging.debug('Classifier.add_case()')
        logging.debug('add case: {}; positive: {}'.format(edge, positive))
        vedge = apply_variables(edge, variables)
        logging.debug('case after apply_variables: {}'.format(vedge))

        if vedge is None:
            logging.debug('failed to add case.')
            return

        cases = []
        for case in self.cases:
            if remove_variables(case[0]) != edge:
                cases.append(case)
        cases.append((vedge, positive))
        self.cases = cases

    def assign_rule_case_matches(self, oneshot=True):
        for rule in self.rules:
            rule.case_matches = []

        for edge, positive in self.cases:
            nedge = edge
            for rule in self.rules:
                if rule.positive == positive:
                    if rule.matches(nedge):
                        rule.case_matches.append(nedge)
                        if oneshot:
                            break

    def score(self):
        s = 0
        for edge, positive in self.cases:
            if self.classify(edge):
                if positive:
                    s += 1
                else:
                    if self.false_positive_penalty < 0:
                        return -1
                    else:
                        s -= self.false_positive_penalty
        return s

    def test(self, rule):
        self.rules.append(rule)
        _score = self.score()
        self.rules.pop(-1)
        return _score

    def extract_patterns(self):
        logging.debug('Classifier.extract_patterns()')

        self.rules = []
        best_score = 0

        cases = [edge for edge, positive in self.cases if positive]
        while len(cases) > 0:
            print('-> cases: {}; rules: {}'.format(len(cases), len(self.rules)))
            for rule in self.rules:
                print(rule)
            print()
            logging.debug('{} remaining cases'.format(len(cases)))

            edge = cases[0]
            logging.debug('matching case: {}'.format(str(edge)))
        
            best_rule = None
            best_new_rule = None
            for rule in self.rules:
                cur_pattern = rule.pattern
                logging.debug('matching with rule: {}'.format(str(cur_pattern)))
                pattern = common_pattern(cur_pattern, edge)
                logging.debug('common pattern: {}'.format(pattern))
                if pattern:
                    rule.pattern = pattern
                    score = self.score()
                    rule.pattern = cur_pattern
                    if score > best_score:
                        logging.debug('best score found')
                        best_score = score
                        best_rule = rule
            for _edge in cases[1:]:
                if _edge:
                    logging.debug('matching with case: {}'.format(str(_edge)))
                    pattern = common_pattern(edge, _edge)
                    logging.debug('common pattern: {}'.format(pattern))
                    if pattern:
                        new_rule = Rule(True, hg=self.hg)
                        new_rule.pattern = pattern
                        score = self.test(new_rule)
                        if score > best_score:
                            logging.debug('best score found')
                            best_score = score
                            best_new_rule = new_rule
                            best_rule = None
            if best_rule:
                cur_pattern = best_rule.pattern
                best_rule.pattern = common_pattern(cur_pattern, edge)
                logging.debug('new merged rule: {}'.format(best_rule.pattern))
            elif best_new_rule:
                self.rules.append(best_new_rule)
                logging.debug('new merged case: {}'.format(best_new_rule))
            # create new rule
            else:
                rule = Rule(True, hg=self.hg)
                rule.pattern = apply_curly_brackets(edge)
                self.rules.append(rule)
                logging.debug('new rule: {}'.format(rule.pattern))

            cases = [edge for edge, positive in self.cases if positive and not self.classify(edge)]

    def generalize_relation_rule(self, rule, pos, score):
        edge = edge_at(rule.pattern, pos)
        nargs = len(edge) - 1
        removals = []
        best_rule = rule
        for i in range(nargs - 1, -1, -1):
            if not contains_variable(edge[i + 1]):
                removals.append(i)
                new_edge = remove_arguments(edge, removals)
                new_rule = rule.copy()
                new_rule.pattern = replace_at(rule.pattern, new_edge, pos)
                if self.test(new_rule) < score:
                    removals.pop(-1)
                else:
                    best_rule = new_rule
                # leave at least one argument
                if len(removals) == nargs - 1:
                    break
        return best_rule

    def generalize_rule(self, rule, pos, score):
        logging.debug('Classifier.generalize_rule()')
        logging.debug('rule: {}; pos: {}'.format(rule, pos))
        cur_edge = edge_at(rule.pattern, pos)
        # deal with var functional pattern
        if is_variable(cur_edge):
            return self.generalize_rule(rule, pos + [1], score)

        # most general
        if not contains_variable(cur_edge):
            if cur_edge.mt in {'P', 'B'} and cur_edge.argroles() != '':
                new_rule = rule.copy()
                new_rule.pattern = replace_at(rule.pattern,
                                              hedge('*/{}.{}'.format(cur_edge.mt, cur_edge.argroles())),
                                              pos)
                if self.test(new_rule) >= score:
                    return new_rule
            else:
                new_rule = rule.copy()
                new_rule.pattern = replace_at(rule.pattern, hedge('*'), pos)
                if self.test(new_rule) >= score:
                    return new_rule

            new_rule = rule.copy()

            # try atoms
            atoms = [atom for atom in cur_edge.atoms() if not is_wildcard(atom)]
            atom_mappings = {}
            for atom in atoms:
                atom_mappings[atom] = atom
            atoms_fun = _atom_mappings2atom_fun(atom_mappings)
            if atoms_fun:
                new_rule.pattern = replace_at(new_rule.pattern, atoms_fun, pos)
                logging.debug('Trying atoms fun pattern: {}'.format(new_rule.pattern))
                # if specifying all atoms work, try ablations and lemmatizations
                if self.test(new_rule) >= score:
                    logging.debug(
                        'Atoms fun pattern worked: {}. Trying ablations and lemmatizations.'.format(new_rule.pattern))

                    # heuristic: try to remove atoms with no argroles first
                    for atom in sorted(atoms, key=lambda x: len(x.argroles())):
                        atom_mappings[atom] = None
                        atoms_fun = _atom_mappings2atom_fun(atom_mappings)
                        if atoms_fun:
                            new_rule.pattern = replace_at(new_rule.pattern, atoms_fun, pos)
                            if self.test(new_rule) >= score:
                                logging.debug('Ablation of atom {} successful: {}'.format(atom, new_rule.pattern))
                                continue
                        if atom.root() not in {':', '+'}:
                            atom_mappings[atom] = self._atom2lemmafun(atom)
                            atoms_fun = _atom_mappings2atom_fun(atom_mappings)
                            if atoms_fun:
                                new_rule.pattern = replace_at(
                                    new_rule.pattern, _atom_mappings2atom_fun(atom_mappings), pos)
                                if self.test(new_rule) >= score:
                                    if self.test(new_rule) >= score:
                                        logging.debug(
                                            'Lemmatization of atom {} successful: {}'.format(atom, new_rule.pattern))
                                        continue
                        atom_mappings[atom] = atom
                    atoms_fun = _atom_mappings2atom_fun(atom_mappings)
                    if atoms_fun:
                        new_rule.pattern = replace_at(new_rule.pattern, atoms_fun, pos)
                        logging.debug(
                            'Atoms fun pattern after ablations and lemmatizations: {}'.format(new_rule.pattern))
                        return new_rule

        new_rule = rule.copy()

        if cur_edge.atom:
            if not is_wildcard(cur_edge) and cur_edge.root() not in {':', '+'}:
                # try lemma
                new_rule.pattern = replace_at(rule.pattern, self._atom2lemmafun(cur_edge), pos)
                if self.test(new_rule) < score:
                    new_rule = rule.copy()
                return new_rule
        else:
            # directional heuristic
            if cur_edge.cmt == 'M':
                for i in range(len(cur_edge)):
                    new_rule = self.generalize_rule(new_rule, pos + [i], score)
            else:
                for i in range(len(cur_edge) - 1, -1, -1):
                    new_rule = self.generalize_rule(new_rule, pos + [i], score)
            if cur_edge[0].mtype() == 'P':
                new_rule = self.generalize_relation_rule(new_rule, pos, score)
        return new_rule

    def generalize(self):
        logging.debug('Classifier.generalize()')

        rules = self.rules
        new_rules = []
        score = self.score()
        for rule in rules:
            self.rules = []
            cur_rule = rule
            for _rule in rules:
                if _rule != cur_rule:
                    self.rules.append(_rule)
            new_rule = self.generalize_rule(cur_rule, [], score)
            new_rules.append(new_rule)
        self.rules = new_rules

    # remove redundant rules
    def trim(self):
        logging.debug('Classifier.trim()')

        score = self.score()
        rules = self.rules

        done = False
        while not done:
            done = True
            for rule in rules:
                new_rules = [_rule for _rule in rules if _rule != rule]
                self.rules = new_rules
                if self.score() >= score:
                    rules = new_rules
                    done = False
                    break

        self.rules = rules

    def merge(self):
        logging.debug('Classifier.merge()')

        rules = self.rules
        new_rules = self.rules
        score = self.score()

        for rule in rules[1:]:
            logging.debug('Trying to merge rule: {}'.format(rule.pattern))
            for cur_rule in new_rules:
                if cur_rule != rule:
                    logging.debug('with rule: {}'.format(rule.pattern))
                    self.rules = [r for r in new_rules if r != rule and r != cur_rule]
                    new_rule = cur_rule.copy()
                    new_rule.pattern = merge_patterns(cur_rule.pattern, rule.pattern)
                    logging.debug('merged rule: {}'.format(new_rule.pattern))
                    if new_rule.pattern and self.test(new_rule) >= score:
                        logging.debug('merged rule accepted')
                        new_rules = self.rules
                        new_rules.append(new_rule)
                        break

        self.rules = new_rules

    def learn(self):
        logging.debug('Classifier.learn()')
        self.extract_patterns()
        self.generalize()
        self.trim()
        self.merge()
        self._index_rules()

    def suggest_variables(self):
        variables = {}
        for rule in self.rules:
            for variable in all_variables(rule.pattern):
                variables[variable] = None
        return variables

    def _index_rules(self):
        index = 1
        for rule in self.rules:
            rule.index = index
            index += 1

    def _atom2lemmafun(self, atom):
        _lemma = lemma(self.hg, atom, same_if_none=True)
        root = _lemma.root()
        _role = _lemma.type()
        argroles = atom.argroles()
        if len(argroles) > 0:
            _role = '{}.{}'.format(_role, argroles)
        atom = hedge('{}/{}'.format(root, _role))
        return hedge(('lemma', atom))

    def to_json(self):
        rules = [rule.to_json() for rule in self.rules]
        cases = [(edge.to_str(), positive) for edge, positive in self.cases]
        return {'rules': rules, 'cases': cases}

    def save(self):
        if self.file_path:
            with open(self.file_path, 'wt') as f:
                f.write(json.dumps(self.to_json()))


def from_json(json_obj, file_path=None, hg=None):
    cls = Classifier(file_path=file_path, hg=hg)
    cls.rules = [rule_from_json(rule_json, hg=hg) for rule_json in json_obj['rules']]
    cls._index_rules()
    cls.cases = [(hedge(edge_str), positive) for edge_str, positive in json_obj['cases']]
    return cls


def from_file(file_path, hg=None):
    with open(file_path, 'rt') as f:
        json_str = f.read()
    return from_json(json.loads(json_str), file_path=file_path, hg=hg)
