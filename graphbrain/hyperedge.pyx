import itertools
from collections import Counter
import graphbrain.constants as const


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


def edges2str(edges, roots_only=False):
    """Convert a collection of edges to a string representation
    (no outer parenthesis).
    """
    edges_strings = [edge.to_str(roots_only) for edge in edges]
    return ' '.join(edges_strings)


def _matches_wildcard(edge, wildcard):
    wparts = wildcard.parts()

    # structural match
    struct_code = wparts[0][0]
    if struct_code == '@':
        if not edge.is_atom():
            return False
    elif struct_code == '&':
        if edge.is_atom():
            return False

    # role match
    if len(wparts) > 1:
        # type match
        wrole = wildcard.role()
        wtype = wrole[0]
        eatom = edge.atom()
        etype = eatom.type()
        n = len(wtype)
        if len(etype) < n or etype[:n] != wtype:
            return False

        if len(wrole) > 1:
            erole = eatom.role()
            # check if edge role has enough parts to satisfy the wildcard
            # specification
            if len(erole) < len(wrole):
                return False

            pos = 1

            # argroles match
            if wtype[0] in {'B', 'P'}:
                wargroles_parts = wrole[1].split('~')
                if len(wargroles_parts) == 1:
                    wargroles_parts.append('')
                wposroles, wnegroles = wargroles_parts
                wargroles = set(wposroles) | set(wnegroles)
                eargroles = erole[1]
                for argrole in wargroles:
                    min_count = wposroles.count(argrole)
                    # if there are argrole exclusions
                    fixed = wnegroles.count(argrole) > 0
                    count = eargroles.count(argrole)
                    if count < min_count:
                        return False
                    # deal with exclusions
                    if fixed and count > min_count:
                        return False
                pos = 2

            # match rest of role
            while pos < len(wrole):
                if erole[pos] != wrole[pos]:
                    return False

    # match rest of atom
    if len(wparts) > 2:
        eparts = eatom.parts()
        # check if edge role has enough parts to satisfy the wildcard
        # specification
        if len(eparts) < len(wparts):
            return False

        while pos < len(wparts):
            if eparts[pos] != wparts[pos]:
                return False
            pos += 1

    return True


def _varname(atom):
    label = atom.parts()[0]
    if label[0] in {'*', '@', '&'}:
        return label[1:]
    elif label[:3] == '...':
        return label[3:]
    else:
        return label


def _match_by_argroles(edge, pattern, role_counts, matched=(), curvars={}):
    if len(role_counts) == 0:
        return [{}]

    argrole, n = role_counts[0]

    # match connector
    if argrole == 'X':
        eitems = [edge[0]]
        pitems = [pattern[0]]
    # match any argrole
    elif argrole == '*':
        eitems = [e for e in edge if e not in matched]
        pitems = pattern[-n:]
    # match specific argrole
    else:
        eitems = edge.edges_with_argrole(argrole)
        pitems = pattern.edges_with_argrole(argrole)

    if len(eitems) < n:
        return []

    result = []

    perms = tuple(itertools.permutations(eitems, r=n))
    for perm in perms:
        success = False
        vars = {}
        for i, eitem in enumerate(perm):
            success = False
            pitem = pitems[i]
            if pitem.is_atom():
                if pitem.is_pattern():
                    varname = _varname(pitem)
                    # check if variable is already assigned
                    if varname in curvars:
                        if curvars[varname] != eitem:
                            break
                    elif varname in vars:
                        if vars[varname] != eitem:
                            break
                    # if not, try to match
                    elif _matches_wildcard(eitem, pitem):
                        if len(varname) > 0:
                            vars[varname] = eitem
                    else:
                        break
                elif eitem != pitem and argrole != 'X':
                    break
                perm_result = [vars]
            else:
                if eitem.is_atom():
                    break
                else:
                    all_vars = {**curvars, **vars}
                    sresult = match_pattern(eitem, pitem, all_vars)
                    perm_result = [{**all_vars, **subvars}
                                   for subvars in sresult]
            success = True
        if success:
            remaining_result = _match_by_argroles(edge,
                                                  pattern,
                                                  role_counts[1:],
                                                  matched + perm,
                                                  {**curvars, **vars})
            for vars in perm_result:
                for remaining_vars in remaining_result:
                    all_vars = {**curvars, **vars, **remaining_vars}
                    if all_vars not in result:
                        result.append(all_vars)

    return result


