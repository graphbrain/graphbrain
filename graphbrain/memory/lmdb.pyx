import shutil
import lmdb
from graphbrain.hypergraph import Hypergraph
from graphbrain.hyperedge import *
from graphbrain.memory.permutations import *


def _ent2key(entity):
    return entity.to_str().encode()


def _encode_attributes(attributes):
    str_list = [''.join((key, '|', str(attributes[key])))
                for key in attributes]
    return '\\'.join(str_list)


def _decode_attributes(value):
    tokens = value.decode().split('\\')
    attributes = {}
    for token in tokens:
        parts = token.split('|')
        attributes[parts[0]] = parts[1]
    return attributes


class LMDB(Hypergraph):
    """Implements LMDB hypergraph storage."""

    def __init__(self, locator_string):
        self.locator_string = locator_string
        self.env = None
        self.edges_db = None
        self.perms_db = None
        self.meta_db = None
        self._open()

    # ============================================
    # Implementation of abstract interface methods
    # ============================================

    def close(self):
        pass

    def name(self):
        return self.locator_string

    def destroy(self):
        shutil.rmtree(self.locator_string)
        self._open()

    def all(self):
        with self.env.begin(db=self.edges_db) as txn:
            cursor = txn.cursor()
            for key, _ in cursor:
                yield hedge(key.decode())

    def all_attributes(self):
        with self.env.begin(db=self.edges_db) as txn:
            cursor = txn.cursor()
            for key, value in cursor:
                edge = hedge(key.decode())
                attributes = _decode_attributes(value)
                yield (edge, attributes)

    def atom_count(self):
        return self._read_counter('atom_count')

    def edge_count(self):
        return self._read_counter('edge_count')

    def primary_atom_count(self):
        return self._read_counter('primary_atom_count')

    def primary_edge_count(self):
        return self._read_counter('primary_edge_count')

    # ==========================================
    # Implementation of private abstract methods
    # ==========================================

    def _exists(self, entity):
        return self._exists_key(_ent2key(entity))

    def _add(self, edge, primary):
        ent_key = _ent2key(edge)
        if not self._exists_key(ent_key):
            if primary:
                self._add_key(ent_key, {'p': 1, 'd': 0, 'dd': 0})
                self._inc_degrees(edge)
            else:
                self._add_key(ent_key, {'p': 0, 'd': 0, 'dd': 0})
            if edge.is_atom():
                if primary:
                    self._inc_counter('primary_atom_count')
                self._inc_counter('atom_count')
            else:
                self._write_edge_permutations(edge)
            if primary:
                self._inc_counter('primary_edge_count')
            self._inc_counter('edge_count')
        # if an entity is to be added as primary, but it already exists as
        # non-primary, then make it primary and update the degrees
        elif primary and not self._is_primary(edge):
            self._set_attribute(edge, 'p', 1)
            self._inc_degrees(edge)
        return edge

    def _remove(self, edge, deep):
        primary = self.is_primary(edge)

        if not edge.is_atom():
            if deep:
                for child in edge:
                    self._remove(child, deep=True)
            else:
                if primary:
                    self._dec_degrees(edge)

        ent_key = _ent2key(edge)
        if self._exists_key(ent_key):
            if edge.is_atom():
                self._dec_counter('atom_count')
                if primary:
                    self._dec_counter('primary_atom_count')
            else:
                self._remove_edge_permutations(edge)
            self._dec_counter('edge_count')
            if primary:
                self._dec_counter('primary_edge_count')
            self._remove_key(ent_key)

    def _is_primary(self, edge):
        return self._get_int_attribute(edge, 'p') == 1

    def _set_primary(self, edge, value):
        self._set_attribute(edge, 'p', 1 if value else 0)

    def _search(self, pattern):
        nodes = []
        positions = []
        for i, node in enumerate(pattern):
            if not node.is_pattern():
                nodes.append(node)
                positions.append(i)

        start_str = edges2str(nodes)
        end_str = str_plus_1(start_str)

        with self.env.begin(db=self.perms_db) as txn:
            cursor = txn.cursor()
            if cursor.set_range(start_str.encode()):
                for key, _ in cursor:
                    perm_str = key.decode()
                    if perm_str >= end_str:
                        break
                    tokens = split_edge_str(perm_str)
                    nper = int(tokens[-1])
                    if nper == first_permutation(len(tokens) - 1, positions):
                        edge = perm2edge(perm_str)
                        if edge and edge_matches_pattern(edge, pattern):
                            yield edge

    def _star(self, center, limit=None):
        center_str = center.to_str()
        start_str = ''.join((center_str, ' '))
        end_str = str_plus_1(start_str)

        with self.env.begin(db=self.perms_db) as txn:
            cursor = txn.cursor()
            if cursor.set_range(start_str.encode()):
                count = 0
                for key, _ in cursor:
                    if limit and count >= limit:
                        break
                    perm_str = key.decode()
                    if perm_str >= end_str:
                        break
                    edge = perm2edge(perm_str)
                    if edge:
                        position = edge.index(center)
                        nper = int(split_edge_str(perm_str)[-1])
                        if nper == first_permutation(len(edge), (position,)):
                            count += 1
                            yield(edge)

    def _atoms_with_root(self, root):
        start_str = ''.join((root, '/'))
        end_str = str_plus_1(start_str)

        with self.env.begin(db=self.edges_db) as txn:
            cursor = txn.cursor()
            if cursor.set_range(start_str.encode()):
                for key, _ in cursor:
                    skey = key.decode()
                    if skey >= end_str:
                        break
                    yield hedge(skey)

    def _edges_with_edges(self, edges, root):
        start_str = ' '.join([edge.to_str() for edge in edges])
        if root:
            start_str = ''.join((start_str, ' ', root, '/'))
        end_str = str_plus_1(start_str)

        with self.env.begin(db=self.perms_db) as txn:
            cursor = txn.cursor()
            if cursor.set_range(start_str.encode()):
                for key, _ in cursor:
                    perm_str = key.decode()
                    if perm_str >= end_str:
                        break
                    edge = perm2edge(perm_str)
                    if edge:
                        if root is None:
                            if all([item in edge for item in edges]):
                                positions = [edge.index(item)
                                             for item in edges]
                                nper = int(split_edge_str(perm_str)[-1])
                                if nper == first_permutation(len(edge),
                                                             positions):
                                    yield(edge)
                        else:
                            # TODO: remove redundant results when a root is
                            # present
                            yield(edge)

    def _set_attribute(self, edge, attribute, value):
        ent_key = _ent2key(edge)
        return self._set_attribute_key(ent_key, attribute, value)

    def _inc_attribute(self, edge, attribute):
        ent_key = _ent2key(edge)
        return self._inc_attribute_key(ent_key, attribute)

    def _dec_attribute(self, edge, attribute):
        ent_key = _ent2key(edge)
        return self._dec_attribute_key(ent_key, attribute)

    def _get_str_attribute(self, edge, attribute, or_else=None):
        ent_key = _ent2key(edge)
        return self._get_str_attribute_key(ent_key, attribute, or_else)

    def _get_int_attribute(self, edge, attribute, or_else=None):
        ent_key = _ent2key(edge)
        return self._get_int_attribute_key(ent_key, attribute, or_else)

    def _get_float_attribute(self, edge, attribute, or_else=None):
        ent_key = _ent2key(edge)
        return self._get_float_attribute_key(ent_key, attribute, or_else)

    def _degree(self, edge):
        return self.get_int_attribute(edge, 'd', 0)

    def _deep_degree(self, edge):
        return self.get_int_attribute(edge, 'dd', 0)

    # =====================
    # Local private methods
    # =====================

    def _open(self):
        self.env = lmdb.open(self.locator_string, max_dbs=3)
        self.edges_db = self.env.open_db(b'edges')
        self.perms_db = self.env.open_db(b'perms')
        self.meta_db = self.env.open_db(b'meta')

    def _add_key(self, ent_key, attributes):
        """Adds the given entity, given its key."""
        value = _encode_attributes(attributes)
        with self.env.begin(db=self.edges_db, write=True) as txn:
            txn.put(ent_key, value.encode())

    def _write_edge_permutation(self, perm):
        """Writes a given permutation."""
        with self.env.begin(db=self.perms_db, write=True) as txn:
            txn.put(perm.encode(), b'x')

    def _write_edge_permutations(self, edge):
        """Writes all permutations of the edge."""
        do_with_edge_permutations(edge, self._write_edge_permutation)

    def _remove_edge_permutation(self, perm):
        """Removes a given permutation."""
        with self.env.begin(db=self.perms_db, write=True) as txn:
            txn.delete(perm.encode())

    def _remove_edge_permutations(self, edge):
        """Removes all permutations of the edge."""
        do_with_edge_permutations(edge, self._remove_edge_permutation)

    def _remove_key(self, edge_key):
        """Removes an edge, given its key."""
        with self.env.begin(db=self.edges_db, write=True) as txn:
            txn.delete(edge_key)

    def _exists_key(self, edge_key):
        """Checks if the given entity exists."""
        with self.env.begin(db=self.edges_db) as txn:
            return txn.get(edge_key) is not None

    def _set_attribute_key(self, ent_key, attribute, value):
        """Sets the value of an attribute by ent_key."""
        if self._exists_key(ent_key):
            attributes = self._attribute_key(ent_key)
            if isinstance(value, str):
                value = value.replace('|', ' ').replace('\\', ' ')
            attributes[attribute] = value
        else:
            attributes = {'p': 0, 'd': 0, 'dd': 0}
            attributes[attribute] = value
        self._add_key(ent_key, attributes)

    def _inc_attribute_key(self, ent_key, attribute):
        """Increments an attribute of an entity."""
        if self._exists_key(ent_key):
            attributes = self._attribute_key(ent_key)
            cur_value = int(attributes[attribute])
            attributes[attribute] = cur_value + 1
            self._add_key(ent_key, attributes)
            return True
        else:
            return False

    def _dec_attribute_key(self, ent_key, attribute):
        """Decrements an attribute of an entity."""
        if self._exists_key(ent_key):
            attributes = self._attribute_key(ent_key)
            cur_value = int(attributes[attribute])
            attributes[attribute] = cur_value - 1
            self._add_key(ent_key, attributes)
            return True
        else:
            return False

    def _attribute_key(self, ent_key):
        with self.env.begin(db=self.edges_db) as txn:
            value = txn.get(ent_key)
            return _decode_attributes(value)

    def _get_str_attribute_key(self, ent_key, attribute, or_else=None):
        if self._exists_key(ent_key):
            attributes = self._attribute_key(ent_key)
            if attribute in attributes:
                return attributes[attribute]
            else:
                return or_else
        else:
            return or_else

    def _get_int_attribute_key(self, ent_key, attribute, or_else=None):
        if self._exists_key(ent_key):
            attributes = self._attribute_key(ent_key)
            if attribute in attributes:
                return int(attributes[attribute])
            else:
                return or_else
        else:
            return or_else

    def _get_float_attribute_key(self, ent_key, attribute, or_else=None):
        if self._exists_key(ent_key):
            attributes = self._attribute_key(ent_key)
            if attribute in attributes:
                return float(attributes[attribute])
            else:
                return or_else
        else:
            return or_else

    def __read_counter_key(self, counter_key):
        """Reads a counter by key."""
        with self.env.begin(db=self.meta_db) as txn:
            value = txn.get(counter_key)
            if value is None:
                return 0
            else:
                return int(value)

    def _read_counter(self, counter):
        """Reads a counter by name."""
        return self.__read_counter_key(counter.encode())

    def _inc_counter(self, counter, by=1):
        """Increments a counter."""
        key = counter.encode()
        value = self.__read_counter_key(key)
        with self.env.begin(db=self.meta_db, write=True) as txn:
            txn.put(key, str(value + by).encode())

    def _dec_counter(self, counter, by=1):
        """Decrements a counter."""
        key = counter.encode()
        value = self.__read_counter_key(key)
        with self.env.begin(db=self.meta_db, write=True) as txn:
            txn.put(key, str(value - by).encode())

    def _inc_degrees(self, edge, depth=0):
        if depth > 0:
            ent_key = _ent2key(edge)
            if not self._exists_key(ent_key):
                d = 1 if depth == 1 else 0
                self._add_key(ent_key, {'p': 0, 'd': d, 'dd': 1})
                if edge.is_atom():
                    self._inc_counter('atom_count')
                self._inc_counter('edge_count')
            else:
                if depth == 1:
                    self._inc_attribute_key(ent_key, 'd')
                self._inc_attribute_key(ent_key, 'dd')
        if not edge.is_atom():
            for child in edge:
                self._inc_degrees(child, depth + 1)

    def _dec_degrees(self, edge, depth=0):
        if depth > 0:
            ent_key = _ent2key(edge)
            if depth == 1:
                self._dec_attribute_key(ent_key, 'd')
            self._dec_attribute_key(ent_key, 'dd')
        if not edge.is_atom():
            for child in edge:
                self._dec_degrees(child, depth + 1)
