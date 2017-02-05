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


import math
import itertools
import plyvel
import gb.hypergraph.edge as ed
from gb.hypergraph.backend import Backend


def nthperm(li, n):
    # TODO: make this more efficient
    indices = [i for i in range(len(li))]
    pos = 0
    pindices = None
    for perm in itertools.permutations(indices):
        if pos >= n:
            pindices = perm
            break
        pos += 1
    return tuple(li[pindices[i]] for i in range(len(li)))


def do_with_edge_permutations(edge, f):
    """Applies the function f to all permutations of the given edge."""
    nperms = math.factorial(len(edge))
    for nperm in range(nperms):
        perm_str = ' '.join([ed.edge2str(e) for e in nthperm(edge, nperm)])
        perm_str = '%s %s' % (perm_str, nperm)
        f(perm_str)


def unpermutate(tokens, nper):
    """Reorder the tokens vector to revert a permutation, specified by nper."""
    n = len(tokens)
    rg = [x for x in range(n)]
    indices = nthperm(rg, nper)

    res = [None] * n
    pos = 0
    for i in indices:
        res[i] = tokens[pos]
        pos += 1

    return tuple(res)


def perm2edge(perm_str):
    """Transforms a permutation string from a database query into an edge."""
    try:
        tokens = ed.split_edge_str(perm_str[1:])
        nper = int(tokens[-1])
        tokens = tokens[:-1]
        tokens = unpermutate(tokens, nper)
        return ed.str2edge(' '.join(tokens))
    except ValueError:
        print(u'VALUE ERROR! perm2edge %s' % perm_str)


def str_plus_1(s):
    """Increment a string by one, regaring lexicographical ordering."""
    last_char = s[-1]
    last_char = chr(ord(last_char) + 1)
    return '%s%s' % (s[:-1], last_char)


def edge_matches_pattern(edge, pattern):
    """Check if an edge matches a pattern."""
    n = len(edge)
    if n != len(pattern):
        return False
    for i in range(n):
        if (pattern[i] is not None) and (pattern[i] != edge[i]):
            return False
    return True


def vertex2key(vertex):
    return ('v%s' % ed.edge2str(vertex)).encode('utf-8')


def encode_metrics(metrics):
    str_list = ['%s:%s' % (key, metrics[key]) for key in metrics]
    return ' '.join(str_list).encode('utf-8')


def decode_metrics(value):
    tokens = value.decode('utf-8').split(' ')
    metrics = {}
    for token in tokens:
        parts = token.split(':')
        metrics[parts[0]] = parts[1]
    return metrics


