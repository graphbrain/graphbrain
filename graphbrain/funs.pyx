import string
import numpy as np


def is_atom(entity):
    """Checks if entity is an atom."""
    return isinstance(entity, str)


def is_edge(entity):
    """Checks if entity is an edge."""
    return not is_atom(entity)


def atom_parts(atom):
    """Splits an atom into its parts."""
    return atom.split('/')


def root(atom):
    """Extracts the root of an atom (e.g. the root of graphbrain/c/1 is graphbrain)."""
    return atom_parts(atom)[0]


def build_atom(text, role, namespace=None):
    """Build an atom from text, role and namespace."""
    atom = '%s/%s' % (str2atom(text), role)
    if namespace:
        atom = '%s/%s' % (atom, namespace)
    return atom


def str2atom(s):
    """Converts a string into a valid atom."""
    atom = s.lower()
    atom = atom.replace("/", "_")
    atom = atom.replace(" ", "_")
    atom = atom.replace("(", "_")
    atom = atom.replace(")", "_")
    return atom


def label(atom):
    """Converts an atom into a string representation."""
    return root(atom).replace('_', ' ')


def _open_pars(s):
    """Number of consecutive open parenthesis at the beginning of the string."""
    pos = 0
    while s[pos] == '(':
        pos += 1
    return pos


def _close_pars(s):
    """Number of consecutive close parenthesis at the end of the string."""
    pos = -1
    while s[pos] == ')':
        pos -= 1
    return -pos - 1


def _parsed_token(token):
    if _edge_str_has_outer_parens(token):
        return str2ent(token)
    else:
        return token


def _edge_str_has_outer_parens(str edge_str):
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


def str2ent(str ent_str):
    """Convert a string representation of an entity to an entity."""

    cdef str edge_inner_str = ent_str

    if _edge_str_has_outer_parens(ent_str):
        edge_inner_str = ent_str[1:-1]

    tokens = split_edge_str(edge_inner_str)
    if not tokens:
        return None
    elements = tuple(_parsed_token(token) for token in tokens)
    if len(elements) > 1:
        return elements
    elif len(elements) > 0:
        return elements[0]
    else:
        return None


def edges2str(edges, roots_only=False):
    """Convert a collection of edges to a string representation (no outer parenthesis)."""
    edges_string = []
    for entity in edges:
        if is_edge(entity):
            entity_str = ent2str(entity, roots_only)
        else:
            if roots_only:
                entity_str = root(entity)
            else:
                entity_str = entity

        if entity_str != '':
            edges_string.append(entity_str)
    return ' '.join(edges_string)


def ent2str(entity, roots_only=False):
    """Convert an entity to its string representation."""
    if is_atom(entity):
        if roots_only:
            return root(entity)
        else:
            return entity
    else:
        return '(%s)' % edges2str(entity, roots_only)


def atoms(entity):
    """Returns set of atoms contained in an entity."""
    if is_atom(entity):
        return {entity}
    else:
        atom_set = set()
        for item in entity:
            for atom in atoms(item):
                atom_set.add(atom)
        return atom_set


def depth(edge):
    """Returns maximal depth of an entity, an atom has depth 0."""
    if is_atom(edge):
        return 0
    else:
        max_d = 0
        for item in edge:
            d = depth(item)
            if d > max_d:
                max_d = d
        return max_d + 1


def roots(entity):
    """Returns entity with root-only atoms."""
    if is_atom(entity):
        return root(entity)
    else:
        return tuple(roots(item) for item in entity)


def contains(entity, needle, deep=False):
    """Checks if 'needle' is contained in entity."""
    if is_atom(entity):
        return entity == needle
    else:
        for item in entity:
            if item == needle:
                return True
            if deep:
                if contains(item, needle, True):
                    return True
        return False


def size(entity):
    """Size of an entity, atom size is 1."""
    if is_atom(entity):
        return 1
    else:
        return len(entity)


def subedges(edge):
    """Returns all the subedges contained in the edge, including atoms and itself."""
    edges = {edge}
    if is_edge(edge):
        for item in edge:
            edges = edges.union(subedges(item))
    return edges
