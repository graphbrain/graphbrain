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


from null import Null
from sqlite import SQLite
from mysql import MySQL
import constants as const


class HyperGraph:
    """Hypergraph operations."""

    def __init__(self, params):
        backend = params['backend']
        if backend == 'none':
            pass
        elif backend == 'null':
            self.backend = Null()
        elif backend == 'sqlite':
            self.backend = SQLite(params)
        elif backend == 'mysql':
            self.backend = MySQL(params)
        else:
            raise RuntimeError('Unkown hypergraph backend: %s' % backend)

    def exists(self, vertex):
        """Checks if the given edge exists in the hypergraph."""
        return self.backend.exists(vertex)

    def add(self, edges, timestamp=-1):
        """Adds one or multiple edges to the hypergraph if it does not exist yet.
                Adding multiple edges at the same time might be faster."""
        return self.backend.add(edges, timestamp)

    def remove(self, edges):
        """Removes one or multiple edges from the hypergraph.
           Removing multiple edges at the same time might be faster."""
        self.backend.remove(edges)

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

    def destroy(self):
        """Erase the hypergraph."""
        self.backend.destroy()

    def degree(self, vertex):
        """Returns the degree of a vertex."""
        return self.backend.degree(vertex)

    def timestamp(self, vertex):
        """Returns the timestamp of a vertex."""
        return self.backend.timestamp(vertex)

    def batch_exec(self, funs):
        """Evaluates all the functions 'funs', which must be of arity 1.
        The functions are passed hg as a parameters and are evaluated
        as a batch.
        Evaluating function as a batch might be faster."""
        self.backend.batch_exec(funs)

    def f_all(self, f):
        """Returns a lazy sequence resulting from applying f to every
        vertex map (including non-atomic) in the hypergraph.
        A vertex map contains the keys :vertex and :degree."""
        return self.backend.f_all(f)

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

    def add_belief(self, source, edges, timestamp=-1):
        """A belif is a fact with a source. The fact is created as a normal edge
           if it does not exist yet. Another edge is created to assign the fact to
           the source.

           Multiple edges can be provided, in which case all the beliefs will be
           inserted at once. This may be faster."""
        if isinstance(edges[0], (list, tuple)):
            new_edges = []
            for edge in edges:
                new_edges.append(edge)
                new_edges.append((const.source, edge, source))
            self.add(new_edges, timestamp)
        else:
            self.add((edges, (const.source, edges, source)), timestamp)

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
