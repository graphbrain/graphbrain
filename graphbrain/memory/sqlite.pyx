import json

from sqlite3 import connect

from graphbrain.hyperedge import edges2str
from graphbrain.hyperedge import hedge
from graphbrain.hyperedge import match_pattern
from graphbrain.hyperedge import split_edge_str
from graphbrain.hypergraph import Hypergraph
from graphbrain.memory.permutations import do_with_edge_permutations
from graphbrain.memory.permutations import first_permutation
from graphbrain.memory.permutations import perm2edge
from graphbrain.memory.permutations import str_plus_1


def _edge2key(edge):
    return edge.to_str()


def _encode_attributes(attributes):
    return json.dumps(attributes,
                      ensure_ascii=False,
                      check_circular=False,
                      separators=(',', ':'))


def _decode_attributes(value):
    return json.loads(value)


class SQLite(Hypergraph):
    """Implements SQLite hypergraph storage."""

    def __init__(self, locator_string):
        self.locator_string = locator_string
        self.conn = connect(self.locator_string, isolation_level=None)
        self.cur = None

        # self.conn.execute('PRAGMA synchronous = OFF')
        # self.conn.execute('PRAGMA journal_mode = MEMORY')

        self.conn.execute(
            'CREATE TABLE IF NOT EXISTS v (key TEXT PRIMARY KEY, value TEXT)')
        self.conn.execute(
            'CREATE TABLE IF NOT EXISTS p (key TEXT PRIMARY KEY)')

    # ============================================
    # Implementation of abstract interface methods
    # ============================================

    def close(self):
        if self.conn:
            self.conn.close()
            self.conn = None

    def name(self):
        return self.locator_string

    def destroy(self):
        cur = self.conn.cursor()
        cur.execute('DELETE FROM v')
        cur.execute('DELETE FROM p')

    def all(self):
        cur = self.conn.cursor()
        for key, _ in cur.execute('SELECT * FROM v'):
            edge = hedge(key)
            if edge is not None:
                yield edge

    def all_attributes(self):
        cur = self.conn.cursor()
        for key, value in cur.execute('SELECT * FROM v'):
            edge = hedge(key)
            if edge is not None:
                attributes = _decode_attributes(value)
                yield (edge, attributes)

    def add_with_attributes(self, edge, attributes):
        self._begin_transaction()
        key = _edge2key(edge)
        self._add_key(key, attributes)
        if not edge.is_atom():
            self._write_edge_permutations(edge)
        self._end_transaction()
        return edge

    # ==========================================
    # Implementation of private abstract methods
    # ==========================================

    def _exists(self, edge):
        return self._exists_key(_edge2key(edge))

    def _add(self, edge, primary):
        self._begin_transaction()
        key = _edge2key(edge)
        if not self._exists_key(key):
            if primary:
                self._add_key(key, {'p': 1, 'd': 0, 'dd': 0})
                self._inc_degrees(edge)
            else:
                self._add_key(key, {'p': 0, 'd': 0, 'dd': 0})
            if not edge.is_atom():
                self._write_edge_permutations(edge)
        # if an edge is to be added as primary, but it already exists as
        # non-primary, then make it primary and update the degrees
        elif primary and not self._is_primary(edge):
            self._set_attribute(edge, 'p', 1)
            self._inc_degrees(edge)
        self._end_transaction()
        return edge

    def _remove(self, edge, deep):
        self._begin_transaction()
        primary = self.is_primary(edge)

        if not edge.is_atom():
            if deep:
                for child in edge:
                    self._remove(child, deep=True)
            else:
                if primary:
                    self._dec_degrees(edge)

        key = _edge2key(edge)
        if self._exists_key(key):
            if not edge.is_atom():
                self._remove_edge_permutations(edge)
            self._remove_key(key)
        self._end_transaction()

    def _is_primary(self, edge):
        return self._get_int_attribute(edge, 'p') == 1

    def _set_primary(self, edge, value):
        self._set_attribute(edge, 'p', 1 if value else 0)

    def _search(self, pattern, strict=True):
        for result in self._match(pattern, strict=strict):
            yield result[0]

    def _match_structure(self, pattern, strict):
        if not strict or pattern.is_full_pattern():
            for edge in self.all():
                yield edge
        else:
            nodes = []
            positions = []
            for i, node in enumerate(pattern):
                if not node.is_pattern():
                    nodes.append(node)
                    positions.append(i)
            start_str = edges2str(nodes)
            end_str = str_plus_1(start_str)

            cur = self.conn.cursor()
            for row in cur.execute(
                    'SELECT * FROM p WHERE key >= ? AND key < ?',
                    (start_str, end_str)):
                key = row[0]
                tokens = split_edge_str(key)
                nper = int(tokens[-1])

                if nper == first_permutation(len(tokens) - 1, positions):
                    yield perm2edge(key)

    def _match(self, pattern, strict=True, curvars={}):
        for edge in self._match_structure(pattern, strict):
            results = match_pattern(edge, pattern, curvars=curvars)
            if len(results) > 0:
                yield (edge, results)

    def _star(self, center, limit=None):
        center_str = center.to_str()
        start_str = ''.join((center_str, ' '))
        end_str = str_plus_1(start_str)

        count = 0
        cur = self.conn.cursor()
        for row in cur.execute(
                'SELECT * FROM p WHERE key >= ? AND key < ?',
                (start_str, end_str)):
            key = row[0]
            if limit and count >= limit:
                break
            edge = perm2edge(key)
            if edge:
                position = edge.index(center)
                nper = int(split_edge_str(key)[-1])
                if nper == first_permutation(len(edge), (position,)):
                    count += 1
                    yield(edge)

    def _atoms_with_root(self, root):
        start_str = ''.join((root, '/'))
        end_str = str_plus_1(start_str)

        cur = self.conn.cursor()
        for key, value in cur.execute(
                'SELECT * FROM v WHERE key >= ? AND key < ?',
                (start_str, end_str)):
            symb = hedge(key)
            yield(symb)

    def _edges_with_edges(self, edges, root):
        start_str = ' '.join([edge.to_str() for edge in edges])
        if root:
            start_str = ''.join((start_str, ' ', root, '/'))
        end_str = str_plus_1(start_str)

        cur = self.conn.cursor()
        for row in cur.execute(
                'SELECT * FROM p WHERE key >= ? AND key < ?',
                (start_str, end_str)):
            key = row[0]
            edge = perm2edge(key)
            if edge:
                if root is None:
                    if all([item in edge for item in edges]):
                        positions = [edge.index(item) for item in edges]
                        nper = int(split_edge_str(key)[-1])
                        if nper == first_permutation(len(edge), positions):
                            yield(edge)
                else:
                    # TODO: remove redundant results when a root is present
                    yield(edge)

    def _set_attribute(self, edge, attribute, value):
        self._begin_transaction()
        key = _edge2key(edge)
        result = self._set_attribute_key(key, attribute, value)
        self._end_transaction()
        return result

    def _inc_attribute(self, edge, attribute):
        self._begin_transaction()
        key = _edge2key(edge)
        result = self._inc_attribute_key(key, attribute)
        self._end_transaction()
        return result

    def _dec_attribute(self, edge, attribute):
        self._begin_transaction()
        key = _edge2key(edge)
        result = self._dec_attribute_key(key, attribute)
        self._end_transaction()
        return result

    def _get_str_attribute(self, edge, attribute, or_else=None):
        key = _edge2key(edge)
        return self._get_str_attribute_key(key, attribute, or_else)

    def _get_int_attribute(self, edge, attribute, or_else=None):
        key = _edge2key(edge)
        return self._get_int_attribute_key(key, attribute, or_else)

    def _get_float_attribute(self, edge, attribute, or_else=None):
        key = _edge2key(edge)
        return self._get_float_attribute_key(key, attribute, or_else)

    def _degree(self, edge):
        return self.get_int_attribute(edge, 'd', 0)

    def _deep_degree(self, edge):
        return self.get_int_attribute(edge, 'dd', 0)

    # =====================
    # Local private methods
    # =====================

    def _begin_transaction(self):
        self.cur = self.conn.cursor()
        self.cur.execute('BEGIN TRANSACTION')

    def _end_transaction(self):
        self.conn.commit()
        self.cur = None

    def _add_key(self, key, attributes):
        """Adds the given edge, given its key."""
        value = _encode_attributes(attributes)
        self.cur.execute(
            'INSERT OR REPLACE INTO v (key, value) VALUES(?, ?)',
            (key, value))

    def _write_edge_permutation(self, perm):
        """Writes a given permutation."""
        self.cur.execute('INSERT OR IGNORE INTO p (key) VALUES(?)', (perm,))

    def _write_edge_permutations(self, edge):
        """Writes all permutations of the edge."""
        do_with_edge_permutations(edge, self._write_edge_permutation)

    def _remove_edge_permutation(self, perm):
        """Removes a given permutation."""
        self.cur.execute('DELETE FROM p WHERE key = ?', (perm,))

    def _remove_edge_permutations(self, edge):
        """Removes all permutations of the edge."""
        do_with_edge_permutations(edge, self._remove_edge_permutation)

    def _remove_key(self, key):
        """Removes an edge, given its key."""
        self.cur.execute('DELETE FROM v WHERE key = ?', (key,))

    def _exists_key(self, key):
        """Checks if the given edge exists."""
        cur = self.conn.cursor()
        for key, _ in cur.execute('SELECT * FROM v WHERE key = ?', (key, )):
            return True
        return False

    def _set_attribute_key(self, key, attribute, value):
        """Sets the value of an attribute by key."""
        exists = self._exists_key(key)
        if exists:
            exists = True
            attributes = self._attribute_key(key)
            attributes[attribute] = value
        else:
            attributes = {'p': 0, 'd': 0, 'dd': 0}
            attributes[attribute] = value
        self._add_key(key, attributes)
        return exists

    def _inc_attribute_key(self, key, attribute):
        """Increments an attribute of an edge."""
        if self._exists_key(key):
            attributes = self._attribute_key(key)
            if attribute in attributes:
                cur_value = int(attributes[attribute])
                attributes[attribute] = cur_value + 1
            else:
                attributes[attribute] = 1
            self._add_key(key, attributes)
            return True
        else:
            return False

    def _dec_attribute_key(self, key, attribute):
        """Decrements an attribute of an edge."""
        if self._exists_key(key):
            attributes = self._attribute_key(key)
            cur_value = int(attributes[attribute])
            attributes[attribute] = cur_value - 1
            self._add_key(key, attributes)
            return True
        else:
            return False

    def _attribute_key(self, key):
        cur = self.conn.cursor()
        for key, value in cur.execute('SELECT * FROM v WHERE key = ?', (key,)):
            return _decode_attributes(value)
        return None

    def _get_str_attribute_key(self, key, attribute, or_else=None):
        if self._exists_key(key):
            attributes = self._attribute_key(key)
            if attribute in attributes:
                return attributes[attribute]
            else:
                return or_else
        else:
            return or_else

    def _get_int_attribute_key(self, key, attribute, or_else=None):
        if self._exists_key(key):
            attributes = self._attribute_key(key)
            if attribute in attributes:
                return int(attributes[attribute])
            else:
                return or_else
        else:
            return or_else

    def _get_float_attribute_key(self, key, attribute, or_else=None):
        if self._exists_key(key):
            attributes = self._attribute_key(key)
            if attribute in attributes:
                return float(attributes[attribute])
            else:
                return or_else
        else:
            return or_else

    def _inc_degrees(self, edge, depth=0):
        if depth > 0:
            key = _edge2key(edge)
            if not self._exists_key(key):
                d = 1 if depth == 1 else 0
                self._add_key(key, {'p': 0, 'd': d, 'dd': 1})
            else:
                if depth == 1:
                    self._inc_attribute_key(key, 'd')
                self._inc_attribute_key(key, 'dd')
        if not edge.is_atom():
            for child in edge:
                self._inc_degrees(child, depth + 1)

    def _dec_degrees(self, edge, depth=0):
        if depth > 0:
            key = _edge2key(edge)
            if depth == 1:
                self._dec_attribute_key(key, 'd')
            self._dec_attribute_key(key, 'dd')
        if not edge.is_atom():
            for child in edge:
                self._dec_degrees(child, depth + 1)
