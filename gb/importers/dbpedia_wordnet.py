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


import gb.constants as const
import gb.hypergraph.symbol as sym
import gb.hypergraph.edge as ed
from gb.importers.dbpedia import DBPediaReader


def process_wordnet_instance(instance):
    parts = instance.split('-')
    if parts[0] != 'synset':
        return None, None
    if parts[2] != 'noun':
        return None, None
    name = parts[1].lower()
    number = parts[3]
    if len(number) < 2:
        number = '0%s' % number
    namespace = 'wn.%s.n.%s' % (name, number)
    return name, namespace


class DBPediaWordnetReader(DBPediaReader):
    def __init__(self, hg):
        DBPediaReader.__init__(self, hg)

    def process_entity(self, entity):
        if entity == '<http://dbpedia.org/property/wordnet_type>':
            return const.is_type_of
        if '#' in entity:
            parts = entity.split('#')
        else:
            parts = entity.split('/')
        namespace = parts[-2]
        name = parts[-1]
        if len(namespace) == 0:
            return None
        if namespace[0] == '<':
            namespace = namespace[1:]
        if name[-1] == '>':
            name = name[:-1]
        if namespace == 'resource':
            name, namespace = self.process_resource(name)
        elif namespace == 'instances':
            name, namespace = process_wordnet_instance(name)
        if name is None:
            return None
        name = name.lower()
        return sym.build(name, namespace)

    def process_line(self, line):
        line_str = line.decode()
        if line_str[0] == '#':
            return
        parts = line_str.split(' ')
        edge = (self.process_entity(parts[1]), self.process_entity(parts[0]), self.process_entity(parts[2]))
        if None in edge:
            return

        self.hg.add_belief(const.dbpedia, edge)
        print(ed.edge2str(edge))


def read(hg, filename):
    DBPediaWordnetReader(hg).create_edges(filename)
