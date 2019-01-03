import math
import itertools
import plyvel
from graphbrain.funs import *
from graphbrain.backends.backend import Backend


permcache = {}


def nthperm(n, nper):
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
    """Reorder the tokens vector to perform a permutation, specified by nper."""
    n = len(tokens)
    indices = nthperm(n, nper)
    return tuple(tokens[i] for i in indices)


def unpermutate(tokens, nper):
    """Reorder the tokens vector to revert a permutation, specified by nper."""
    n = len(tokens)
    # rg = [x for x in range(n)]
    indices = nthperm(n, nper)

    res = [None] * n
    pos = 0
    for i in indices:
        res[i] = tokens[pos]
        pos += 1

    return tuple(res)


def do_with_edge_permutations(edge, f):
    """Applies the function f to all permutations of the given edge."""
    nperms = math.factorial(len(edge))
    for nperm in range(nperms):
        perm_str = ' '.join([edge2str(e) for e in permutate(edge, nperm)])
        perm_str = '%s %s' % (perm_str, nperm)
        f(perm_str)


def perm2edge(perm_str):
    """Transforms a permutation string from a database query into an edge."""
    try:
        tokens = split_edge_str(perm_str[1:])
        if tokens is None:
            return None
        nper = int(tokens[-1])
        tokens = tokens[:-1]
        tokens = unpermutate(tokens, nper)
        return str2edge(' '.join(tokens))
    except ValueError as v:
        return None
        # print(u'VALUE ERROR! %s perm2edge %s' % (v, perm_str))


def str_plus_1(s):
    """Increment a string by one, regaring lexicographical ordering."""
    last_char = s[-1]
    last_char = chr(ord(last_char) + 1)
    return '%s%s' % (s[:-1], last_char)


def edge_matches_pattern(edge, pattern, open_ended):
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


def vertex2key(vertex):
    return ('v%s' % edge2str(vertex)).encode('utf-8')


def encode_attributes(attributes):
    str_list = ['%s|%s' % (key, attributes[key]) for key in attributes]
    return '\\'.join(str_list).encode('utf-8')


def decode_attributes(value):
    tokens = value.decode('utf-8').split('\\')
    attributes = {}
    for token in tokens:
        parts = token.split('|')
        attributes[parts[0]] = parts[1]
    return attributes


