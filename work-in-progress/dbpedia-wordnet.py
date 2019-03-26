import graphbrain.constants as const
from graphbrain.funs import *
from graphbrain.importers.dbpedia import DBPediaReader


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
        return build_symbol(name, namespace)

    def process_line(self, line):
        line_str = line.decode()
        if line_str[0] == '#':
            return
        parts = line_str.split(' ')
        edge = (self.process_entity(parts[1]), self.process_entity(parts[0]), self.process_entity(parts[2]))
        if None in edge:
            return

        self.hg.add_belief(const.dbpedia, edge)
        print(edge2str(edge))


def read(hg, filename):
    DBPediaWordnetReader(hg).create_edges(filename)