def match_pattern(edge, pattern, curvars={}):
    """Matches an edge to a pattern. This means that, if the edge fits the
    pattern, then a dictionary will be returned with the values for each
    pattern variable. If the pattern specifies no variables but the edge
    matches it, then an empty dictionary is returned. If the edge does
    not match the pattern, None is returned.

    Patterns are themselves edges. They can match families of edges
    by employing special atoms:

    -> '\*' represents a general wildcard (matches any entity)

    -> '@' represents an atomic wildcard (matches any atom)

    -> '&' represents an edge wildcard (matches any edge)

    -> '...' at the end indicates an open-ended pattern.

    The three wildcards ('\*', '@' and '&') can be used to specify variables,
    for example '\*x', '&claim' or '@actor'. In case of a match, these
    variables are assigned the hyperedge they correspond to. For example,

    (1) the edge: (is/Pd (my/Mp name/Cn) mary/Cp)
    applied to the pattern: (is/Pd (my/Mp name/Cn) \*NAME)
    produces the result: {'NAME', mary/Cp}

    (2) the edge: (is/Pd (my/Mp name/Cn) mary/Cp)
    applied to the pattern: (is/Pd (my/Mp name/Cn) &NAME)
    produces the result: {}

    (3) the edge: (is/Pd (my/Mp name/Cn) mary/Cp)
    applied to the pattern: (is/Pd @ \*NAME)
    produces the result: None
    """

    edge = hedge(edge)
    pattern = hedge(pattern)

    # atomic patterns
    if pattern.is_atom():
        if _matches_wildcard(edge, pattern):
            vars = {}
            if pattern.is_pattern():
                varname = _varname(pattern)
                if len(varname) > 0:
                    vars[varname] = edge
            return [{**curvars, **vars}]
        else:
            return []

    # open ended?
    if pattern[-1].to_str() == '...':
        pattern = hedge(pattern[:-1])
        if len(edge) < len(pattern):
            return []
    else:
        if len(edge) != len(pattern):
            return []

    result = [{}]
    argroles = pattern[0].argroles().split('~')[0]
    # match by order
    if len(argroles) == 0:
        for i, pitem in enumerate(pattern):
            eitem = edge[i]
            _result = []
            for vars in result:
                if pitem.is_atom():
                    if pitem.is_pattern():
                        varname = _varname(pitem)
                        if varname in curvars:
                            if curvars[varname] != eitem:
                                continue
                        elif varname in vars:
                            if vars[varname] != eitem:
                                continue
                        elif _matches_wildcard(eitem, pitem):
                            if len(varname) > 0:
                                vars[varname] = eitem
                        else:
                            continue
                    elif eitem != pitem:
                        continue
                    _result.append(vars)
                else:
                    if not eitem.is_atom():
                        sresult = match_pattern(
                            eitem, pitem, {**curvars, **vars})
                        for subvars in sresult:
                            _result.append({**vars, **subvars})
            result = _result
    # match by argroles
    else:
        result = []
        # match connectors first
        econn = edge[0]
        pconn = pattern[0]
        for vars in match_pattern(econn, pconn, curvars):
            role_counts = Counter(argroles).most_common()
            unknown_roles = (len(pattern) - 1) - len(argroles)
            if unknown_roles > 0:
                role_counts.append(('*', unknown_roles))
            # add connector pseudo-argrole
            role_counts = [('X', 1)] + role_counts
            sresult = _match_by_argroles(edge,
                                         pattern,
                                         role_counts,
                                         curvars={**curvars, **vars})
            for svars in sresult:
                result.append({**vars, **svars})

    return list({**curvars, **vars} for vars in result)


