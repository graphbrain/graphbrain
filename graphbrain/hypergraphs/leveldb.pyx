import math
import itertools
import plyvel
from .hypergraph import Hypergraph
from graphbrain.funs import *


# maximum permutations of an edge that are written to the database
MAX_PERMS = 1000


permcache = {}


def _nthperm(n, nper):
    if n in permcache and nper in permcache[n]:
        return permcache[n][nper]

    pos = 0
    pindices = None
    for perm in itertools.permutations(range(n)):
        if pos >= nper:
            pindices = perm
            break
        pos += 1
    perm = tuple(pindices[i] for i in range(n))
    if n not in permcache:
        permcache[n] = {}
    permcache[n][nper] = perm
    return perm


def permutate(tokens, nper):
    """Reorder the tokens vector to perform a permutation,
       specified by nper."""
    n = len(tokens)
    indices = _nthperm(n, nper)
    return tuple(tokens[i] for i in indices)


def _unpermutate(tokens, nper):
    """Reorder the tokens vector to revert a permutation,
       specified by nper."""
    n = len(tokens)
    # rg = [x for x in range(n)]
    indices = _nthperm(n, nper)

    res = [None] * n
    pos = 0
    for i in indices:
        res[i] = tokens[pos]
        pos += 1

    return tuple(res)


def _do_with_edge_permutations(edge, f):
    """Applies the function f to all permutations of the given edge."""
    nperms = min(math.factorial(len(edge)), MAX_PERMS)
    for nperm in range(nperms):
        perm_str = ' '.join([ent2str(e) for e in permutate(edge, nperm)])
        perm_str = '%s %s' % (perm_str, nperm)
        f(perm_str)


def _perm2edge(perm_str):
    """Transforms a permutation string from a database query
       into an edge."""
    try:
        tokens = split_edge_str(perm_str[1:])
        if tokens is None:
            return None
        nper = int(tokens[-1])
        tokens = tokens[:-1]
        tokens = _unpermutate(tokens, nper)
        return str2ent(' '.join(tokens))
    except ValueError as v:
        return None
        # print(u'VALUE ERROR! %s _perm2edge %s' % (v, perm_str))


def _str_plus_1(s):
    """Increment a string by one, regaring lexicographical ordering."""
    last_char = s[-1]
    last_char = chr(ord(last_char) + 1)
    return '%s%s' % (s[:-1], last_char)


