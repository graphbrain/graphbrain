from collections import Counter
from graphbrain import hedge
from graphbrain.hyperedge import edge_matches_pattern


argrole_order = {
    'm': -1,
    's': 0,
    'p': 1,
    'a': 2,
    'c': 3,
    'o': 4,
    'i': 5,
    't': 6,
    'j': 7,
    'x': 8,
    'r': 9,
    '?': 10
}


def normalize_edge(edge):
    if edge.is_atom():
        return edge
    conn = edge[0]
    ar = conn.argroles()
    if ar != '':
        roles_edges = zip(ar, edge[1:])
        roles_edges = sorted(roles_edges,
                             key=lambda role_edge: argrole_order[role_edge[0]])
        ar = ''.join([role_edge[0] for role_edge in roles_edges])
        pred = conn.atom()
        new_pred = hedge('{}/{}.{}'.format(pred.root(), pred.type(), ar))
        conn = conn.replace_atom(pred, new_pred)
        edge = hedge([conn] + list(role_edge[1] for role_edge in roles_edges))
    return hedge([normalize_edge(subedge) for subedge in edge])


def edge2pattern(edge, root=False, subtype=False):
    if root and edge.is_atom():
        root_str = edge.root()
    else:
        root_str = '*'
    if subtype:
        et = edge.type()
    else:
        et = edge.type()[0]
    pattern = '{}/{}'.format(root_str, et)
    ar = edge.argroles()
    if ar == '':
        return hedge(pattern)
    else:
        return hedge('{}.{}'.format(pattern, ar))


class PatternCounter:
    def __init__(self,
                 depth=2,
                 count_subedges=True,
                 expansions={'*'},
                 match_roots=set(),
                 match_subtypes={}):
        self.patterns = Counter()
        self.depth = depth
        self.count_subedges = count_subedges
        self.expansions = expansions
        self.match_roots = match_roots
        self.match_subtypes = match_subtypes

    def _matches_expansions(self, edge):
        for expansion in self.expansions:
            if edge_matches_pattern(edge, expansion):
                return True
        return False

    def _list2patterns(self, ledge, depth=1):
        if depth > self.depth:
            return []
        first = ledge[0]
        ft = first.type()[0]
        subtype = ft in self.match_subtypes
        hpats = [edge2pattern(first, subtype=subtype)]
        if first.is_atom() and ft in self.match_roots:
            hpats.append(edge2pattern(first, root=True, subtype=subtype))
        if not first.is_atom() and self._matches_expansions(first):
            hpats += self._list2patterns(list(first), depth + 1)
        if len(ledge) == 1:
            patterns = [[hpat] for hpat in hpats]
        else:
            patterns = []
            for pattern in self._list2patterns(ledge[1:], depth=depth):
                for hpat in hpats:
                    patterns.append([hpat] + pattern)
        return patterns

    def _edge2patterns(self, edge):
        return list(hedge(pattern)
                    for pattern
                    in self._list2patterns(list(normalize_edge(edge))))

    def count(self, edge):
        if not edge.is_atom():
            if self._matches_expansions(edge):
                for pattern in self._edge2patterns(edge):
                    self.patterns[hedge(pattern)] += 1
                if self.count_subedges:
                    for subedge in edge:
                        self.count(subedge)