def edge_matches_pattern(edge, pattern):
    """Check if an edge matches a pattern.

    Patterns are themselves edges. They can match families of edges
    by employing special atoms:

    -> '*' represents a general wildcard (matches any entity)

    -> '@' represents an atomic wildcard (matches any atom)

    -> '&' represents an edge wildcard (matches any edge)

    -> '...' at the end indicates an open-ended pattern.

    The pattern can be any valid hyperedge, including the above special atoms.
    Examples: (is/Pd graphbrain/C @)
    (says/Pd * ...)
    """
    result = match_pattern(edge, pattern)
    return len(result) > 0


def rel_arg_role(relation, position):
    """Returns argument role of argument in a given 'position' of 'relation'.
    Returns None if argument role cannot be determined.

    Example:
    The argument role of argument at position 0 in:
    (is/Pd.sc graphbrain/C great/C)
    is:
    s
    """
    if relation.type()[0] != 'R':
        return None
    else:
        pred = relation.predicate()
        if pred:
            role = pred.role()
            if len(role) > 1:
                arg_roles = role[1]
                if position < len(arg_roles):
                    return arg_roles[position]
        return None


def _parsed_token(token):
    if _edge_str_has_outer_parens(token):
        return hedge(token)
    else:
        return Atom((token,))


def hedge(source):
    """Create an hyperedge."""
    cdef str edge_str
    cdef str edge_inner_str
    if type(source) in {tuple, list}:
        return Hyperedge(tuple(hedge(item) for item in source))
    elif type(source) is str:
        edge_str = source.strip().replace('\n', ' ')
        edge_inner_str = edge_str

        if _edge_str_has_outer_parens(edge_str):
            edge_inner_str = edge_str[1:-1]

        tokens = split_edge_str(edge_inner_str)
        if not tokens:
            return None
        edges = tuple(_parsed_token(token) for token in tokens)
        if len(edges) > 1:
            return Hyperedge(edges)
        elif len(edges) > 0:
            return Atom(edges[0])
        else:
            return None
    elif type(source) in {Hyperedge, Atom}:
        return source
    else:
        return None


def build_atom(text, *parts):
    """Build an atom from text and other parts."""
    atom = str2atom(text)
    parts_str = '/'.join([part for part in parts if part])
    if len(parts_str) > 0:
        atom = ''.join((atom, '/', parts_str))
    return Atom((atom,))


