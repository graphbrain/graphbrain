import string
import numpy as np
import graphbrain.constants as const


# ======================
# Syntax-level functions
# ======================

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


def replace_atom_part(atom, part_pos, part):
    """Build a new atom by replacing an atom part in a given atom."""
    parts = atom_parts(atom)
    parts[part_pos] = part
    return '/'.join([part for part in parts if part])


def str2atom(s):
    """Converts a string into a valid atom."""
    atom = s.lower()
    atom = atom.replace('/', '_')
    atom = atom.replace(' ', '_')
    atom = atom.replace('(', '_')
    atom = atom.replace(')', '_')
    atom = atom.replace('.', '_')
    return atom


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

    ent_str = ent_str.replace('\n', ' ')
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
    """Convert an entity to its string representation.

    Keyword argument:
    roots_only -- only the roots of the atoms will be used to create
                  the string representation.
    """
    if is_atom(entity):
        if roots_only:
            return root(entity)
        else:
            return entity
    else:
        return '({})'.format(edges2str(entity, roots_only))


def label(entity):
    """Generate human-readable label from entity."""
    if is_atom(entity):
        return root(entity).replace('_', ' ')
    else:
        if len(entity) == 2:
            edge = entity
        elif entity[0] == const.noun_connector_pred:
            edge = entity[1:]
        else:
            edge = (entity[1], entity[0]) + entity[2:]
        return ' '.join([label(item) for item in edge])


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
    """Checks if 'needle' is contained in entity.

    Keyword argument:
    deep -- search recursively (default False)"""
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


def edge_matches_pattern(edge, pattern):
    """Check if an edge matches a pattern.

    Patterns are themselves edges. They can match families of edges
    by employing special atoms:
        -> '*' represents a general wildcard (matches any entity)
        -> '@' represents an atomic wildcard (matches any atom)
        -> '&' represents an edge wildcard (matches any edge)
        -> '...' at the end indicates an open-ended pattern.

    The pattern can be a valid edge.
    Examples: ('is/pd', 'graphbrain/c', '@')
              ('says/pd', '*', '...')

    The pattern can be a string, that must represent an edge.
    Examples: '(is/pd graphbrain/c @)'
              '(says/pd * ...)'
    """

    # if pattern is string, convert to edge and make recursive call.
    if type(pattern) == str:
        edge = str2ent(pattern)
        if is_edge(edge):
            return edge_matches_pattern(edge)
        else:
            # TODO: error or warning?
            return False
    else:
        # open ended?
        if pattern[-1] == '...':
            pattern = pattern[:-1]
            if len(edge) < len(pattern):
                return False
        else:
            if len(edge) != len(pattern):
                return False

        for i, pitem in enumerate(pattern):
            eitem = edge[i]
            if is_atom(pitem):
                if pitem == '@':
                    if not is_atom(eitem):
                        return False
                elif pitem == '&':
                    if not is_edge(eitem):
                        return False
                elif pitem != '*':
                    if eitem != pitem:
                        return False
            else:
                if is_edge(eitem):
                    if not edge_matches_pattern(eitem, pitem):
                        return False
                else:
                    return False
        return True


def nest(inner, outer, before=False):
    """Returns a new entity built by nesting the given 'inner' entity
    inside the given 'outer' entity.

    Nesting the 'inner' atom a in the 'outer' atom b produces:
    (b a)

    Nesting the 'inner' edge (a b) in the 'outer' atom c produces:
    (c (a b))

    If the outer entity is an edge, the result depends on the 'before'
    parameter. Considering the 'inner' entity (a b) and the 'outer'
    entity (c d), nesting if 'before' is True produces:
    (c d (a b))
    If 'before' is False it produces:
    (c (a b) d)

    Keyword argument:
    before -- controls how outer edges are handled, as per above.
              (default: False)
    """
    if is_atom(outer):
        return outer, inner
    else:
        if before:
            return outer + (inner,)
        else:
            return (outer[0], inner) + outer[1:]


def parens(entity):
    """Returns a tuple. Used when one wants to guarantee one is working
    with an edge-like structure, i.e. a tuple.
    If entity is an  atom, it returns (atom).
    If entity is an edge, it returns the same edge.
    """
    if is_atom(entity):
        return (entity,)
    else:
        return tuple(entity)


def insert_first_argument(entity, argument):
    """Returns an edge built by placing 'argument' as the first item
    after the connector of 'entity'. If 'entity' is an atom, then
    it becomes the connector of the returned edge.returned

    For example, considering the 'entity' a and the 'argument' b, this
    function returns:
    (a b)

    Considering the 'entity' (a b c) and the 'argument' (d e), it
    returns:
    (a (d e) b c)
    """
    if is_atom(entity):
        return (entity, argument)
    elif is_edge(entity):
        return (entity[0], argument) + entity[1:]
    else:
        return None