def _edge_matches_pattern(edge, pattern, open_ended):
    """Check if an edge matches a pattern."""
    if open_ended:
        if len(edge) < len(pattern):
            return False
    else:
        if len(edge) != len(pattern):
            return False

    for i in range(len(pattern)):
        if (pattern[i] is not None) and (pattern[i] != edge[i]):
            return False
    return True


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
        # plyvel.repair_db(locator_string)
        self.db = plyvel.DB(self.locator_string, create_if_missing=True)

    # ==================================
    # Implementation of abstract methods
    # ==================================
    def close(self):
        self.db.close()

    def name(self):
        return self.locator_string

    def destroy(self):
        """Erase the hypergraph."""
        self.db.close()
        plyvel.destroy_db(self.locator_string)
        self.db = plyvel.DB(self.locator_string, create_if_missing=True)

    def all(self):
        """Returns a lazy sequence of all the entities
           in the hypergraph."""
        start_str = 'v'
        end_str = _str_plus_1(start_str)
        start_key = (u'%s' % start_str).encode('utf-8')
        end_key = (u'%s' % end_str).encode('utf-8')

        for key, value in self.db.iterator(start=start_key, stop=end_key):
            entity = str2ent(key.decode('utf-8')[1:])
            yield entity

    def all_attributes(self):
        """Returns a lazy sequence with a tuple for each entity
           in the hypergraph.
           The first element of the tuple is the entity itself,
           the second is a dictionary of attribute values
           (as strings)."""
        start_str = 'v'
        end_str = _str_plus_1(start_str)
        start_key = (u'%s' % start_str).encode('utf-8')
        end_key = (u'%s' % end_str).encode('utf-8')

        for key, value in self.db.iterator(start=start_key, stop=end_key):
            entity = str2ent(key.decode('utf-8')[1:])
            attributes = _decode_attributes(value)
            yield (entity, attributes)

    def atom_count(self):
        """Total number of atoms in the hypergraph"""
        return self._read_counter('atom_count')

    def edge_count(self):
        """Total number of edge in the hypergraph"""
        return self._read_counter('edge_count')

    def total_degree(self):
        """Total degree of the hypergraph"""
        return self._read_counter('total_degree')

    def _exists(self, entity):
        """Checks if the given entity exists in the hypergraph."""
        return self._exists_key(_ent2key(entity))

    def _add(self, edge):
        """Adds an edge to the hypergraph if it does not exist yet."""
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
        """Removes an edge from the hypergraph."""
        edge_key = _ent2key(edge)
        if self._exists_key(edge_key):
            self._dec_counter('edge_count')
            self._dec_counter('total_degree', by=len(edge))
            for entity in edge:
                ent_key = _ent2key(entity)
                self._dec_attribute_key(ent_key, 'd')
            self._remove_edge_permutations(edge)
            self._remove_key(edge_key)

    def _pattern2edges(self, pattern, open_ended):
        """Return generator for all the edges that match a pattern.
           A pattern is a collection of entity ids and wildcards.
           Wildcards are represented by None.
           Pattern example: ('is/p', None, None)"""
        nodes = [node for node in pattern if node is not None]
        start_str = edges2str(nodes)
        end_str = _str_plus_1(start_str)
        start_key = (u'p%s' % start_str).encode('utf-8')
        end_key = (u'p%s' % end_str).encode('utf-8')

        edges = []
        for key, value in self.db.iterator(start=start_key, stop=end_key):
            perm_str = key.decode('utf-8')
            edge = _perm2edge(perm_str)
            if edge:
                edges.append(edge)

        return (edge for edge in edges
                if _edge_matches_pattern(edge, pattern, open_ended))

    def _star(self, center, limit=None):
        """Return all the edges that contain a given entity.
        Entity can be atomic or an edge."""
        center_id = center
        if isinstance(center, (list, tuple)):
            center_id = ent2str(center)
        return self._str2perms(center_id, limit)

    def _atoms_with_root(self, root):
        """Find all atoms with the given root."""
        start_str = '%s/' % root
        end_str = _str_plus_1(start_str)
        start_key = (u'v%s' % start_str).encode('utf-8')
        end_key = (u'v%s' % end_str).encode('utf-8')

        symbs = set()
        for key, value in self.db.iterator(start=start_key, stop=end_key):
            symb = str2ent(key.decode('utf-8')[1:])
            symbs.add(symb)
        return symbs

    def _edges_with_atoms(self, atoms, root):
        """Find all edges containing the given atoms,
           and a given root"""
        if root:
            start_str = '%s %s/' % (' '.join(atoms), root)
        else:
            start_str = ' '.join(atoms)
        end_str = _str_plus_1(start_str)
        start_key = (u'p%s' % start_str).encode('utf-8')
        end_key = (u'p%s' % end_str).encode('utf-8')

        edges = set()
        for key, value in self.db.iterator(start=start_key, stop=end_key):
            perm_str = key.decode('utf-8')
            edge = _perm2edge(perm_str)
            if edge:
                edges.add(edge)

        return edges

    def _set_attribute(self, entity, attribute, value):
        """Sets the value of an attribute."""
        ent_key = _ent2key(entity)
        return self._set_attribute_key(ent_key, attribute, value)

    def _inc_attribute(self, entity, attribute):
        """Increments an attribute of an entity."""
        ent_key = _ent2key(entity)
        return self._inc_attribute_key(ent_key, attribute)

    def _dec_attribute(self, entity, attribute):
        """Decrements an attribute of an entity."""
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
        """Returns the degree of an entity."""
        return self.get_int_attribute(entity, 'd', 0)
    # =========================================
    # End of implementation of abstract methods
    # =========================================

    def _add_key(self, ent_key, attributes):
        """Adds the given entity, given its key."""
        value = _encode_attributes(attributes)
        self.db.put(ent_key, value)

    def _write_edge_permutation(self, perm):
        perm_key = (u'p%s' % ent2str(perm)).encode('utf-8')
        self.db.put(perm_key, b'x')

    def _write_edge_permutations(self, edge):
        """Writes all permutations of the given edge."""
        _do_with_edge_permutations(edge, self._write_edge_permutation)

    def _remove_edge_permutation(self, perm):
        perm_key = (u'p%s' % ent2str(perm)).encode('utf-8')
        self.db.delete(perm_key)

    def _remove_edge_permutations(self, edge):
        """Removes all permutations of the given edge."""
        _do_with_edge_permutations(edge, self._remove_edge_permutation)

    def _remove_key(self, ent_key):
        """Removes an entity, given its key."""
        self.db.delete(ent_key)

    def _str2perms(self, center_id, limit=None):
        """Query database for all the edge permutations that contain a
           given entity, represented as a string."""
        start_str = '%s ' % center_id
        end_str = _str_plus_1(start_str)
        start_key = (u'p%s' % start_str).encode('utf-8')
        end_key = (u'p%s' % end_str).encode('utf-8')

        edges = []
        count = 0
        for key, value in self.db.iterator(start=start_key, stop=end_key):
            perm_str = key.decode('utf-8')
            edge = _perm2edge(perm_str)
            if edge:
                edges.append(edge)
            if limit:
                count += 1
                if count >= limit:
                    break

        return set(edges)

    def _exists_key(self, ent_key):
        """Checks if the given entity exists in the hypergraph."""
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