class Hyperedge(tuple):
    """Non-atomic hyperedge."""
    def __new__(cls, edges):
        return super(Hyperedge, cls).__new__(cls, tuple(edges))

    def is_atom(self):
        """Checks if edge is an atom."""
        return False

    def to_str(self, roots_only=False):
        """Converts edge to its string representation.

        Keyword argument:
        roots_only -- only the roots of the atoms will be used to create
        the string representation.
        """
        s = ' '.join([edge.to_str(roots_only=roots_only) for edge in self])
        return ''.join(('(', s, ')'))

    def label(self):
        """Generate human-readable label for edge."""
        if len(self) == 2:
            edge = self
        elif self[0][0][:3] == '+/B':
            edge = self[1:]
        else:
            edge = (self[1], self[0]) + self[2:]
        return ' '.join([item.label() for item in edge])

    def atom(self):
        return self[1].atom()

    def atoms(self):
        """Returns the set of atoms contained in the edge.

        For example, consider the edge:
        (the/md (of/br mayor/cc (the/md city/cs)))
        in this case, edge.atoms() returns:
        [the/md, of/br, mayor/cc, city/cs]
        """
        atom_set = set()
        for item in self:
            for atom in item.atoms():
                atom_set.add(atom)
        return atom_set

    def all_atoms(self):
        """Returns a list of all the atoms contained in the edge. Unlike
        atoms(), which does not return repeated atoms, all_atoms() does
        return repeated atoms if they are different objects.

        For example, consider the edge:
        (the/md (of/br mayor/cc (the/md city/cs)))
        in this case, edge.all_atoms() returns:
        [the/md, of/br, mayor/cc, the/md, city/cs]
        """
        atoms = []
        for item in self:
            atoms += item.all_atoms()
        return atoms

    def depth(self):
        """Returns maximal depth of edge, an atom has depth 0."""
        max_d = 0
        for item in self:
            d = item.depth()
            if d > max_d:
                max_d = d
        return max_d + 1

    def roots(self):
        """Returns edge with root-only atoms."""
        return Hyperedge(tuple(item.roots() for item in self))

    def contains(self, needle, deep=False):
        """Checks if 'needle' is contained in edge.

        Keyword argument:
        deep -- search recursively (default False)"""
        for item in self:
            if item == needle:
                return True
            if deep:
                if item.contains(needle, True):
                    return True
        return False

    def subedges(self):
        """Returns all the subedges contained in the edge, including atoms
        and itself.
        """
        edges = {self}
        for item in self:
            edges = edges.union(item.subedges())
        return edges

    def nest(self, outer, before=False):
        """Returns a new entity built by nesting this edge (inner)
        inside the given (outer) edge.

        Nesting the 'inner' atom (a) in the 'outer' atom (b) produces:
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
        if outer.is_atom():
            return Hyperedge((outer, self))
        else:
            if before:
                return outer + (self,)
            else:
                return Hyperedge((outer[0], self)) + outer[1:]

    def insert_first_argument(self, argument):
        """Returns an edge built by placing 'argument' as the first item
        after the connector of this edge. If this edge is an atom, then
        it becomes the connector of the returned edge.

        For example, considering the 'edge' (a) and the 'argument' (b), this
        function returns:
        (a b)

        Considering the 'edge' (a b c) and the 'argument' (d e), it
        returns:
        (a (d e) b c)
        """
        return Hyperedge((self[0], argument) + self[1:])

    def connect(self, arguments):
        """Returns an edge built by adding the items in 'arguments' to the
        end of this edge. 'arguments' must be a collection.

        For example, connecting the edge (a b) with the 'arguments'
        (c d) produces:
        (a b c d)
        """
        if arguments is None or len(arguments) == 0:
            return self
        else:
            return Hyperedge(self + arguments)

    def sequence(self, entity, before, flat=True):
        """Returns an edge built by sequencing the 'entity', if it's an
        atom, or the elements of 'entity' if it is an edge, either before
        or after the elements of this edge.

        If flat is False, then both this edge and 'entity' are treated as
        self-contained edges when building the new edge.

        For example, connecting the edge (a b) and the 'entity' c
        produces, if before is True:
        (c a b)
        and if before is False:
        (a b c)
        Connecting the edge (a b) and the 'entity' (c d)
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
                return entity + self
            else:
                return self + entity
        else:
            if before:
                return Hyperedge((entity, self))
            else:
                return Hyperedge((self, entity))

    def replace_atom(self, old, new):
        """Returns edge built by replacing every instance of 'old' in
        this edge with 'new'.
        """
        return Hyperedge(tuple(item.replace_atom(old, new) for item in self))

    def type(self):
        """Returns the type of this edge as a string.

        The type is derived from the type of the
        connector. This applies recursively, as necessary.

        For example:
        (is/Pd.so graphbrain/Cp.s great/C) has type 'Rd'
        (red/M shoes/Cn.p) has type 'Cn'
        (before/Tt noon/C) has type 'St'
        """
        # TODO: verify correctness and throw errors
        ptype = self[0].type()
        if ptype[0] == 'P':
            outter_type = 'R'
        elif ptype[0] == 'M':
            return self[1].type()
        elif ptype[0] == 'T':
            outter_type = 'S'
        elif ptype[0] == 'J':
            inner_type = self[1].type()[0]
            if inner_type in {'P', 'S'}:
                return 'R'
            elif inner_type in {'M', 'B'}:
                return 'C'
            else:
                return inner_type
        else:
            return 'C'

        return '{}{}'.format(outter_type, ptype[1:])

    def connector_type(self):
        """Returns the type of the edge's connector.
        If the edge has no connector (i.e. it's an atom), then the atom
        type is returned.
        """
        return self[0].type()

    def atom_with_type(self, atom_type):
        """Returns the first atom found in the edge that has the given
        'atom_type', or whose type starts with 'atom_type'.
        If no such atom is found, returns None.

        For example, given the edge (+/B a/Cn b/Cp) and the 'atom_type'
        c, this function returns:
        a/Cn
        If the 'atom_type' is 'Cp', the it will return:
        b/Cp
        """
        for item in self:
            atom = item.atom_with_type(atom_type)
            if atom:
                return atom
        return None

    def contains_atom_type(self, atom_type):
        """Checks if the edge contains any atom with the given type.
        The edge is searched recursively, so the atom can appear at any depth.
        """
        return self.atom_with_type(atom_type) is not None

    def predicate(self):
        """Returns predicate atom if this edge is a non-atom of type
        relation or predicate. Returns itself if it is an atom of type
        predicate. Returns None otherwise.
        """
        et = self.type()[0]
        if et == 'R':
            return self[0].predicate()
        elif et == 'P':
            return self[1].predicate()
        return None

    def is_pattern(self):
        """Check if this edge defines a pattern, i.e. if it includes at least
        one pattern matcher.

        Pattern matchers are:
        '*', '@', '&', '...' and variables (atom label starting with an
        uppercase letter)
        """
        return any(item.is_pattern() for item in self)

    def is_full_pattern(self):
        """Check if every atom is a pattern matcher.

        Pattern matchers are:
        '*', '@', '&', '...' and variables (atom label starting with an
        uppercase letter)
        """
        return all(item.is_full_pattern() for item in self)

    def argroles(self):
        """Returns the argument roles string of the edge, if it exists.
        Otherwise returns empty string.
        """
        et = self.type()[0]
        if et not in {'B', 'P'}:
            return ''
        return self[1].argroles()

    def replace_argroles(self, argroles):
        """Returns an edge with the argroles of the connector atom replaced
        with the provided string.
        Returns same edge if the atom does not contain a role part."""
        st = self.type()[0]
        if st in {'C', 'R'}:
            new_edge = [self[0].replace_argroles(argroles)]
            new_edge += self[1:]
            return Hyperedge(new_edge)
        elif st == 'P':
            new_edge = [self[0], self[1].replace_argroles(argroles)]
            return Hyperedge(new_edge)
        return self

    def insert_argrole(self, argrole, pos):
        """Returns an edge with the given argrole inserted at the specified
        position in the argroles of the connector atom.
        Same restrictions as in replace_argroles() apply."""
        st = self.type()[0]
        if st in {'C', 'R'}:
            new_edge = [self[0].insert_argrole(argrole, pos)]
            new_edge += self[1:]
            return Hyperedge(new_edge)
        elif st == 'P':
            new_edge = [self[0], self[1].insert_argrole(argrole, pos)]
            return Hyperedge(new_edge)
        return self

    def insert_edge_with_argrole(self, edge, argrole, pos):
        """Returns a new edge with the provided edge and its argroles inserted
        at the specified position."""
        new_edge = self.insert_argrole(argrole, pos)
        new_edge = new_edge[:pos + 1] + (edge,) + new_edge[pos + 1:]
        return Hyperedge(new_edge)

    def edges_with_argrole(self, argrole):
        """Returns the list of edges with the given argument role."""
        edges = []
        connector = self[0]

        for pos, role in enumerate(connector.argroles()):
            if role == argrole:
                if pos < len(self) - 1:
                    edges.append(self[pos + 1])
        return edges

    def main_concepts(self):
        """Returns the list of main concepts in an concept edge.
        A main concept is a central concept in a built concept, e.g.:
        in ('s/Bp.am zimbabwe/Mp economy/Cn.s), economy/Cn.s is the main
        concept.

        If entity is not an edge, or its connector is not of type builder,
        or the builder does not contain concept role annotations, or no
        concept is annotated as the main one, then an empty list is
        returned.
        """

        concepts = []
        connector = self[0]
        if not connector.is_atom():
            return concepts
        if connector.type()[0] != 'B':
            return concepts

        return self.edges_with_argrole('m')

    def apply_vars(self, vars):
        return hedge([edge.apply_vars(vars) for edge in self])

    def __add__(self, other):
        if type(other) in {tuple, list}:
            return Hyperedge(super(Hyperedge, self).__add__(other))
        elif other.is_atom():
            return Hyperedge(super(Hyperedge, self).__add__((other,)))
        else:
            return Hyperedge(super(Hyperedge, self).__add__(other))

    def __str__(self):
        return self.to_str()

    def __repr__(self):
        return self.to_str()