def connect(entity, arguments):
    """Returns an edge built by adding the items in 'arguments' to the
    end of 'entity'. 'arguments' must be a collection.

    For example, connecting the 'entity' (a b) with the 'arguments'
    (c d) produces:
    (a b c d)
    """
    if len(arguments) == 0:
        return entity
    else:
        return parens(entity) + parens(arguments)


def sequence(target, entity, before, flat=True):
    """Returns an edge built by sequencing the 'entity', if it's an
    edge, or the elements of 'entity' if it is an edge, either before
    or after the elements of 'target'.

    If flat is False, then both 'target' and 'entity' are treated as
    self-contained entities when building the new edge.

    For example, connecting the 'target' (a b) and the 'entity' c
    produces, if before is True:
    (c a b)
    and if before is False:
    (a b c)
    Connecting the 'target' (a b) and the 'entity' (c d)
    produces, if before is True:
    (c d a b)
    and if before is False:
    (a b c d)
    This last example, if 'flat' is False, becomes respectively:
    ((c d) (a b))
    ((a b) (c d))
    """
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
    """Returns edge built by replacing every instance of 'atom' in
    'target' with the output of calling 'fun' to 'atom'.
    """
    if is_atom(target):
        if target == atom:
            return fun(atom)
        else:
            return target
    else:
        return tuple(apply_fun_to_atom(fun, atom, item) for item in target)


def replace_atom(entity, old, new):
    """Returns edge built by replacing every instance of 'old' in
    'entity' with 'new'.
    """
    if is_atom(entity):
        if entity == old:
            return new
        else:
            return entity
    else:
        return tuple(replace_atom(item, old, new) for item in entity)


# ========================
# Semantic-level functions
# ========================

def atom_role(atom):
    """Returns the role of an atom as a list of the subrole strings.

    The role of an atom is its second part, right after the root.
    A dot notation is used to separate the subroles. For example,
    the role of graphbrain/cp.s/1 is:

        cp.s

    For this case, this function returns:

        ['cp', 's']

    If the atom only has a root, it is assumed to be a concept.
    In this case, this function returns the role with just the
    generic concept type:

        ['c'].
    """
    parts = atom.split('/')
    if len(parts) < 2:
        return list('c')
    else:
        return parts[1].split('.')


def atom_type(atom):
    """Returns the type of the atom.

    The type of an atom is its first subrole. For example, the
    type of graphbrain/cp.s/1 is 'cp'.

    If the atom only has a root, it is assumed to be a concept.
    In this case, this function returns the generic concept type: 'c'.
    """
    return atom_role(atom)[0]


def entity_type(entity):
    """Returns the type of the entity as a string.entity

    If the entity is an atom, atom_type(atom) is returned.
    If it is an edge, then the type is derived from the type of the
    connector. This applies recursively, as necessary.

    For example:
    graphbrain/cp.s/1 has type 'cp'
    (is/pd.so graphbrain/cp.s great/c) has type 'rd'
    (red/m shoes/cn.p) has type 'cn'
    (before/tt noon/c) has type 'st'
    """
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
    """Returns the type of the entity's connector.
    If the entity has no connector (i.e. it's an atom), then the entity
    type is returned.
    """
    if is_atom(entity):
        return entity_type(entity)
    else:
        return entity_type(entity[0])


def atom_with_type(entity, atom_type):
    """Returns the first atom found in 'entity' that has the given
    'atom_type', or whose type starts with 'atom_type'.
    If no such atom is found, returns None.

    For example, given the 'entity' (+/b a/cn b/cp) and the 'atom_type'
    c, this function returns:
    a/cn
    If the 'atom_type' is 'cp', the it will return:
    b/cp
    """
    if is_atom(entity):
        n = len(atom_type)
        et = entity_type(entity)
        if len(et) >= n and et[:n] == atom_type:
            return entity
    else:
        for item in entity:
            atom = atom_with_type(item, atom_type)
            if atom:
                return atom
    return None


def predicate(entity):
    """Returns predicate atom if 'entity' is a relation or edge of type
    predicate, or the 'entity' itself if it is an atom of type
    predicate. Returns None otherwise.
    """
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
    """Returns argument role of argument in a given 'position' of 'relation'.
    Returns None if argument role cannot be determined.

    Example:
    The argument role of argument at position 0 in:
    (is/pd.sc graphbrain/c great/c)
    is:
    s
    """
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


def is_constant(entity):
    if is_atom(entity):
        return entity not in {'*', '@', '&', '...'}
    else:
        return all(is_constant(item) for item in entity)


def no_constant(entity):
    if is_atom(entity):
        return entity in {'*', '@', '&', '...'}
    else:
        return all(no_constant(item) for item in entity)
