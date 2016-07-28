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


from sqlite import SQLite
from null import Null


class HyperGraph:
    """Hypergraph low-level operations."""

    def __init__(self, params):
        backend = params['backend']
        if backend == 'sqlite':
            self.ops = SQLite(params)
        elif backend == 'null':
            self.ops = Null()
        else:
            raise RuntimeError('Unkown hypergraph backend: %s' % backend)

    def exists(self, vertex):
        """Checks if the given edge exists in the hypergraph."""
        return self.ops.exists(vertex)

    def add(self, edges, timestamp=-1):
        """Adds one or multiple edges to the hypergraph if it does not exist yet.
                Adding multiple edges at the same time might be faster."""
        return self.ops.add(edges, timestamp)

    def remove(self, edges):
        """Removes one or multiple edges from the hypergraph.
           Removing multiple edges at the same time might be faster."""
        self.ops.remove(edges)

    def pattern2edges(self, pattern):
        """Return all the edges that match a pattern.
        A pattern is a collection of entity ids and wildcards (None)."""
        return self.ops.pattern2edges(pattern)

    def star(self, center):
        """Return all the edges that contain a given entity.
        Entity can be atomic or an edge."""
        return self.ops.star(center)

    def symbols_with_root(self, root):
        """Find all symbols with the given root."""
        return self.ops.symbols_with_root(root)

    def destroy(self):
        """Erase the hypergraph."""
        self.ops.destroy()

    def degree(self, vertex):
        """Returns the degree of a vertex."""
        return self.ops.degree(vertex)

    def timestamp(self, vertex):
        """Returns the timestamp of a vertex."""
        return self.ops.timestamp(vertex)

    def batch_exec(self, funs):
        """Evaluates all the functions 'funs', which must be of arity 1.
        The functions are passed hg as a parameters and are evaluated
        as a batch.
        Evaluating function as a batch might be faster."""
        self.ops.batch_exec(funs)

    def f_all(self, f):
        """Returns a lazy sequence resulting from applying f to every
        vertex map (including non-atomic) in the hypergraph.
        A vertex map contains the keys :vertex and :degree."""
        self.ops.f_all(f)

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
