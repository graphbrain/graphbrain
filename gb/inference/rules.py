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


import progressbar


class RuleOutput(object):
    def __init__(self):
        self.create = []

    def create(self, edge):
        self.create.append(edge)


class Rules(object):
    def __init__(self):
        self.rules = {}
        self.created = 0

    def add_rule(self, name):
        def decorator(f):
            self.rules[name] = f
            return f
        return decorator

    def apply_to(self, hg):
        i = 0
        with progressbar.ProgressBar(max_value=hg.edge_count()) as bar:
            for edge in hg.all():
                if edge:
                    output = RuleOutput()
                    for name in self.rules:
                        self.rules[name](hg, edge, output)
                        for _ in output.create:
                            self.created += 1
                i += 1
                bar.update(i)
