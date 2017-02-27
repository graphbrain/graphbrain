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


from gb.hypergraph.null import Null
from gb.hypergraph.leveldb import LevelDB
import gb.constants as const


class HyperGraph(object):
    """Hypergraph operations."""

    def __init__(self, params):
        backend = params['backend']
        if backend == 'none':
            pass
        elif backend == 'null':
            self.backend = Null()
        elif backend == 'leveldb':
            self.backend = LevelDB(params)
        else:
            raise RuntimeError('Unkown hypergraph backend: %s' % backend)

    def close(self):
        self.backend.close()

    def name(self):
        return self.backend.name()

    def exists(self, vertex):
        """Checks if the given edge exists in the hypergraph."""
        return self.backend.exists(vertex)

    def add(self, edge, timestamp=-1):
        """Adds and edge to the hypergraph if it does not exist yet."""
        return self.backend.add(edge, timestamp)

    def remove(self, edge):
        """Removes and edge from the hypergraph."""
        self.backend.remove(edge)

    def pattern2edges(self, pattern):
        """Return all the edges that match a pattern.
        A pattern is a collection of entity ids and wildcards (None)."""
        return self.backend.pattern2edges(pattern)

    def star(self, center):
        """Return all the edges that contain a given entity.
        Entity can be atomic or an edge."""
        return self.backend.star(center)

    def symbols_with_root(self, root):
        """Find all symbols with the given root."""
        return self.backend.symbols_with_root(root)

    def edges_with_symbols(self, symbols, root=None):
        """Find all edges containing the given symbols, and optionally a given root"""
        return self.backend.edges_with_symbols(symbols, root)

    def destroy(self):
        """Erase the hypergraph."""
        self.backend.destroy()

    def set_metric(self, vertex, metric, value):
        """Sets the value of a metric."""
        return self.backend.set_metric(vertex, metric, value)

    def inc_metric(self, vertex, metric):
        """Increments a metric of a vertex."""
        return self.backend.inc_metric(vertex, metric)

    def dec_metric(self, vertex, metric):
        """Increments a metric of a vertex."""
        return self.backend.dec_metric(vertex, metric)

    def get_int_metric(self, vertex, metric, or_else=None):
        """Returns value of metric as integer value."""
        return self.backend.get_int_metric(vertex, metric, or_else)

    def get_float_metric(self, vertex, metric, or_else=None):
        """Returns value of metric as float value."""
        return self.backend.get_float_metric(vertex, metric, or_else)

    def degree(self, vertex):
        """Returns the degree of a vertex."""
        return self.backend.degree(vertex)

    def timestamp(self, vertex):
        """Returns the timestamp of a vertex."""
        return self.backend.timestamp(vertex)

    def remove_by_pattern(self, pattern):
        """Removes from the hypergraph all edges that match the pattern."""
        edges = self.pattern2edges(pattern)
        for edge in edges:
            self.remove(edge)

    # TODO: this can be optimized
    def ego(self, center, depth):
        if depth > 0:
            edges = self.star(center)
            edges = edges[1:]
            ids = [item for sublist in edges for item in sublist]
            ids = set(ids)
            next_edges = [self.ego(vid, depth - 1) for vid in ids]
            return ids.union(next_edges)

    def add_belief(self, source, edge, timestamp=-1):
        """A belif is a fact with a source. The fact is created as a normal edge
           if it does not exist yet. Another edge is created to assign the fact to
           the source."""
        self.add(edge, timestamp)
        self.add((const.source, edge, source), timestamp)

    def sources(self, edge):
        """Set of sources (nodes) that support a statement (edge)."""
        edges = self.pattern2edges((const.source, edge, None))
        sources = [edge[2] for edge in edges]
        return set(sources)

    def remove_belief(self, source, edge):
        """A belif is a fact with a source. The link from the source to the fact
           is removed. If no more sources support the fact, then the fact is also
           removed."""
        self.remove((const.source, edge, source))
        if len(self.sources(edge)) == 0:
            self.remove(edge)

    def all(self):
        """Returns a lazy sequence of all the vertices in the hypergraph."""
        return self.backend.all()

    def all_metrics(self):
        """Returns a lazy sequence with a tuple for each vertex in the hypergraph.
           The first element of the tuple is the vertex itself,
           the second is a dictionary of metrics values (as strings)."""
        return self.backend.all_metrics()

    def symbol_count(self):
        """Total number of symbols in the hypergraph"""
        return self.backend.symbol_count()

    def edge_count(self):
        """Total number of edge in the hypergraph"""
        return self.backend.edge_count()

    def total_degree(self):
        """Total degree of the hypergraph"""
        return self.backend.total_degree()
