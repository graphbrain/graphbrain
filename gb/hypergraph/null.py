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


from backend import Backend


class Null(Backend):
    """Hypergraph low-level operations."""

    def __init__(self):
        Backend.__init__(self)

    def exists(self, vertex):
        """Checks if the given edge exists in the hypergraph."""
        return False

    def add(self, edges, timestamp=-1):
        """Adds one or multiple edges to the hypergraph if it does not exist yet.
                Adding multiple edges at the same time might be faster."""
        return edges

    def remove(self, edges):
        """Removes one or multiple edges from the hypergraph.
           Removing multiple edges at the same time might be faster."""
        pass

    def pattern2edges(self, pattern):
        """Return all the edges that match a pattern.
        A pattern is a collection of entity ids and wildcards (None)."""
        return []

    def star(self, center):
        """Return all the edges that contain a given entity.
        Entity can be atomic or an edge."""
        return []

    def symbols_with_root(self, root):
        """Find all symbols with the given root."""
        return []

    def destroy(self):
        """Erase the hypergraph."""
        pass

    def degree(self, vertex):
        """Returns the degree of a vertex."""
        return 0

    def timestamp(self, vertex):
        """Returns the timestamp of a vertex."""
        return -1

    def batch_exec(self, funs):
        """Evaluates all the functions 'funs', which must be of arity 1.
        The functions are passed hg as a parameters and are evaluated
        as a batch.
        Evaluating function as a batch might be faster."""
        pass

    def f_all(self, f):
        """Returns a lazy sequence resulting from applying f to every
        vertex map (including non-atomic) in the hypergraph.
        A vertex map contains the keys :vertex and :degree."""
        return []
