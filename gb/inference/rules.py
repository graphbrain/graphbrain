#   Copyright (c) 2016 CNRS - Centre national de la recherche scientifique.
#   All rights reserved.
#
#   Written by Telmo Menezes <telmo@telmomenezes.com>
#
#   This file is part of GraphBrain.
#
#   GraphBrain is free software: you can redistribute it and/or modify
#   it under the terms of the GNU Affero General Public License as published by
#   the Free Software Foundation, either version 3 of the License, or
#   (at your option) any later version.
#
#   GraphBrain is distributed in the hope that it will be useful,
#   but WITHOUT ANY WARRANTY; without even the implied warranty of
#   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#   GNU Affero General Public License for more details.
#
#   You should have received a copy of the GNU Affero General Public License
#   along with GraphBrain.  If not, see <http://www.gnu.org/licenses/>.


import itertools
import progressbar
from gb.funs import *


class RuleOutput(object):
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

    def apply_to(self, hg):
        i = 0
        with progressbar.ProgressBar(max_value=len(self.rules)) as bar:
            for rule in self.rules:
                for edge in self.rules[rule]['pattern'].apply_to(hg):
                    output = RuleOutput()
                    self.rules[rule]['fun'](hg, edge, output)
                    for _ in output.create_edges:
                        self.created += 1
                print('%s [%s]' % (rule, self.created))
                i += 1
                bar.update(i)
