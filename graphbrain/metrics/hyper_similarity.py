from graphbrain.funs import *


def valid_symbol(s):
    if is_edge(s):
        return True
    if is_root(s):
        return False
    if symbol_namespace(s) == 'gb':
        return False
    if s[0] == '+':
        return False
    if symbol_namespace(s)[:3] != 'nlp':
        return False
    if symbol_namespace(s)[-3:] == 'adp':
        return False
    if symbol_namespace(s)[-3:] == 'det':
        return False
    if symbol_namespace(s)[-4:] == 'verb':
        return False
    if symbol_namespace(s)[-4:] == 'pron':
        return False
    return True


class HyperSimilarity:
    def __init__(self, hg):
        self.hg = hg
        self.cs_cache = {}

    def sphere(self, edge):
        edges = set(self.hg.star(edge))
        for e in edges:
            if self.hg.degree(e) > 0:
                edges = edges.union(self.sphere(e))
        return edges

    def concept_sphere(self, edge):
        if edge in self.cs_cache:
            return self.cs_cache[edge]

        concepts = set()
        for item in self.sphere(edge):
            concepts = concepts.union(subedges(item))

        self.cs_cache[edge] = concepts

        return concepts

    def setweight(self, cs):
        weight = 0.
        for item in cs:
            deg = self.hg.degree(item)
            if deg > 0:
                weight += 1. / float(deg)
        return weight

    def setsimilarity_(self, cs1, cs2):
        csi = [v for v in cs1.intersection(cs2) if valid_symbol(v)]
        csu = [v for v in cs1.union(cs2) if valid_symbol(v)]
        for v in csi:
            deg = float(self.hg.degree(v))
            print('%s %s' % (v, deg))
        w_i = self.setweight(csi)
        w_u = self.setweight(csu)
        if w_u == 0.:
            return 0.
        return w_i / w_u

    def setsimilarity(self, cs1, cs2):
        csi = [v for v in cs1.intersection(cs2) if valid_symbol(v)]
        simil = 0.
        for v in csi:
            deg = float(self.hg.degree(v))
            # print('%s %s' % (v, deg))
            if deg > 0.:
                simil += 1. / deg
        return simil

    def similarity(self, edge1, edge2):
        cs1 = self.concept_sphere(edge1)
        cs2 = self.concept_sphere(edge2)
        return self.setsimilarity(cs1, cs2)

    def nsimilarity(self, edges1, edges2):
        cs1 = set()
        for edge in edges1:
            cs1 = cs1.union(self.concept_sphere(edge))
        cs2 = set()
        for edge in edges2:
            cs2 = cs2.union(self.concept_sphere(edge))

        return self.setsimilarity(cs1, cs2)

    def synonym_similarity(self, meronomy, syn_id_1, syn_id_2):
        return self.nsimilarity(meronomy.synonym_full_edges(syn_id_1), meronomy.synonym_full_edges(syn_id_2))