class LevelDB(Backend):
    """Implements LevelDB hypergraph storage."""

    def __init__(self, params):
        Backend.__init__(self)
        self.dir_path = params['hg']
        # plyvel.repair_db(file_path)
        self.db = plyvel.DB(self.dir_path, create_if_missing=True)

    def close(self):
        self.db.close()

    def name(self):
        return self.dir_path

    def add_key(self, vert_key, attributes):
        """Adds the given vertex, given its key."""
        value = encode_attributes(attributes)
        self.db.put(vert_key, value)

    def write_edge_permutation(self, perm):
        perm_key = (u'p%s' % edge2str(perm)).encode('utf-8')
        self.db.put(perm_key, b'x')

    def write_edge_permutations(self, edge):
        """Writes all permutations of the given edge."""
        do_with_edge_permutations(edge, self.write_edge_permutation)

    def remove_edge_permutation(self, perm):
        perm_key = (u'p%s' % edge2str(perm)).encode('utf-8')
        self.db.delete(perm_key)

    def remove_edge_permutations(self, edge):
        """Removes all permutations of the given edge."""
        do_with_edge_permutations(edge, self.remove_edge_permutation)

    def remove_key(self, vert_key):
        """Removes a vertex, given its key."""
        self.db.delete(vert_key)

    def str2perms(self, center_id, limit=None):
        """Query database for all the edge permutations that contain a given entity, represented as a string."""
        start_str = '%s ' % center_id
        end_str = str_plus_1(start_str)
        start_key = (u'p%s' % start_str).encode('utf-8')
        end_key = (u'p%s' % end_str).encode('utf-8')

        edges = []
        count = 0
        for key, value in self.db.iterator(start=start_key, stop=end_key):
            perm_str = key.decode('utf-8')
            edge = perm2edge(perm_str)
            if edge:
                edges.append(edge)
            if limit:
                count += 1
                if count >= limit:
                    break

        return set(edges)

    def pattern2edges(self, pattern, open_ended):
        """Return all the edges that match a pattern. A pattern is a collection of entity ids and wildcards (None)."""
        nodes = [node for node in pattern if node is not None]
        start_str = edges2str(nodes)
        end_str = str_plus_1(start_str)
        start_key = (u'p%s' % start_str).encode('utf-8')
        end_key = (u'p%s' % end_str).encode('utf-8')

        edges = []
        for key, value in self.db.iterator(start=start_key, stop=end_key):
            perm_str = key.decode('utf-8')
            edge = perm2edge(perm_str)
            if edge:
                edges.append(edge)

        return set([edge for edge in edges if edge_matches_pattern(edge, pattern, open_ended)])

    def exists_key(self, vertex_key):
        """Checks if the given vertex exists in the hypergraph."""
        return self.db.get(vertex_key) is not None

    def exists(self, vertex):
        """Checks if the given vertex exists in the hypergraph."""
        return self.exists_key(vertex2key(vertex))

    def set_attribute_key(self, vert_key, attribute, value):
        """Sets the value of an attribute by vertex_key."""
        if self.exists_key(vert_key):
            attributes = self.attribute_key(vert_key)
            if isinstance(value, str):
                value = value.replace('|', ' ').replace('\\', ' ')
            attributes[attribute] = value
            self.add_key(vert_key, attributes)
            return True
        else:
            return False

    def set_attribute(self, vertex, attribute, value):
        """Sets the value of an attribute."""
        vert_key = vertex2key(vertex)
        return self.set_attribute_key(vert_key, attribute, value)

    def inc_attribute_key(self, vert_key, attribute):
        """Increments an attribute of a vertex."""
        if self.exists_key(vert_key):
            attributes = self.attribute_key(vert_key)
            cur_value = int(attributes[attribute])
            attributes[attribute] = cur_value + 1
            self.add_key(vert_key, attributes)
            return True
        else:
            return False

    def dec_attribute_key(self, vert_key, attribute):
        """Decrements an attribute of a vertex."""
        if self.exists_key(vert_key):
            attributes = self.attribute_key(vert_key)
            cur_value = int(attributes[attribute])
            attributes[attribute] = cur_value - 1
            self.add_key(vert_key, attributes)
            return True
        else:
            return False

    def inc_attribute(self, vertex, attribute):
        """Increments an attribute of a vertex."""
        vert_key = vertex2key(vertex)
        return self.inc_attribute_key(vert_key, attribute)

    def dec_attribute(self, vertex, attribute):
        """Decrements an attribute of a vertex."""
        vert_key = vertex2key(vertex)
        return self.dec_attribute_key(vert_key, attribute)

    def add(self, edge, timestamp=-1):
        """Adds an edge to the hypergraph if it does not exist yet."""
        edge_key = vertex2key(edge)
        if not self.exists_key(edge_key):
            self.inc_counter('edge_count')
            self.inc_counter('total_degree', by=len(edge))
            for vert in edge:
                vert_key = vertex2key(vert)
                if not self.inc_attribute_key(vert_key, 'd'):
                    if symbol_type(vert) == SymbolType.EDGE:
                        self.inc_counter('edge_count')
                    else:
                        self.inc_counter('symbol_count')
                    self.add_key(vert_key, {'d': 1, 't': timestamp})
            self.add_key(edge_key, {'d': 0, 't': timestamp})
            self.write_edge_permutations(edge)
        return edge

    def remove(self, edge):
        """Removes an edge from the hypergraph."""
        edge_key = vertex2key(edge)
        if self.exists_key(edge_key):
            self.dec_counter('edge_count')
            self.dec_counter('total_degree', by=len(edge))
            for vert in edge:
                vert_key = vertex2key(vert)
                self.dec_attribute_key(vert_key, 'd')
            self.remove_edge_permutations(edge)
            self.remove_key(edge_key)

    def star(self, center, limit=None):
        """Return all the edges that contain a given entity.
        Entity can be atomic or an edge."""
        center_id = center
        if isinstance(center, (list, tuple)):
            center_id = edge2str(center)
        return self.str2perms(center_id, limit)

    def symbols_with_root(self, root):
        """Find all edge_symbols with the given root."""
        start_str = '%s/' % root
        end_str = str_plus_1(start_str)
        start_key = (u'v%s' % start_str).encode('utf-8')
        end_key = (u'v%s' % end_str).encode('utf-8')

        symbs = set()
        for key, value in self.db.iterator(start=start_key, stop=end_key):
            symb = str2edge(key.decode('utf-8')[1:])
            symbs.add(symb)
        return symbs

    def edges_with_symbols(self, symbols, root=None):
        """Find all edges containing the given edge_symbols, and optionally a given root"""
        if root:
            start_str = '%s %s/' % (' '.join(symbols), root)
        else:
            start_str = ' '.join(symbols)
        end_str = str_plus_1(start_str)
        start_key = (u'p%s' % start_str).encode('utf-8')
        end_key = (u'p%s' % end_str).encode('utf-8')

        edges = set()
        for key, value in self.db.iterator(start=start_key, stop=end_key):
            perm_str = key.decode('utf-8')
            edge = perm2edge(perm_str)
            if edge:
                edges.add(edge)

        return edges

    def destroy(self):
        """Erase the hypergraph."""
        self.db.close()
        plyvel.destroy_db(self.dir_path)
        self.db = plyvel.DB(self.dir_path, create_if_missing=True)

    def attribute_key(self, vert_key):
        value = self.db.get(vert_key)
        return decode_attributes(value)

    def get_str_attribute_key(self, vert_key, attribute, or_else=None):
        if self.exists_key(vert_key):
            attributes = self.attribute_key(vert_key)
            if attribute in attributes:
                return attributes[attribute]
            else:
                return or_else
        else:
            return or_else

    def get_str_attribute(self, vertex, attribute, or_else=None):
        vert_key = vertex2key(vertex)
        return self.get_str_attribute_key(vert_key, attribute, or_else)

    def get_int_attribute(self, vertex, attribute, or_else=None):
        vert_key = vertex2key(vertex)
        return self.get_int_attribute_key(vert_key, attribute, or_else)

    def get_int_attribute_key(self, vert_key, attribute, or_else=None):
        if self.exists_key(vert_key):
            attributes = self.attribute_key(vert_key)
            if attribute in attributes:
                return int(attributes[attribute])
            else:
                return or_else
        else:
            return or_else

    def get_float_attribute(self, vertex, attribute, or_else=None):
        vert_key = vertex2key(vertex)
        return self.get_float_attribute_key(vert_key, attribute, or_else)

    def get_float_attribute_key(self, vert_key, attribute, or_else=None):
        if self.exists_key(vert_key):
            attributes = self.attribute_key(vert_key)
            if attribute in attributes:
                return float(attributes[attribute])
            else:
                return or_else
        else:
            return or_else

    def degree(self, vertex):
        """Returns the degree of a vertex."""
        return self.get_int_attribute(vertex, 'd', 0)

    def timestamp(self, vertex):
        """Returns the timestamp of a vertex."""
        return self.get_int_attribute(vertex, 't', -1)

    def all(self):
        """Returns a lazy sequence of all the vertices in the hypergraph."""
        start_str = 'v'
        end_str = str_plus_1(start_str)
        start_key = (u'%s' % start_str).encode('utf-8')
        end_key = (u'%s' % end_str).encode('utf-8')

        for key, value in self.db.iterator(start=start_key, stop=end_key):
            vert = str2edge(key.decode('utf-8')[1:])
            yield vert

    def all_attributes(self):
        """Returns a lazy sequence with a tuple for each vertex in the hypergraph.
           The first element of the tuple is the vertex itself,
           the second is a dictionary of attribute values (as strings)."""
        start_str = 'v'
        end_str = str_plus_1(start_str)
        start_key = (u'%s' % start_str).encode('utf-8')
        end_key = (u'%s' % end_str).encode('utf-8')

        for key, value in self.db.iterator(start=start_key, stop=end_key):
            vert = str2edge(key.decode('utf-8')[1:])
            attributes = decode_attributes(value)
            yield (vert, attributes)

    def read_counter_key(self, counter_key):
        """Reads a counter by key."""
        value = self.db.get(counter_key)
        if value is None:
            return 0
        else:
            return int(value.decode('utf-8'))

    def read_counter(self, counter):
        """Reads a counter by name."""
        return self.read_counter_key(counter.encode('utf-8'))

    def inc_counter(self, counter, by=1):
        """Increments a counter."""
        counter_key = counter.encode('utf-8')
        value = self.read_counter_key(counter_key)
        self.db.put(counter_key, str(value + by).encode('utf-8'))

    def dec_counter(self, counter, by=1):
        """Decrements a counter."""
        counter_key = counter.encode('utf-8')
        value = self.read_counter_key(counter_key)
        self.db.put(counter_key, str(value - by).encode('utf-8'))

    def symbol_count(self):
        """Total number of edge_symbols in the hypergraph"""
        return self.read_counter('symbol_count')

    def edge_count(self):
        """Total number of edge in the hypergraph"""
        return self.read_counter('edge_count')

    def total_degree(self):
        """Total degree of the hypergraph"""
        return self.read_counter('total_degree')
