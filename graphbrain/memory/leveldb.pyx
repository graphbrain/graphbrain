import json

import plyvel

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
                      separators=(',', ':')).encode('utf-8')


def _decode_attributes(value):
    return json.loads(value.decode('utf-8'))


class LevelDB(KeyValue):
    """Implements LevelDB hypergraph storage."""

    def __init__(self, locator_string):
        super().__init__(locator_string)
        self.db = plyvel.DB(self.locator_string, create_if_missing=True)

    # ===================================
    # Implementation of interface methods
    # ===================================

    def close(self):
        self.db.close()

    def destroy(self):
        self.db.close()
        plyvel.destroy_db(self.locator_string)
        self.db = plyvel.DB(self.locator_string, create_if_missing=True)

    def all(self):
        start_str = 'v'
        end_str = str_plus_1(start_str)
        start_key = start_str.encode('utf-8')
        end_key = end_str.encode('utf-8')

        for key, value in self.db.iterator(start=start_key, stop=end_key):
            edge = hedge(key.decode('utf-8')[1:])
            if edge is not None:
                yield edge

    def all_attributes(self):
        start_str = 'v'
        end_str = str_plus_1(start_str)
        start_key = start_str.encode('utf-8')
        end_key = end_str.encode('utf-8')

        for key, value in self.db.iterator(start=start_key, stop=end_key):
            edge = hedge(key.decode('utf-8')[1:])
            attributes = _decode_attributes(value)
            yield edge, attributes

    def begin_transaction(self):
        pass

    def end_transaction(self):
        pass

    # ==========================================
    # Implementation of private abstract methods
    # ==========================================

    # from KeyValue
    def _edge2key(self, edge):
        return (''.join(('v', edge.to_str()))).encode('utf-8')

    def _exists_key(self, key):
        """Checks if the given key exists."""
        return self.db.get(key) is not None

    def _add_key(self, key, attributes):
        """Adds the given edge, given its key."""
        value = _encode_attributes(attributes)
        self.db.put(key, value)

    def _attribute_key(self, key):
        value = self.db.get(key)
        return _decode_attributes(value)

    def _write_edge_permutation(self, perm):
        """Writes a given permutation."""
        perm_key = (''.join(('p', perm))).encode('utf-8')
        self.db.put(perm_key, b'x')

    def _remove_edge_permutation(self, perm):
        """Removes a given permutation."""
        perm_key = (''.join(('p', perm))).encode('utf-8')
        self.db.delete(perm_key)

    def _remove_key(self, key):
        """Removes an edge, given its key."""
        self.db.delete(key)

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
            start_key = (''.join(('p', start_str))).encode('utf-8')
            end_key = (''.join(('p', end_str))).encode('utf-8')

            for key, value in self.db.iterator(start=start_key, stop=end_key):
                perm_str = key.decode('utf-8')

                tokens = split_edge_str(perm_str[1:])
                nper = int(tokens[-1])

                if nper == first_permutation(len(tokens) - 1, positions):
                    yield perm2edge(perm_str[1:])

    # from Hypergraph
    def _star(self, center, limit=None):
        center_str = center.to_str()
        start_str = ''.join((center_str, ' '))
        end_str = str_plus_1(start_str)
        start_key = (''.join(('p', start_str))).encode('utf-8')
        end_key = (''.join(('p', end_str))).encode('utf-8')

        count = 0
        for key, value in self.db.iterator(start=start_key, stop=end_key):
            if limit and count >= limit:
                break
            perm_str = key.decode('utf-8')
            edge = perm2edge(perm_str[1:])
            if edge:
                position = edge.index(center)
                nper = int(split_edge_str(perm_str[1:])[-1])
                if nper == first_permutation(len(edge), (position,)):
                    count += 1
                    yield edge

    def _atoms_with_root(self, root):
        start_str = ''.join((root, '/'))
        end_str = str_plus_1(start_str)
        start_key = (''.join(('v', start_str))).encode('utf-8')
        end_key = (''.join(('v', end_str))).encode('utf-8')

        for key, value in self.db.iterator(start=start_key, stop=end_key):
            symb = hedge(key.decode('utf-8')[1:])
            yield symb

    def _edges_with_edges(self, edges, root):
        start_str = ' '.join([edge.to_str() for edge in edges])
        if root:
            start_str = ''.join((start_str, ' ', root, '/'))
        end_str = str_plus_1(start_str)
        start_key = (''.join(('p', start_str))).encode('utf-8')
        end_key = (''.join(('p', end_str))).encode('utf-8')

        for key, value in self.db.iterator(start=start_key, stop=end_key):
            perm_str = key.decode('utf-8')
            edge = perm2edge(perm_str[1:])
            if edge:
                if root is None:
                    if all([item in edge for item in edges]):
                        positions = [edge.index(item) for item in edges]
                        nper = int(split_edge_str(perm_str[1:])[-1])
                        if nper == first_permutation(len(edge), positions):
                            yield edge
                else:
                    # TODO: remove redundant results when a root is present
                    yield edge
