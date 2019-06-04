from enum import Enum
from graphbrain import *


def is_var(atom):
    return is_atom(atom) and atom[0].isupper()


def pattern_and_variables(entity):
    pattern = []
    varset = set()
    for item in entity:
        if is_atom(item):
            if is_var(item):
                varset.add(item)
                pattern.append('*')
            else:
                pattern.append(item)
        else:
            pattern_, varset_ = pattern_and_variables(item)
            pattern.append(pattern_)
            varset |= varset_
    return pattern, varset


def match_variables(expression, entity, variables):
    if is_edge(expression) and is_atom(entity):
        return False
    for pair in zip(expression, entity):
        if is_atom(pair[0]):
            if is_var(pair[0]):
                var = pair[0]
                if var in variables:
                    if variables[var] != pair[1]:
                        return False
                else:
                    variables[var] = pair[1]
        elif is_edge(pair[1]):
            if not match_variables(pair[0], pair[1], variables):
                return False
    return True


def apply_variables(expression, variables):
    edge = []
    for entity in expression:
        if is_edge(entity):
            edge.append(apply_variables(entity, variables))
        elif is_var(entity):
            if entity in variables:
                edge.append(variables[entity])
            else:
                return None
        else:
            edge.append(entity)
    return tuple(edge)


class ActionType(Enum):
    REAL = 1
    DRY = 2
    LOG = 3


class Program(object):
    def __init__(self, hg, action_type=ActionType.REAL, verbose=False):
        self.hg = hg
        self.exprs = []
        self.action_type = action_type
        self.verbose = verbose
        self.triggered = 0
        self.added = 0
        self.added_edges = []

    def load(self, prog_path):
        with open(prog_path, 'r') as myfile:
            self.loads(myfile.read())

    def loads(self, prog_str):
        str_exp = ''
        expr_count = 0
        for line in prog_str.split('\n'):
            part = line.strip()
            if len(part) == 0 and len(str_exp) > 0:
                self.load_expr(str_exp)
                expr_count += 1
                str_exp = ''
            else:
                str_exp = '{} {}'.format(str_exp, part)
        if len(str_exp) > 0:
            self.load_expr(str_exp)
            expr_count += 1

        if self.verbose:
            print('{} expressions found.'.format(expr_count))

    def load_expr(self, str_exp):
        expr = str2ent(str_exp)
        self.exprs.append(expr)

    def list(self):
        for expr in self.exprs:
            print(ent2str(expr))

    def _add_edge(self, edge):
        if not self.hg.exists(edge):
            self.added += 1
        if self.action_type == ActionType.REAL:
            self.hg.add(edge)
        elif self.action_type == ActionType.LOG:
            self.added_edges.append(edge)

        if self.verbose:
            print('created: {}'.format(ent2str(edge)))

    def match_expressions(self, expressions):
        expr = expressions[0]
        pattern, _ = pattern_and_variables(expr)
        for entity in self.hg.pat2ents(pattern):
            variables = {}
            match = True
            for expr in expressions:
                if not match_variables(expr, entity, variables):
                    match = False
                    break
            if match:
                yield variables

    def eval_conditions(self, program):
        if program[0] == 'and':
            conds = program[1:]
        else:
            conds = [program]

        return self.match_expressions(conds)

    def eval_action(self, program, variables):
        if len(program) < 2:
            raise RuntimeError(
                'ERROR: actions must have at least two arguments.')
        pred = program[0]
        if pred == '+':
            for expr in program[1:]:
                edge = apply_variables(expr, variables)
                if edge:
                    self._add_edge(edge)
                    return True
                else:
                    return False
        else:
            raise RuntimeError(
                'ERROR: unknown action predicate: {}'.format(
                    ent2str(pred)))

    def eval_rule(self, rule):
        if len(rule) != 3:
            raise RuntimeError('ERROR: rules must have exactly two arguments.')

        for variables in self.eval_conditions(rule[1]):
            if self.eval_action(rule[2], variables):
                self.triggered += 1

    def eval_expr(self, expr):
        if is_edge(expr):
            pred = expr[0]
            if pred == 'rule':
                return self.eval_rule(expr)
            else:
                raise RuntimeError(
                    'ERROR: unknown expression predicate: {}'.format(
                        ent2str(pred)))
        else:
            raise RuntimeError('ERROR: expression cannot be an atom')

    def eval(self):
        self.triggered = 0
        self.added = 0

        for expr in self.exprs:
            self.eval_expr(expr)

        if self.verbose:
            print('triggered rules: {}'.format(self.triggered))
            print('added edges: {}'.format(self.added))


if __name__ == '__main__':
    hg = hypergraph('test.hg')
    hg.destroy()

    hg.add(str2ent('(is/pd graphbrain/c (so/m great/c)'))
    hg.add(str2ent('(is/pd graphbrain/c (not/m great/c))'))

    prog_text = """
(rule
    (and (P G X)
         (is/pd * (Z Y))
         (is/pd * (not/m Y)))
    (+ (xpto X Y)))
"""

    prog = Program(hg, verbose=True)
    prog.loads(prog_text)
    prog.eval()
