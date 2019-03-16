import itertools
import progressbar
from graphbrain.funs import *


class Actions(object):
    def __init__(self):
        self.create_edges = []

    def create(self, edge):
        self.create_edges.append(edge)


def str2pattern(pattern_str):
    pattern = Pattern()
    pattern.open_ended = False
    pattern_edge = str2edge(pattern_str)
    if pattern_edge[-1] == '...':
        pattern.open_ended = True
        pattern_edge = pattern_edge[:-1]
    pattern.pattern = []
    for item in pattern_edge:
        if item == '*':
            pattern.pattern.append(None)
        else:
            pattern.pattern.append(item)
    return pattern


class Pattern(object):
    def __init__(self):
        self.open_ended = False
        self.pattern = None
        self.label = ''

    def variables(self):
        variables = []
        for i in range(len(self.pattern)):
            item = self.pattern[i]
            if item and is_symbol(item) and item[0] == ':':
                var = item[1:]
                variables.append((var, i))
        return variables

    def generate(self, rules):
        variables = self.variables()

        if len(variables) == 0:
            return [self]

        patterns = []
        domains = [rules.globals[var[0]] for var in variables]
        instances = list(itertools.product(*domains))
        for instance in instances:
            pattern = Pattern()
            pattern.open_ended = self.open_ended
            pattern.pattern = self.pattern[:]
            for i in range(len(variables)):
                pattern.pattern[variables[i][1]] = instance[i]
            pattern.label = '_'.join([edge2str(item) for item in instance])
            patterns.append(pattern)
        return patterns

    def apply_to(self, hg):
        return hg.pattern2edges(self.pattern, open_ended=self.open_ended)


class Rules(object):
    def __init__(self):
        self.rules = {}
        self.created = 0
        self.globals = {}

    def add_rule(self, pattern_str):
        def decorator(f):
            patterns = str2pattern(pattern_str).generate(self)
            for pattern in patterns:
                name = f.__name__
                if pattern.label != '':
                    name = '%s_%s' % (name, pattern.label)
                self.rules[name] = {'fun': f, 'pattern': pattern}
            return f
        return decorator

    def apply_to(self, hg, dry_run=False):
        rules = tuple(self.rules.keys())
        for i in progressbar.progressbar(range(len(rules)), redirect_stdout=True):
            rule = rules[i]
            for edge in self.rules[rule]['pattern'].apply_to(hg):
                actions = Actions()
                self.rules[rule]['fun'](hg, edge, actions)
                for new_edge in actions.create_edges:
                    self.created += 1
                    if not dry_run:
                        hg.add(new_edge)
            print('applied rule: %s; new edges: %s' % (rule, self.created))