class LevelDB(Backend):
    """Implements LevelDB hypergraph storage."""

    def __init__(self, params):
        Backend.__init__(self)
        self.dir_path = params['hg']
        # plyvel.repair_db(file_path)
        self.db = plyvel.DB(self.dir_path, create_if_missing=True)

    def close(self):
        self.db.close()

    def add_key(self, vert_key, metrics):
        """Adds the given vertex, given its key."""
        value = encode_metrics(metrics)
        self.db.put(vert_key, value)

    def write_edge_permutation(self, perm):
        perm_key = (u'p%s' % ed.edge2str(perm)).encode('utf-8')
        self.db.put(perm_key, b'x')

    def write_edge_permutations(self, edge):
        """Writes all permutations of the given edge."""
        do_with_edge_permutations(edge, self.write_edge_permutation)

    def remove_edge_permutation(self, perm):
        perm_key = (u'p%s' % ed.edge2str(perm)).encode('utf-8')
        self.db.delete(perm_key)

    def remove_edge_permutations(self, edge):
        """Removes all permutations of the given edge."""
        do_with_edge_permutations(edge, self.remove_edge_permutation)

    def remove_key(self, vert_key):
        """Removes a vertex, given its key."""
        self.db.delete(vert_key)

    def str2perms(self, center_id):
        """Query database for all the edge permutations that contain a given entity, represented as a string."""
        start_str = '%s ' % center_id
        end_str = str_plus_1(start_str)
        start_key = (u'p%s' % start_str).encode('utf-8')
        end_key = (u'p%s' % end_str).encode('utf-8')

        edges = []
        for key, value in self.db.iterator(start=start_key, stop=end_key):
            perm_str = key.decode('utf-8')
            edges.append(perm2edge(perm_str))

        return set(edges)

    def pattern2edges(self, pattern):
        """Return all the edges that match a pattern. A pattern is a collection of entity ids and wildcards (None)."""
        nodes = [node for node in pattern if node is not None]
        start_str = ed.nodes2str(nodes)
        end_str = str_plus_1(start_str)
        start_key = (u'p%s' % start_str).encode('utf-8')
        end_key = (u'p%s' % end_str).encode('utf-8')

        edges = []
        for key, value in self.db.iterator(start=start_key, stop=end_key):
            perm_str = key.decode('utf-8')
            edges.append(perm2edge(perm_str))

        return set([edge for edge in edges if edge_matches_pattern(edge, pattern)])

    def exists_key(self, vertex_key):
        """Checks if the given vertex exists in the hypergraph."""
        return self.db.get(vertex_key) is not None

    def exists(self, vertex):
        """Checks if the given vertex exists in the hypergraph."""
        return self.exists_key(vertex2key(vertex))

    def set_metric_key(self, vert_key, metric, value):
        """Sets the value of a metric by vertex_key."""
        if self.exists_key(vert_key):
            metrics = self.metrics_key(vert_key)
            metrics[metric] = value
            self.add_key(vert_key, metrics)
            return True
        else:
            return False

    def set_metric(self, vertex, metric, value):
        """Sets the value of a metric."""
        vert_key = vertex2key(vertex)
        return self.set_metric_key(vert_key, metric, value)

    def inc_metric_key(self, vert_key, metric):
        """Increments a metric of a vertex."""
        if self.exists_key(vert_key):
            metrics = self.metrics_key(vert_key)
            cur_value = int(metrics[metric])
            metrics[metric] = cur_value + 1
            self.add_key(vert_key, metrics)
            return True
        else:
            return False

    def dec_metric_key(self, vert_key, metric):
        """Decrements a metric of a vertex."""
        if self.exists_key(vert_key):
            metrics = self.metrics_key(vert_key)
            cur_value = int(metrics[metric])
            metrics[metric] = cur_value - 1
            self.add_key(vert_key, metrics)
            return True
        else:
            return False

    def inc_metric(self, vertex, metric):
        """Increments a metric of a vertex."""
        vert_key = vertex2key(vertex)
        return self.inc_metric_key(vert_key, metric)

    def dec_metric(self, vertex, metric):
        """Decrements a metric of a vertex."""
        vert_key = vertex2key(vertex)
        return self.dec_metric_key(vert_key, metric)

    def add(self, edge, timestamp=-1):
        """Adds an edges to the hypergraph if it does not exist yet."""
        edge_key = vertex2key(edge)
        if not self.exists_key(edge_key):
            for vert in edge:
                vert_key = vertex2key(vert)
                if not self.inc_metric_key(vert_key, 'd'):
                    self.add_key(vert_key, {'d': 1, 't': timestamp})
            self.add_key(edge_key, {'d': 0, 't': timestamp})
            self.write_edge_permutations(edge)
        return edge

    def remove(self, edges):
        """Removes an edges from the hypergraph."""
        edge_key = vertex2key(edges)
        if self.exists_key(edge_key):
            for vert in edges:
                vert_key = vertex2key(vert)
                self.dec_metric_key(vert_key, 'd')
            self.remove_edge_permutations(edges)
            self.remove_key(edge_key)

    def star(self, center):
        """Return all the edges that contain a given entity.
        Entity can be atomic or an edge."""
        center_id = center
        if isinstance(center, (list, tuple)):
            center_id = ed.edge2str(center)
        return self.str2perms(center_id)

    def symbols_with_root(self, root):
        """Find all symbols with the given root."""
        start_str = '%s/' % root
        end_str = str_plus_1(start_str)
        start_key = (u'p%s' % start_str).encode('utf-8')
        end_key = (u'p%s' % end_str).encode('utf-8')

        symbs = []
        for key, value in self.db.iterator(start=start_key, stop=end_key):
            perm_str = key.decode('utf-8')
            symb = ed.split_edge_str(perm_str)[0][1:]
            symbs.append(symb)

        return set(symbs)

    def destroy(self):
        """Erase the hypergraph."""
        self.db.close()
        plyvel.destroy_db(self.dir_path)
        self.db = plyvel.DB(self.dir_path, create_if_missing=True)

    def metrics_key(self, vert_key):
        value = self.db.get(vert_key)
        return decode_metrics(value)

    def metrics(self, vertex):
        vert_key = vertex2key(vertex)
        return self.metrics_key(vert_key)

    def get_int_metric(self, vertex, metric, or_else=None):
        vert_key = vertex2key(vertex)
        return self.get_int_metric_key(vert_key, metric, or_else)

    def get_int_metric_key(self, vert_key, metric, or_else=None):
        if self.exists_key(vert_key):
            metrics = self.metrics_key(vert_key)
            if metric in metrics:
                return int(metrics[metric])
            else:
                return or_else
        else:
            return or_else

    def get_float_metric(self, vertex, metric, or_else=None):
        vert_key = vertex2key(vertex)
        return self.get_float_metric_key(vert_key, metric, or_else)

    def get_float_metric_key(self, vert_key, metric, or_else=None):
        if self.exists_key(vert_key):
            metrics = self.metrics_key(vert_key)
            if metric in metrics:
                return float(metrics[metric])
            else:
                return or_else
        else:
            return or_else

    def degree(self, vertex):
        """Returns the degree of a vertex."""
        return self.get_int_metric(vertex, 'd', 0)

    def timestamp(self, vertex):
        """Returns the timestamp of a vertex."""
        return self.get_int_metric(vertex, 't', -1)

    def all(self):
        """Returns a lazy sequence of all the vertices in the hypergraph."""
        start_str = 'v'
        end_str = str_plus_1(start_str)
        start_key = (u'%s' % start_str).encode('utf-8')
        end_key = (u'%s' % end_str).encode('utf-8')

        for key, value in self.db.iterator(start=start_key, stop=end_key):
            vert = ed.str2edge(key.decode('utf-8')[1:])
            yield vert

    def all_metrics(self):
        """Returns a lazy sequence with a tuple for each vertex in the hypergraph.
           The first element of the tuple is the vertex itself,
           the second is a dictionary of metrics values (as strings)."""
        start_str = 'v'
        end_str = str_plus_1(start_str)
        start_key = (u'%s' % start_str).encode('utf-8')
        end_key = (u'%s' % end_str).encode('utf-8')

        for key, value in self.db.iterator(start=start_key, stop=end_key):
            vert = ed.str2edge(key.decode('utf-8')[1:])
            metrics = decode_metrics(value)
            yield (vert, metrics)
