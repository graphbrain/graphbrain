import string
import random
import numpy as np


class SymbolType(object):
    def __init__(self):
        pass

    UNKNOWN = 0
    CONCEPT = 1
    EDGE = 2
    INTEGER = 3
    FLOAT = 4
    URL = 5


def hashed(txt):
    """Creates an hash code for a string."""
    s = txt
    h = np.array([[1125899906842597]], dtype=np.uint64)  # prime
    while len(s) > 0:
        c = ord(s[0])
        h = np.dot(np.array([[31]], dtype=np.uint64), (h + np.array([[c]], dtype=np.uint64)))
        s = s[1:]
    return hex(h[0][0])[2:]


def __random_hash():
    """Creates random hash code."""
    s = ''.join(random.choice(string.ascii_lowercase + string.ascii_uppercase + string.digits) for _ in range(100))
    return hashed(s)


def symbol_type(sym):
    """Type of symbol: CONCEPT, EDGE, INTEGER, FLOAT or URL"""
    if isinstance(sym, (list, tuple)):
        return SymbolType.EDGE
    elif isinstance(sym, str):
        if sym[:7] == 'http://':
            return SymbolType.URL
        elif sym[:8] == 'https://':
            return SymbolType.URL
        else:
            return SymbolType.CONCEPT
    elif isinstance(sym, int):
        return SymbolType.INTEGER
    elif isinstance(sym, float):
        return SymbolType.FLOAT
    else:
        return SymbolType.UNKNOWN


def is_symbol(entity):
    """Checks if entity is a symbol."""
    return symbol_type(entity) != SymbolType.EDGE


def is_edge(entity):
    """Checks if entity is an edge."""
    return symbol_type(entity) == SymbolType.EDGE


def symbol_parts(sym):
    """Splits a symbol into its parts.
    All symbol types except CONCEPT only have one part."""
    if symbol_type(sym) == SymbolType.CONCEPT:
        return sym.split('/')
    else:
        return [sym]


def symbol_root(sym):
    """Extracts the root of a symbol (e.g. the root of graphbrain/1 is graphbrain)"""
    return symbol_parts(sym)[0]


def symbol_namespace(sym):
    """Extracts the namespace of a symbol (e.g. the namespace of graphbrain/1 is 1)
    Returns None if symbol has no namespace."""
    ps = symbol_parts(sym)
    if len(ps) > 1:
        return symbol_parts(sym)[1]
    else:
        return None


def is_root(sym):
    """Is the symbol the root of itself?"""
    return sym == symbol_root(sym)


def build_symbol(text, namespace):
    """Build a concept symbol from text and a namespace."""
    return '%s/%s' % (str2symbol(text), namespace)


def str2symbol(s):
    """Converts a string into a valid symbol"""
    sym = s.lower()
    sym = sym.replace("/", "_")
    sym = sym.replace(" ", "_")
    sym = sym.replace("(", "_")
    sym = sym.replace(")", "_")
    return sym


def symbol2str(sym):
    """Converts a symbol into a string representation."""
    stype = symbol_type(sym)
    if stype == SymbolType.CONCEPT:
        return symbol_root(sym).replace('_', ' ')
    elif stype == SymbolType.URL:
        return sym
    else:
        return str(sym)


def new_meaning(sym, prefix=''):
    """Creates a new symbol for the given root.
    If given edge_symbols is not a root, return it unchanged."""
    if is_root(sym):
        return build_symbol(sym, '%s%s' % (prefix, __random_hash()))
    else:
        return sym


def __open_pars(s):
    """Number of consecutive open parenthesis at the beginning of the string."""
    pos = 0
    while s[pos] == '(':
        pos += 1
    return pos


def __close_pars(s):
    """Number of consecutive close parenthesis at the end of the string."""
    pos = -1
    while s[pos] == ')':
        pos -= 1
    return -pos - 1


class __TokenType(object):
    def __init__(self):
        pass

    INTEGER = 0
    DOUBLE = 1
    STRING = 2


def __token_type(token):
    """Determine the type of a string token: STRING, INTEGER or DOUBLE."""

    s = token
    pos = 0
    point = False
    numbers = False

    while len(s) > 0:
        c = s[0]
        if c == '-':
            if pos > 0 or len(token) == 1:
                return __TokenType.STRING
        elif c == '.':
            if point:
                return __TokenType.STRING
            else:
                point = True
        elif c < '0' or c > '9':
            return __TokenType.STRING
        else:
            numbers = True
        s = s[1:]
        pos += 1

    if not numbers:
        return __TokenType.STRING

    if point:
        return __TokenType.DOUBLE
    else:
        return __TokenType.INTEGER