class Atom(Hyperedge):
    """Atomic hyperedge."""
    def __new__(cls, edge):
        return super(Hyperedge, cls).__new__(cls, tuple(edge))

    def is_atom(self):
        """Checks if edge is an atom."""
        return True

    def parts(self):
        """Splits atom into its parts."""
        return self[0].split('/')

    def root(self):
        """Extracts the root of an atom
        (e.g. the root of graphbrain/C/1 is graphbrain)."""
        return self.parts()[0]

    def replace_atom_part(self, part_pos, part):
        """Build a new atom by replacing an atom part in a given atom."""
        parts = self.parts()
        parts[part_pos] = part
        atom = '/'.join([part for part in parts if part])
        return Atom((atom,))

    def to_str(self, roots_only=False):
        """Converts atom to its string representation.

        Keyword argument:
        roots_only -- only the roots of the atoms will be used to create
        the string representation.
        """
        if roots_only:
            return self.root()
        else:
            return self[0]

    def label(self):
        """Generate human-readable label from entity."""
        return self.root().replace('_', ' ')

    def atom(self):
        return self

    def atoms(self):
        """Returns the set of atoms contained in the edge.

        For example, consider the edge:
        (the/Md (of/Br mayor/Cc (the/Md city/Cs)))
        in this case, edge.atoms() returns:
        [the/Md, of/Br, mayor/Cc, city/Cs]
        """
        return {self}

    def all_atoms(self):
        """Returns a list of all the atoms contained in the edge. Unlike
        atoms(), which does not return repeated atoms, all_atoms() does
        return repeated atoms if they are different objects.

        For example, consider the edge:
        (the/Md (of/Br mayor/Cc (the/Md city/Cs)))
        in this case, edge.all_atoms() returns:
        [the/Md, of/Br, mayor/Cc, the/Md, city/Cs]
        """
        return [self]

    def depth(edge):
        """Returns maximal depth of edge, an atom has depth 0."""
        return 0

    def roots(self):
        """Returns edge with root-only atoms."""
        return Atom((self.root(),))

    def contains(self, needle, deep=False):
        """Checks if 'needle' is contained in edge.

        Keyword argument:
        deep -- search recursively (default False)"""
        return self[0] == needle

    def subedges(self):
        """Returns all the subedges contained in the edge, including atoms
        and itself.
        """
        return {self}

    def insert_first_argument(self, argument):
        """Returns an edge built by placing 'argument' as the first item
        after the connector of this edge. If this edge is an atom, then
        it becomes the connector of the returned edge.

        For example, considering the 'edge' (a) and the 'argument' (b), this
        function returns:
        (a b)

        Considering the 'edge' (a b c) and the 'argument' (d e), it
        returns:
        (a (d e) b c)
        """
        return Hyperedge((self, argument))

    def replace_atom(self, old, new):
        """Returns edge built by replacing every instance of 'old' in
        this edge with 'new'.
        """
        if self == old:
            return new
        else:
            return self

    def role(self):
        """Returns the role of this atom as a list of the subrole strings.

        The role of an atom is its second part, right after the root.
        A dot notation is used to separate the subroles. For example,
        the role of graphbrain/Cp.s/1 is:

            Cp.s

        For this case, this function returns:

            ['Cp', 'S']

        If the atom only has a root, it is assumed to be a concept.
        In this case, this function returns the role with just the
        generic concept type:

            ['C'].
        """
        parts = self[0].split('/')
        if len(parts) < 2:
            return list('C')
        else:
            return parts[1].split('.')

    def simplify_role(self):
        """Returns atom with a simplified role part. In the simplified role,
        only the type is specified.
        """
        parts = self.parts()
        if len(parts) > 1:
            parts[1] = self.type()[0]
        atom_str = '/'.join(parts)
        return Atom((atom_str,))

    def type(self):
        """Returns the type of the atom.

        The type of an atom is its first subrole. For example, the
        type of graphbrain/Cp.s/1 is 'Cp'.

        If the atom only has a root, it is assumed to be a concept.
        In this case, this function returns the generic concept type: 'C'.
        """
        return self.role()[0]

    def connector_type(self):
        """Returns the type of the edge's connector.
        If the edge has no connector (i.e. it's an atom), then the atom
        type is returned.
        """
        return self.type()

    def atom_with_type(self, atom_type):
        """Returns the first atom found in the edge that has the given
        'atom_type', or whose type starts with 'atom_type'.
        If no such atom is found, returns None.

        For example, given the edge (+/B a/Cn b/Bp) and the 'atom_type'
        C, this function returns:
        a/Cn
        If the 'atom_type' is 'Cp', the it will return:
        b/Cp
        """
        n = len(atom_type)
        et = self.type()
        if len(et) >= n and et[:n] == atom_type:
            return self
        else:
            return None

    def predicate(self):
        """Returns predicate atom if this edge is a non-atom of type
        relation or predicate. Returns itself if it is an atom of type
        predicate. Returns None otherwise.
        """
        if self.type()[0] == 'P':
            return self
        return None

    def is_pattern(self):
        """Check if this edge defines a pattern, i.e. if it includes at least
        one pattern matcher.

        Pattern matchers are:
        '*', '@', '&', '...' and variables (atom label starting with an
        uppercase letter)
        """
        return (self[0][0] in {'*', '@', '&'} or
                self[0][:3] == '...' or
                self[0][0].isupper())

    def is_full_pattern(self):
        """Check if every atom is a pattern matcher.

        Pattern matchers are:
        '*', '@', '&', '...' and variables (atom label starting with an
        uppercase letter)
        """
        return self.is_pattern()

    def argroles(self):
        """Returns the argument roles string of the edge, if it exists.
        Otherwise returns empty string.
        """
        et = self.type()[0]
        if et not in {'B', 'P'}:
            return ''
        role = self.role()
        if len(role) < 2:
            return ''
        return role[1]

    def replace_argroles(self, argroles):
        """Returns an atom with the argroles replaced with the provided string.
        Returns same atom if the atom does not contain a role part."""
        parts = self[0].split('/')
        if len(parts) < 2:
            return self
        role = parts[1].split('.')
        if len(role) < 2:
            role.append(argroles)
        else:
            role[1] = argroles
        parts = [parts[0], '.'.join(role)] + parts[2:]
        return Atom(('/'.join(parts),))

    def insert_argrole(self, argrole, pos):
        """Returns an atom with the given argrole inserted at the specified
        position. Same restrictions as in replace_argroles() apply."""
        argroles = self.argroles()
        argroles = argroles[:pos] + argrole + argroles[pos:]
        return self.replace_argroles(argroles)

    def edges_with_argrole(self, argrole):
        """Returns the list of edges with the given argument role"""
        return []

    def main_concepts(self):
        """Returns the list of main concepts in an concept edge.
        A main concept is a central concept in a built concept, e.g.:
        in ('s/Bp.am zimbabwe/Mp economy/Cn.s), economy/Cn.s is the main
        concept.

        If entity is not an edge, or its connector is not of type builder,
        or the builder does not contain concept role annotations, or no
        concept is annotated as the main one, then an empty list is
        returned.
        """
        return []

    def apply_vars(self, vars):
        if self.is_pattern():
            varname = _varname(self)
            if len(varname) > 0 and varname in vars:
                return vars[varname]
        return self

    def __add__(self, other):
        if type(other) in {tuple, list}:
            return Hyperedge((self,) + other)
        elif other.is_atom():
            return Hyperedge((self, other))
        else:
            return Hyperedge((self,) + other)
