import plyvel
from graphbrain.hypergraphs.hypergraph import Hypergraph
from graphbrain.funs import *
from graphbrain.hypergraphs.permutations import *


def _ent2key(entity):
    return ('v%s' % ent2str(entity)).encode('utf-8')


def _encode_attributes(attributes):
    str_list = ['%s|%s' % (key, attributes[key]) for key in attributes]
    return '\\'.join(str_list).encode('utf-8')


def _decode_attributes(value):
    tokens = value.decode('utf-8').split('\\')
    attributes = {}
    for token in tokens:
        parts = token.split('|')
        attributes[parts[0]] = parts[1]
    return attributes


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
        start_key = (u'%s' % start_str).encode('utf-8')
        end_key = (u'%s' % end_str).encode('utf-8')

        for key, value in self.db.iterator(start=start_key, stop=end_key):
            entity = str2ent(key.decode('utf-8')[1:])
            yield entity

    def all_attributes(self):
        start_str = 'v'
        end_str = str_plus_1(start_str)
        start_key = (u'%s' % start_str).encode('utf-8')
        end_key = (u'%s' % end_str).encode('utf-8')

        for key, value in self.db.iterator(start=start_key, stop=end_key):
            entity = str2ent(key.decode('utf-8')[1:])
            attributes = _decode_attributes(value)
            yield (entity, attributes)

    def atom_count(self):
        return self._read_counter('atom_count')

    def edge_count(self):
        return self._read_counter('edge_count')

    def total_degree(self):
        return self._read_counter('total_degree')

    # ==========================================
    # Implementation of private abstract methods
    # ==========================================

    def _exists(self, entity):
        return self._exists_key(_ent2key(entity))

    def _add(self, edge):
        edge_key = _ent2key(edge)
        if not self._exists_key(edge_key):
            self._inc_counter('edge_count')
            self._inc_counter('total_degree', by=len(edge))
            for entity in edge:
                ent_key = _ent2key(entity)
                if not self._inc_attribute_key(ent_key, 'd'):
                    if is_atom(entity):
                        self._inc_counter('atom_count')
                    else:
                        self._inc_counter('edge_count')
                    self._add_key(ent_key, {'d': 1})
            self._add_key(edge_key, {'d': 0})
            self._write_edge_permutations(edge)
        return edge

    def _remove(self, edge):
        edge_key = _ent2key(edge)
        if self._exists_key(edge_key):
            self._dec_counter('edge_count')
            self._dec_counter('total_degree', by=len(edge))
            for entity in edge:
                ent_key = _ent2key(entity)
                self._dec_attribute_key(ent_key, 'd')
            self._remove_edge_permutations(edge)
            self._remove_key(edge_key)

    def _pattern2edges(self, pattern):
        nodes = []
        positions = []
        for i, node in enumerate(pattern):
            if is_constant(node):
                nodes.append(node)
                positions.append(i)
        start_str = edges2str(nodes)
        end_str = str_plus_1(start_str)
        start_key = (u'p%s' % start_str).encode('utf-8')
        end_key = (u'p%s' % end_str).encode('utf-8')

        for key, value in self.db.iterator(start=start_key, stop=end_key):
            perm_str = key.decode('utf-8')

            tokens = split_edge_str(perm_str[1:])
            nper = int(tokens[-1])

            if nper == first_permutation(len(tokens) - 1, positions):
                edge = perm2edge(perm_str)
                if edge and edge_matches_pattern(edge, pattern):
                    yield edge

    def _star(self, center, limit=None):
        center_str = ent2str(center)
        start_str = '%s ' % center_str
        end_str = str_plus_1(start_str)
        start_key = (u'p%s' % start_str).encode('utf-8')
        end_key = (u'p%s' % end_str).encode('utf-8')

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
        start_str = '%s/' % root
        end_str = str_plus_1(start_str)
        start_key = (u'v%s' % start_str).encode('utf-8')
        end_key = (u'v%s' % end_str).encode('utf-8')

        for key, value in self.db.iterator(start=start_key, stop=end_key):
            symb = str2ent(key.decode('utf-8')[1:])
            yield(symb)

    def _edges_with_atoms(self, atoms, root):
        if root:
            start_str = '%s %s/' % (' '.join(atoms), root)
        else:
            start_str = ' '.join(atoms)
        end_str = str_plus_1(start_str)
        start_key = (u'p%s' % start_str).encode('utf-8')
        end_key = (u'p%s' % end_str).encode('utf-8')

        for key, value in self.db.iterator(start=start_key, stop=end_key):
            perm_str = key.decode('utf-8')
            edge = perm2edge(perm_str)
            if edge:
                yield(edge)

    def _set_attribute(self, entity, attribute, value):
        ent_key = _ent2key(entity)
        return self._set_attribute_key(ent_key, attribute, value)

    def _inc_attribute(self, entity, attribute):
        ent_key = _ent2key(entity)
        return self._inc_attribute_key(ent_key, attribute)

    def _dec_attribute(self, entity, attribute):
        ent_key = _ent2key(entity)
        return self._dec_attribute_key(ent_key, attribute)

    def _get_str_attribute(self, entity, attribute, or_else=None):
        ent_key = _ent2key(entity)
        return self._get_str_attribute_key(ent_key, attribute, or_else)

    def _get_int_attribute(self, entity, attribute, or_else=None):
        ent_key = _ent2key(entity)
        return self._get_int_attribute_key(ent_key, attribute, or_else)

    def _get_float_attribute(self, entity, attribute, or_else=None):
        ent_key = _ent2key(entity)
        return self._get_float_attribute_key(ent_key, attribute, or_else)

    def _degree(self, entity):
        return self.get_int_attribute(entity, 'd', 0)

    # =====================
    # Local private methods
    # =====================

    def _add_key(self, ent_key, attributes):
        """Adds the given entity, given its key."""
        value = _encode_attributes(attributes)
        self.db.put(ent_key, value)

    def _write_edge_permutation(self, perm):
        """Writes a given permutation."""
        perm_key = (u'p%s' % ent2str(perm)).encode('utf-8')
        self.db.put(perm_key, b'x')

    def _write_edge_permutations(self, edge):
        """Writes all permutations of the edge."""
        do_with_edge_permutations(edge, self._write_edge_permutation)

    def _remove_edge_permutation(self, perm):
        """Removes a given permutation."""
        perm_key = (u'p%s' % ent2str(perm)).encode('utf-8')
        self.db.delete(perm_key)

    def _remove_edge_permutations(self, edge):
        """Removes all permutations of the edge."""
        do_with_edge_permutations(edge, self._remove_edge_permutation)

    def _remove_key(self, ent_key):
        """Removes an entity, given its key."""
        self.db.delete(ent_key)

    def _exists_key(self, ent_key):
        """Checks if the given entity exists."""
        return self.db.get(ent_key) is not None

    def _set_attribute_key(self, ent_key, attribute, value):
        """Sets the value of an attribute by ent_key."""
        if self._exists_key(ent_key):
            attributes = self._attribute_key(ent_key)
            if isinstance(value, str):
                value = value.replace('|', ' ').replace('\\', ' ')
            attributes[attribute] = value
            self._add_key(ent_key, attributes)
            return True
        else:
            return False

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
        value = self.db.get(ent_key)
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
