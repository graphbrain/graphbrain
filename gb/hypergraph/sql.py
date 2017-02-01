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


def cur2edges(cur):
    """Transforms a cursor from a database query into a set of edges."""
    edges = []
    for row in cur:
        res = row[0]
        tokens = ed.split_edge_str(res)
        nper = int(tokens[-1])
        tokens = tokens[:-1]
        tokens = unpermutate(tokens, nper)
        edge = ed.str2edge(' '.join(tokens))
        edges.append(edge)
    return set(edges)


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


class SQL(Backend):
    """Implements generic SQL hypergraph storage."""

    def __init__(self, conn, ph='?'):
        Backend.__init__(self)
        self.conn = conn
        self.create_tables()
        self.cur = None
        self.ph = ph

    def create_tables(self):
        raise NotImplementedError()

    def open_cursor(self, local=True):
        if local:
            if self.cur is None:
                return self.conn.cursor()
            else:
                return self.cur
        else:
            if self.cur is None:
                self.cur = self.conn.cursor()
            return self.cur

    def close_cursor(self, cur, local=True, commit=False):
        if local:
            if self.cur is None:
                cur.close()
                if commit:
                    self.conn.commit()
        else:
            if commit:
                self.conn.commit()
            self.cur.close()
            self.cur = None

    def update_or_insert(self, table, row, vid):
        """Updates columns or inserts a new row in the vertices table"""
        try:
            cur = self.open_cursor()

            row_str = ','.join(['%s=%s' % (k, self.ph) for k in row.keys()])
            values = [v for v in row.values()]
            values.append(vid)
            cur.execute('UPDATE %s SET %s WHERE id=%s' % (table, row_str, self.ph), values)
            if cur.rowcount == 0:
                values = values[:-1]
                key_str = ','.join([k for k in row.keys()])
                placeholder_str = ','.join([self.ph] * len(row.keys()))
                cur.execute('INSERT INTO %s (%s) values (%s)' % (table, key_str, placeholder_str), values)

            self.close_cursor(cur, local=True, commit=True)
        except Exception:
            print('>>> %s /// %s ' % (vid, row))

    def add_str(self, vert_str, degree, timestamp):
        """Adds the given vertex, represented as a string."""
        self.update_or_insert('vertices',
                              {'id': vert_str, 'degree': degree, 'timestamp': timestamp},
                              vert_str)

    def write_edge_permutation(self, perm):
        eid = ed.edge2str(perm)
        self.update_or_insert('perms', {'id': eid}, eid)

    def write_edge_permutations(self, edge):
        """Writes all permutations of the given edge."""
        do_with_edge_permutations(edge, self.write_edge_permutation)

    def remove_edge_permutation(self, perm):
        eid = ed.edge2str(perm)
        cur = self.conn.cursor()
        cur.execute('DELETE FROM perms WHERE id=%s' % (self.ph,), (eid,))
        self.conn.commit()
        cur.close()

    def remove_edge_permutations(self, edge):
        """Removes all permutations of the given edge."""
        do_with_edge_permutations(edge, self.remove_edge_permutation)

    def remove_str(self, vert_str):
        """Removes the given vertex, represented as a string."""
        cur = self.open_cursor()
        cur.execute('DELETE FROM vertices WHERE id=%s' % (self.ph,), (vert_str,))
        self.close_cursor(cur, local=True, commit=True)

    def str2perms(self, center_id):
        """Query database for all the edge permutations that contain a given entity,
        represented as a string."""
        start_str = '%s ' % center_id
        end_str = str_plus_1(start_str)

        cur = self.open_cursor()
        cur.execute('SELECT id FROM perms WHERE id>=%s AND id<%s' % (self.ph, self.ph), (start_str, end_str))
        edges = cur2edges(cur)
        self.close_cursor(cur, local=True, commit=False)
        return set(edges)

    def pattern2edges(self, pattern):
        """Return all the edges that match a pattern.
        A pattern is a collection of entity ids and wildcards (None)."""
        nodes = [node for node in pattern if node is not None]
        start_str = ed.nodes2str(nodes)
        end_str = str_plus_1(start_str)
        cur = self.open_cursor()
        cur.execute('SELECT id FROM perms WHERE id>=%s AND id<%s' % (self.ph, self.ph), (start_str, end_str))
        edges = cur2edges(cur)
        self.close_cursor(cur, local=True, commit=False)
        return set([edge for edge in edges if edge_matches_pattern(edge, pattern)])

    def exists(self, vertex):
        """Checks if the given vertex exists in the hypergraph."""
        return len(self.pattern2edges(vertex)) > 0

    def inc_degree(self, vert_str):
        """Increments the degree of a vertex."""
        cur = self.open_cursor()
        cur.execute('UPDATE vertices SET degree=degree+1 WHERE id=%s' % (self.ph,), (vert_str,))
        res = cur.rowcount > 0
        self.close_cursor(cur, local=True, commit=True)
        return res

    def dec_degree(self, vert_str):
        """Decrements the degree of a vertex."""
        cur = self.open_cursor()
        cur.execute('UPDATE vertices SET degree=degree-1 WHERE id=%s' % (self.ph,), (vert_str,))
        res = cur.rowcount > 0
        self.close_cursor(cur, local=True, commit=True)
        return res

    def add_raw(self, edge, timestamp):
        """Auxiliary function for add to call from inside a transaction."""
        if not self.exists(edge):
            for vert in edge:
                vert_str = ed.edge2str(vert)
                if not self.inc_degree(vert_str):
                    self.add_str(vert_str, 1, timestamp)
            self.add_str(ed.edge2str(edge), 0, timestamp)
            self.write_edge_permutations(edge)
        return edge

    def add(self, edges, timestamp=-1):
        """Adds one or multiple edges to the hypergraph if it does not exist yet.
        Adding multiple edges at the same time might be faster."""
        if isinstance(edges[0], (list, tuple)):
            cur = self.open_cursor(local=False)
            for edge in edges:
                self.add_raw(edge, timestamp)
            self.close_cursor(cur, local=False, commit=True)
        else:
            self.add_raw(edges, timestamp)
        return edges

    def remove_raw(self, edge):
        """Auxiliary function for remove! to call from inside a transaction."""
        if self.exists(edge):
            for vert in edge:
                self.dec_degree(ed.edge2str(vert))
            self.remove_edge_permutations(edge)
            self.remove_str(ed.edge2str(edge))

    def remove(self, edges):
        """Removes one or multiple edges from the hypergraph
        Removing multiple edges at the same time might be faster."""
        if isinstance(edges[0], (list, tuple)):
            # TODO: use transaction?
            for edge in edges:
                self.remove_raw(edge)
        else:
            self.remove_raw(edges)

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
        cur = self.open_cursor()
        cur.execute('SELECT id FROM perms WHERE id>=%s AND id<%s' % (self.ph, self.ph), (start_str, end_str))
        symbs = []
        for row in cur:
            res = row[0]
            symb = ed.split_edge_str(res)[0]
            symbs.append(symb)
        self.close_cursor(cur, local=True, commit=False)
        return set(symbs)

    def destroy(self):
        """Erase the hypergraph."""
        cur = self.open_cursor()
        cur.execute('DELETE FROM vertices')
        cur.execute('DELETE FROM perms')
        self.close_cursor(cur, local=True, commit=True)

    def degree(self, vertex):
        """Returns the degree of a vertex."""
        vert_str = ed.edge2str(vertex)
        cur = self.open_cursor()
        cur.execute('SELECT degree FROM vertices WHERE id=%s' % (self.ph,), (vert_str,))
        for row in cur:
            deg = row[0]
            cur.close()
            return deg
        self.close_cursor(cur, local=True, commit=False)
        return 0

    def timestamp(self, vertex):
        """Returns the timestamp of a vertex."""
        vert_str = ed.edge2str(vertex)
        cur = self.open_cursor()
        cur.execute('SELECT timestamp FROM vertices WHERE id=%s' % (self.ph,), (vert_str,))
        for row in cur:
            ts = row[0]
            cur.close()
            return ts
        self.close_cursor(cur, local=True, commit=False)
        return -1

    def batch_exec(self, funs):
        """Auxiliary function to implement ops.batch_exec in SQL environments."""
        cur = self.open_cursor(local=False)
        for f in funs:
            f(self)
        self.close_cursor(cur, local=False, commit=True)

    def f_all(self, f):
        """Returns a lazy sequence resulting from applying f to every
           vertex map (including non-atomic) in the hypergraph.
           A vertex map contains the keys vertex and degree."""
        cur = self.open_cursor()
        cur.execute('SELECT id, degree FROM vertices')
        for row in cur:
            vmap = {'vertex': ed.str2edge(row[0]), 'degree': row[1]}
            yield f(vmap)
        self.close_cursor(cur, local=True, commit=False)
