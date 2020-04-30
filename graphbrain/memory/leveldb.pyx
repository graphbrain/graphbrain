import json
import plyvel
from graphbrain.hypergraph import Hypergraph
from graphbrain.hyperedge import *
from graphbrain.memory.permutations import *


def _edge2key(edge):
    return (''.join(('v', edge.to_str()))).encode('utf-8')


def _encode_attributes(attributes):
    return json.dumps(attributes,
                      ensure_ascii=False,
                      check_circular=False,
                      separators=(',', ':')).encode('utf-8')


def _decode_attributes(value):
    return json.loads(value.decode('utf-8'))


class LevelDB(Hypergraph):
    """Implements LevelDB hypergraph storage."""

    def __init__(self, locator_string):
        self.locator_string = locator_string
        self.db = plyvel.DB(self.locator_string, create_if_missing=True)

    # ============================================
    # Implementation of abstract interface methods
    # ============================================

    def close(self):
        self.db.close()

    def name(self):
        return self.locator_string

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
            yield hedge(key.decode('utf-8')[1:])

    def all_attributes(self):
        start_str = 'v'
        end_str = str_plus_1(start_str)
        start_key = start_str.encode('utf-8')
        end_key = end_str.encode('utf-8')

        for key, value in self.db.iterator(start=start_key, stop=end_key):
            edge = hedge(key.decode('utf-8')[1:])
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

    def _exists(self, edge):
        return self._exists_key(_edge2key(edge))

    def _add(self, edge, primary):
        key = _edge2key(edge)
        if not self._exists_key(key):
            if primary:
                self._add_key(key, {'p': 1, 'd': 0, 'dd': 0})
                self._inc_degrees(edge)
            else:
                self._add_key(key, {'p': 0, 'd': 0, 'dd': 0})
            if edge.is_atom():
                if primary:
                    self._inc_counter('primary_atom_count')
                self._inc_counter('atom_count')
            else:
                self._write_edge_permutations(edge)
            if primary:
                self._inc_counter('primary_edge_count')
            self._inc_counter('edge_count')
        # if an edge is to be added as primary, but it already exists as
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

        key = _edge2key(edge)
        if self._exists_key(key):
            if edge.is_atom():
                self._dec_counter('atom_count')
                if primary:
                    self._dec_counter('primary_atom_count')
            else:
                self._remove_edge_permutations(edge)
            self._dec_counter('edge_count')
            if primary:
                self._dec_counter('primary_edge_count')
            self._remove_key(key)

    def _is_primary(self, edge):
        return self._get_int_attribute(edge, 'p') == 1

    def _set_primary(self, edge, value):
        self._set_attribute(edge, 'p', 1 if value else 0)

    def _search(self, pattern):
        for result in self._match(pattern):
            yield result[0]

    def _match_structure(self, pattern):
        if pattern.is_full_pattern():
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
            start_key = (''.join(('p', start_str))).encode('utf-8')
            end_key = (''.join(('p', end_str))).encode('utf-8')

            for key, value in self.db.iterator(start=start_key, stop=end_key):
                perm_str = key.decode('utf-8')

                tokens = split_edge_str(perm_str[1:])
                nper = int(tokens[-1])

                if nper == first_permutation(len(tokens) - 1, positions):
                    yield perm2edge(perm_str)

    def _match(self, pattern, curvars={}):
        for edge in self._match_structure(pattern):
            results = match_pattern(edge, pattern, curvars=curvars)
            if len(results) > 0:
                yield (edge, results)

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
            edge = perm2edge(perm_str)
            if edge:
                position = edge.index(center)
                nper = int(split_edge_str(perm_str[1:])[-1])
                if nper == first_permutation(len(edge), (position,)):
                    count += 1
                    yield(edge)

    def _atoms_with_root(self, root):
        start_str = ''.join((root, '/'))
        end_str = str_plus_1(start_str)
        start_key = (''.join(('v', start_str))).encode('utf-8')
        end_key = (''.join(('v', end_str))).encode('utf-8')

        for key, value in self.db.iterator(start=start_key, stop=end_key):
            symb = hedge(key.decode('utf-8')[1:])
            yield(symb)

    def _edges_with_edges(self, edges, root):
        start_str = ' '.join([edge.to_str() for edge in edges])
        if root:
            start_str = ''.join((start_str, ' ', root, '/'))
        end_str = str_plus_1(start_str)
        start_key = (''.join(('p', start_str))).encode('utf-8')
        end_key = (''.join(('p', end_str))).encode('utf-8')

        for key, value in self.db.iterator(start=start_key, stop=end_key):
            perm_str = key.decode('utf-8')
            edge = perm2edge(perm_str)
            if edge:
                if root is None:
                    if all([item in edge for item in edges]):
                        positions = [edge.index(item) for item in edges]
                        nper = int(split_edge_str(perm_str[1:])[-1])
                        if nper == first_permutation(len(edge), positions):
                            yield(edge)
                else:
                    # TODO: remove redundant results when a root is present
                    yield(edge)

    def _set_attribute(self, edge, attribute, value):
        key = _edge2key(edge)
        return self._set_attribute_key(key, attribute, value)

    def _inc_attribute(self, edge, attribute):
        key = _edge2key(edge)
        return self._inc_attribute_key(key, attribute)

    def _dec_attribute(self, edge, attribute):
        key = _edge2key(edge)
        return self._dec_attribute_key(key, attribute)

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

    def _add_key(self, key, attributes):
        """Adds the given edge, given its key."""
        value = _encode_attributes(attributes)
        self.db.put(key, value)

    def _write_edge_permutation(self, perm):
        """Writes a given permutation."""
        perm_key = (''.join(('p', perm))).encode('utf-8')
        self.db.put(perm_key, b'x')

    def _write_edge_permutations(self, edge):
        """Writes all permutations of the edge."""
        do_with_edge_permutations(edge, self._write_edge_permutation)

    def _remove_edge_permutation(self, perm):
        """Removes a given permutation."""
        perm_key = (''.join(('p', perm))).encode('utf-8')
        self.db.delete(perm_key)

    def _remove_edge_permutations(self, edge):
        """Removes all permutations of the edge."""
        do_with_edge_permutations(edge, self._remove_edge_permutation)

    def _remove_key(self, key):
        """Removes an edge, given its key."""
        self.db.delete(key)

    def _exists_key(self, key):
        """Checks if the given edge exists."""
        return self.db.get(key) is not None

    def _set_attribute_key(self, key, attribute, value):
        """Sets the value of an attribute by key."""
        if self._exists_key(key):
            attributes = self._attribute_key(key)
            attributes[attribute] = value
        else:
            attributes = {'p': 0, 'd': 0, 'dd': 0}
            attributes[attribute] = value
        self._add_key(key, attributes)

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
        value = self.db.get(key)
        return _decode_attributes(value)

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

    def __read_counter_key(self, counter_key):
        """Reads a counter by key."""
        value = self.db.get(counter_key)
        if value is None:
            return 0
        else:
            return int(value.decode('utf-8'))

    def _read_counter(self, counter):
        """Reads a counter by name."""
        return self.__read_counter_key(counter.encode('utf-8'))

    def _inc_counter(self, counter, by=1):
        """Increments a counter."""
        counter_key = counter.encode('utf-8')
        value = self.__read_counter_key(counter_key)
        self.db.put(counter_key, str(value + by).encode('utf-8'))

    def _dec_counter(self, counter, by=1):
        """Decrements a counter."""
        counter_key = counter.encode('utf-8')
        value = self.__read_counter_key(counter_key)
        self.db.put(counter_key, str(value - by).encode('utf-8'))

    def _inc_degrees(self, edge, depth=0):
        if depth > 0:
            key = _edge2key(edge)
            if not self._exists_key(key):
                d = 1 if depth == 1 else 0
                self._add_key(key, {'p': 0, 'd': d, 'dd': 1})
                if edge.is_atom():
                    self._inc_counter('atom_count')
                self._inc_counter('edge_count')
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
