import json

from sqlite3 import connect

from graphbrain.hyperedge import edges2str
from graphbrain.hyperedge import hedge
from graphbrain.hyperedge import split_edge_str
from graphbrain.memory.keyvalue import KeyValue
from graphbrain.memory.permutations import first_permutation
from graphbrain.memory.permutations import perm2edge
from graphbrain.memory.permutations import str_plus_1
from graphbrain.patterns import is_pattern, is_full_pattern


def _encode_attributes(attributes):
    return json.dumps(attributes,
                      ensure_ascii=False,
                      check_circular=False,
                      separators=(',', ':'))


def _decode_attributes(value):
    return json.loads(value)


class SQLite(KeyValue):
    """Implements SQLite hypergraph storage."""

    def __init__(self, locator_string):
        super().__init__(locator_string)

        self.conn = connect(self.locator_string, isolation_level=None)
        self.cur = None

        # self.conn.execute('PRAGMA synchronous = OFF')
        # self.conn.execute('PRAGMA journal_mode = MEMORY')

        self.conn.execute('CREATE TABLE IF NOT EXISTS v (key TEXT PRIMARY KEY, value TEXT)')
        self.conn.execute('CREATE TABLE IF NOT EXISTS p (key TEXT PRIMARY KEY)')

    # ===================================
    # Implementation of interface methods
    # ===================================

    def close(self):
        if self.conn:
            self.conn.close()
            self.conn = None

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
                yield edge, attributes

    def begin_transaction(self):
        if self.batch_mode:
            return
        self.cur = self.conn.cursor()
        self.cur.execute('BEGIN TRANSACTION')

    def end_transaction(self):
        if self.batch_mode:
            return
        self.conn.commit()
        self.cur = None

    # ==========================================
    # Implementation of private abstract methods
    # ==========================================

    # from KeyValue
    def _edge2key(self, edge):
        return edge.to_str()

    def _exists_key(self, key):
        """Checks if the given key exists."""
        cur = self.conn.cursor()
        for key, _ in cur.execute('SELECT * FROM v WHERE key = ?', (key, )):
            return True
        return False

    def _add_key(self, key, attributes):
        """Adds the given edge, given its key."""
        value = _encode_attributes(attributes)
        self.cur.execute(
            'INSERT OR REPLACE INTO v (key, value) VALUES(?, ?)',
            (key, value))

    def _attribute_key(self, key):
        cur = self.conn.cursor()
        for key, value in cur.execute('SELECT * FROM v WHERE key = ?', (key,)):
            return _decode_attributes(value)
        return None

    def _write_edge_permutation(self, perm):
        """Writes a given permutation."""
        self.cur.execute('INSERT OR IGNORE INTO p (key) VALUES(?)', (perm,))

    def _remove_edge_permutation(self, perm):
        """Removes a given permutation."""
        self.cur.execute('DELETE FROM p WHERE key = ?', (perm,))

    def _remove_key(self, key):
        """Removes an edge, given its key."""
        self.cur.execute('DELETE FROM v WHERE key = ?', (key,))

    def _match_structure(self, pattern, strict):
        if not strict or is_full_pattern(pattern):
            for edge in self.all():
                yield edge
        else:
            nodes = []
            positions = []
            for i, node in enumerate(pattern):
                if not is_pattern(node):
                    nodes.append(node)
                    positions.append(i)
            start_str = edges2str(nodes)
            end_str = str_plus_1(start_str)

            cur = self.conn.cursor()
            for row in cur.execute('SELECT * FROM p WHERE key >= ? AND key < ?', (start_str, end_str)):
                key = row[0]
                tokens = split_edge_str(key)
                nper = int(tokens[-1])

                if nper == first_permutation(len(tokens) - 1, positions):
                    yield perm2edge(key)

    # from Hypergraph
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
                    yield edge

    def _atoms_with_root(self, root):
        start_str = ''.join((root, '/'))
        end_str = str_plus_1(start_str)

        cur = self.conn.cursor()
        for key, value in cur.execute(
                'SELECT * FROM v WHERE key >= ? AND key < ?',
                (start_str, end_str)):
            symb = hedge(key)
            yield symb

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
                            yield edge
                else:
                    # TODO: remove redundant results when a root is present
                    yield edge
