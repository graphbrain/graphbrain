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


import constants as const
import hypergraph


class BatchHyperGraph(hypergraph.HyperGraph):
    """Hypergraph operations."""

    def __init__(self, hg, buffer_max=1000):
        hypergraph.HyperGraph.__init__(self, {'backend': 'none'})
        self.hg = hg
        self.backend = hg.backend
        self.buffer = []
        self.buffer_max = buffer_max

    def flush(self, timestamp=-1):
        self.backend.add(self.buffer, timestamp)
        self.buffer = []

    def add(self, edges, timestamp=-1):
        """Adds one or multiple edges to the hypergraph if it does not exist yet.
                Adding multiple edges at the same time might be faster."""
        if isinstance(edges[0], (list, tuple)):
            self.buffer += edges
        else:
            self.buffer.append(edges)

        if len(self.buffer) > self.buffer_max:
            self.flush(timestamp)

        return edges
