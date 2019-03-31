import string
import numpy as np


def is_atom(entity):
    """Checks if entity is an atom."""
    return isinstance(entity, str)


def is_edge(entity):
    """Checks if entity is an edge."""
    return isinstance(entity, (tuple, list))


def atom_parts(atom):
    """Splits an atom into its parts."""
    return atom.split('/')


def root(atom):
    """Extracts the root of an atom
    (e.g. the root of graphbrain/c/1 is graphbrain)."""
    return atom_parts(atom)[0]


def build_atom(text, *parts):
    """Build an atom from text and other parts."""
    atom = str2atom(text)
    parts_str = '/'.join([part for part in parts if part])
    if len(parts_str) > 0:
        atom = '{}/{}'.format(atom, parts_str)
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
    """Returns number of consecutive open parenthesis at the beginning of the
    string.
    """
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
    """Check if string representation of edge is delimited by outer
    parenthesis.
    """
    if len(edge_str) < 2:
        return False
    return edge_str[0] == '('


def split_edge_str(str edge_str):
    """Shallow split into tokens of a string representation of an edge,
    without outer parenthesis.
    """
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
    """Convert a collection of edges to a string representation
    (no outer parenthesis).
    """
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
    """Returns all the subedges contained in the edge, including atoms and
    itself.
    """
    edges = {edge}
    if is_edge(edge):
        for item in edge:
            edges = edges.union(subedges(item))
    return edges


def atom_role(atom):
    parts = atom.split('/')
    if len(parts) < 2:
        return tuple('c')
    else:
        return parts[1].split('.')


def atom_type(atom):
    return atom_role(atom)[0]


def entity_type(entity):
    if is_atom(entity):
        return atom_type(entity)
    elif is_edge(entity):
        ptype = entity_type(entity[0])
        if len(entity) < 2:
            return ptype
        else:
            if ptype[0] == 'p':
                outter_type = 'r'
            elif ptype[0] in {'a', 'm', 'w'}:
                return entity_type(entity[1])
            elif ptype[0] == 'x':
                outter_type = 'd'
            elif ptype[0] == 't':
                outter_type = 's'
            else:
                return 'c'

            return '{}{}'.format(outter_type, ptype[1:])
    else:
        return None


def connector_type(entity):
    if is_atom(entity):
        return entity_type(entity)
    else:
        return entity_type(entity[0])


def nest(inner, outer, before):
    if is_atom(outer):
        return outer, inner
    else:
        if before:
            return outer + (inner,)
        else:
            return (outer[0], inner) + outer[1:]


def parens(entity):
    if is_atom(entity):
        return (entity,)
    else:
        return tuple(entity)


def insert_first_argument(entity, argument):
    if is_atom(entity):
        return (entity, argument)
    elif is_edge(entity):
        return (entity[0], argument) + entity[1:]
    else:
        return None


def connect(entity, arguments):
    if len(arguments) == 0:
        return entity
    else:
        return parens(entity) + parens(arguments)


def sequence(target, entity, before, flat=True):
    if flat:
        if before:
            return parens(entity) + parens(target)
        else:
            return parens(target) + parens(entity)
    else:
        if before:
            return (entity, target)
        else:
            return (target, entity)


def apply_fun_to_atom(fun, atom, target):
    if is_atom(target):
        if target == atom:
            return fun(target)
        else:
            return target
    else:
        return tuple(apply_fun_to_atom(fun, atom, item) for item in target)


def replace_atom(entity, old, new):
    if is_atom(entity):
        if entity == old:
            return new
        else:
            return entity
    else:
        return tuple(replace_atom(item, old, new) for item in entity)


def atom_with_type(entity, typ):
    if is_atom(entity):
        n = len(typ)
        et = entity_type(entity)
        if len(et) >= n and et[:n] == typ:
            return entity
    else:
        for item in entity:
            atom = atom_with_type(entity)
            if atom:
                return atom
    return None


def predicate(entity):
    et = entity_type(entity)[0]

    if is_atom(entity):
        if et == 'p':
            return entity
    elif et == 'r':
        return predicate(entity[0])
    elif et == 'p':
        return predicate(entity[1])

    return None


def rel_arg_role(relation, position):
    """Relation argument role.
       Returns None if no role can be determined."""
    if entity_type(relation)[0] != 'r':
        return None
    else:
        pred = predicate(relation)
        if pred:
            role = atom_role(pred)
            if len(role) > 1:
                arg_roles = role[1]
                if position < len(arg_roles):
                    return arg_roles[position]
        return None