def __parsed_token(token):
    """Transform a string token into a value of the correct type."""
    if __edge_str_has_outer_parens(token):
        return str2edge(token)
    else:
        toktype = __token_type(token)
        if toktype == __TokenType.STRING:
            return token
        elif toktype == __TokenType.INTEGER:
            return int(token)
        elif toktype == __TokenType.DOUBLE:
            return float(token)


def __edge_str_has_outer_parens(str edge_str):
    """Check if string representation of edge is delimited by outer parenthesis."""
    if len(edge_str) < 2:
        return False
    return edge_str[0] == '('


def split_edge_str(str edge_str):
    """Shallow split into tokens of a string representation of an edge, without outer parenthesis."""
    cdef int start = 0
    cdef int depth = 0
    cdef int str_length = len(edge_str)
    cdef str c
    cdef int active = 0

    tokens = []
    for i in range(str_length):
        c = edge_str[i]
        if c == ' ':
            if active and depth == 0:
                tokens.append(edge_str[start:i])
                active = 0
        elif c == '(':
            if depth == 0:
                active = 1
                start = i
            depth += 1
        elif c == ')':
            depth -= 1
            if depth == 0:
                tokens.append(edge_str[start:i + 1])
                active = 0
            elif depth < 0:
                # TODO: throw exception?
                return None
        else:
            if not active:
                active = 1
                start = i

    if active:
        if depth > 0:
            # TODO: throw exception?
            return None
        else:
            tokens.append(edge_str[start:])

    return tuple(tokens)


def str2edge(str edge_str):
    """Convert a string representation of an edge to an edge."""

    cdef str edge_inner_str = edge_str

    if __edge_str_has_outer_parens(edge_str):
        edge_inner_str = edge_str[1:-1]

    tokens = split_edge_str(edge_inner_str)
    if not tokens:
        return None
    elements = tuple(__parsed_token(token) for token in tokens)
    if len(elements) > 1:
        return elements
    elif len(elements) > 0:
        return elements[0]
    else:
        return None


def edges2str(edges, namespaces=True):
    """Convert a collection of edges to a string representation (no outer parenthesis)."""
    edges_string = []
    for edge in edges:
        if symbol_type(edge) == SymbolType.EDGE:
            edge_str = edge2str(edge, namespaces)
        else:
            if namespaces:
                edge_str = str(edge).strip()
            else:
                edge_str = str(symbol_root(edge)).strip()
        if edge_str != '':
                edges_string.append(edge_str)
    return ' '.join(edges_string)


def edge2str(edge, namespaces=True):
    """Convert an edge to its string representation."""
    if symbol_type(edge) == SymbolType.EDGE:
        return '(%s)' % edges2str(edge, namespaces)
    else:
        if namespaces:
            return str(edge).strip()
        else:
            return str(symbol_root(edge)).strip()


def edge_symbols(edge):
    """Return set of edge_symbols contained in edge."""
    if symbol_type(edge) == SymbolType.EDGE:
        symbs = set()
        for entity in edge:
            for symb in edge_symbols(entity):
                symbs.add(symb)
        return symbs
    else:
        return {edge}


def edge_depth(edge):
    """Returns maximal depth of the edge, a symbol has depth 0"""
    if symbol_type(edge) == SymbolType.EDGE:
        max_d = 0
        for item in edge:
            d = edge_depth(item)
            if d > max_d:
                max_d = d
        return max_d + 1
    else:
        return 0


def without_namespaces(edge):
    """Returns edge stripped of namespaces"""
    if symbol_type(edge) == SymbolType.EDGE:
        return tuple(without_namespaces(item) for item in edge)
    else:
        return symbol_root(edge)


def edge_contains(edge, concept, deep=False):
    if is_edge(edge):
        for x in edge:
            if x == concept:
                return True
            if deep:
                if edge_contains(x, concept, True):
                    return True
        return False
    else:
        return edge == concept


def edge_size(edge):
    """size of edge, if symbol size is 1"""
    if is_edge(edge):
        return len(edge)
    return 1


def subedges(edge):
    """all the subedges contained in the edge, including atoms and itself"""
    edges = {edge}
    if is_edge(edge):
        for item in edge:
            edges = edges.union(subedges(item))
    return edges


def is_concept(edge):
    """Checks if edge represents a concept, i.e. if it is a symbol or if
       it is an edge that starts with +/gb."""
    if is_edge(edge):
        return len(edge) > 1 and edge[0] == '+/gb'
    else:
        return True
