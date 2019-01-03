import json
from graphbrain.hypergraph import HyperGraph
from graphbrain.funs import *


EXCLUDE_RELS = ['are_synonyms/gb', 'src/gb', 'have_same_lemma/gb']


def exclude(edge):
    if is_edge(edge):
        rel = edge[0]
        if is_edge(rel):
            return False
        return rel in EXCLUDE_RELS
    else:
        return True


def write_edge_data(edge_data, file_path):
    f = open(file_path, 'w')
    for e in edge_data:
        f.write('%s\n' % json.dumps(e, separators=(',', ':')))
    f.close()


class Filter(object):
    def __init__(self, hg):
        self.hg = hg

    def write_edges(self, file_path):
        pass


class AllFilter(Filter):
    def __init__(self, hg):
        Filter.__init__(self, hg)

    def all_edges(self):
        edges = self.hg.all()

        filtered_edges = []
        for edge in edges:
            if not exclude(edge):
                filtered_edges.append(edge)

        result = []
        for e in filtered_edges:
            edge_data = {'edge': edge2str(e),
                         'text': self.hg.get_str_attribute(e, 'text')}
            result.append(edge_data)
        return result

    # override
    def write_edges(self, file_path):
        edge_data = self.all_edges()
        write_edge_data(edge_data, file_path)


class TermFilter(Filter):
    def __init__(self, hg, term):
        Filter.__init__(self, hg)
        self.term = term

    def edges_with_term(self):
        edges = self.hg.all()

        filtered_edges = []
        for edge in edges:
            if not exclude(edge):
                if edge_contains(without_namespaces(edge), self.term):
                    print(edge)
                    filtered_edges.append(edge)

        result = []
        for e in filtered_edges:
            edge_data = {'edge': edge2str(e),
                         'text': self.hg.get_str_attribute(e, 'text')}
            result.append(edge_data)
        return result

    # override
    def write_edges(self, file_path):
        edge_data = self.edges_with_term()
        write_edge_data(edge_data, file_path)


class RelFilter(Filter):
    def __init__(self, hg, term):
        Filter.__init__(self, hg)
        self.term = term

    def rel_has_term(self, edge):
        if is_edge(edge) and len(edge) > 2:
            if len(edge) > 3 or is_edge(edge[2]):
                rel = edge[0]
                if is_edge(rel):
                    return self.term in rel
                else:
                    return rel == self.term
        return False

    def edges_with_rel(self):
        edges = self.hg.all()

        filtered_edges = []
        for edge in edges:
            if not exclude(edge):
                if self.rel_has_term(edge):
                    print(edge)
                    filtered_edges.append(edge)

        result = []
        for e in filtered_edges:
            edge_data = {'edge': edge2str(e),
                         'text': self.hg.get_str_attribute(e, 'text')}
            result.append(edge_data)
        return result

    # override
    def write_edges(self, file_path):
        edge_data = self.edges_with_rel()
        write_edge_data(edge_data, file_path)


if __name__ == '__main__':
    hgr = HyperGraph({'backend': 'leveldb', 'hg': 'reddit-politics.hg'})

    filt = RelFilter(hgr, 'says')
    filt.write_edges('says.json')
