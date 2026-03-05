import json

import plyvel

from graphbrain.hyperedge import hedge
from graphbrain.memory.keyvalue import KeyValue
from graphbrain.memory.permutations import str_plus_1


def _encode_attributes(attributes):
    return json.dumps(attributes, ensure_ascii=False, check_circular=False, separators=(',', ':')).encode('utf-8')


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

    def _permutations_with_prefix(self, prefix):
        end_str = str_plus_1(prefix)
        start_key = (''.join(('p', prefix))).encode('utf-8')
        end_key = (''.join(('p', end_str))).encode('utf-8')
        for key, _ in self.db.iterator(start=start_key, stop=end_key):
            perm_str = key.decode('utf-8')
            yield perm_str[1:]

    def _edges_with_prefix(self, prefix):
        end_str = str_plus_1(prefix)
        start_key = (''.join(('v', prefix))).encode('utf-8')
        end_key = (''.join(('v', end_str))).encode('utf-8')
        for key, _ in self.db.iterator(start=start_key, stop=end_key):
            yield hedge(key.decode('utf-8')[1:])
