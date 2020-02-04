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


def match_pattern(edge, pattern):
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

    (1) the edge: (is/pd (my/mp name/cn) mary/cp)
    applied to the pattern: (is/pd (my/mp name/cn) \*name)
    produces the result: {'name', mary/cp}

    (2) the edge: (is/pd (my/mp name/cn) mary/cp)
    applied to the pattern: (is/pd (my/mp name/cn) &name)
    produces the result: {}

    (3) the edge: (is/pd (my/mp name/cn) mary/cp)
    applied to the pattern: (is/pd @ \*name)
    produces the result: None
    """

    edge = hedge(edge)
    pattern = hedge(pattern)

    # open ended?
    if not pattern.is_atom() and pattern[-1].to_str() == '...':
        pattern = pattern[:-1]
        if len(edge) < len(pattern):
            return None
    else:
        if len(edge) != len(pattern):
            return None

    vars = {}
    for i, pitem in enumerate(pattern):
        eitem = edge[i]
        if pitem.is_atom():
            if len(pitem.parts()) == 1:
                pitem_str = pitem.to_str()
                if pitem_str[0] == '@':
                    if eitem.is_atom():
                        if len(pitem_str) > 1:
                            vars[pitem_str[1:]] = eitem
                    else:
                        return None
                elif pitem_str[0] == '&':
                    if eitem.is_atom():
                        return None
                    else:
                        if len(pitem_str) > 1:
                            vars[pitem_str[1:]] = eitem
                elif pitem_str[0] == '*':
                    if len(pitem_str) > 1:
                        vars[pitem_str[1:]] = eitem
                else:
                    if eitem != pitem:
                        return None
            elif eitem != pitem:
                return None
        else:
            if eitem.is_atom():
                return None
            else:
                sub_vars = match_pattern(eitem, pitem)
                if sub_vars:
                    vars = {**vars, **sub_vars}
                elif type(sub_vars) != dict:
                    return None
    return vars


def edge_matches_pattern(edge, pattern):
    """Check if an edge matches a pattern.

    Patterns are themselves edges. They can match families of edges
    by employing special atoms:

    -> '*' represents a general wildcard (matches any entity)

    -> '@' represents an atomic wildcard (matches any atom)

    -> '&' represents an edge wildcard (matches any edge)

    -> '...' at the end indicates an open-ended pattern.

    The pattern can be any valid hyperedge, including the above special atoms.
    Examples: (is/pd graphbrain/c @)
    (says/pd * ...)
    """
    return match_pattern(edge, pattern) is not None


def rel_arg_role(relation, position):
    """Returns argument role of argument in a given 'position' of 'relation'.
    Returns None if argument role cannot be determined.

    Example:
    The argument role of argument at position 0 in:
    (is/pd.sc graphbrain/c great/c)
    is:
    s
    """
    if relation.type()[0] != 'r':
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
        edge_str = source.replace('\n', ' ')
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
        elif self[0][0][:3] == '+/b':
            edge = self[1:]
        else:
            edge = (self[1], self[0]) + self[2:]
        return ' '.join([item.label() for item in edge])

    def atoms(self):
        """Returns set of atoms contained in edge."""
        atom_set = set()
        for item in self:
            for atom in item.atoms():
                atom_set.add(atom)
        return atom_set

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
        (is/pd.so graphbrain/cp.s great/c) has type 'rd'
        (red/m shoes/cn.p) has type 'cn'
        (before/tt noon/c) has type 'st'
        """
        ptype = self[0].type()
        if ptype[0] == 'p':
            outter_type = 'r'
        elif ptype[0] in {'a', 'm', 'w'}:
            return self[1].type()
        elif ptype[0] == 'x':
            outter_type = 'd'
        elif ptype[0] == 't':
            outter_type = 's'
        else:
            return 'c'

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

        For example, given the edge (+/b a/cn b/cp) and the 'atom_type'
        c, this function returns:
        a/cn
        If the 'atom_type' is 'cp', the it will return:
        b/cp
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
        if et == 'r':
            return self[0].predicate()
        elif et == 'p':
            return self[1].predicate()
        return None

    def is_pattern(self):
        """Check if this edge defines a pattern, i.e. if it includes at least
        one pattern matcher.

        Pattern matchers are:
        '*', '@', '&'' and '...'
        """
        return any(item.is_pattern() for item in self)

    def is_full_pattern(self):
        """Check if every atom is a pattern matcher.

        Pattern matchers are:
        '*', '@', '&'' and '...'
        """
        return all(item.is_full_pattern() for item in self)

    def argroles(self):
        """Returns the argument roles string of the edge, if it exists.
        Otherwise returns empty string.
        """
        et = self.type()[0]
        if et not in {'b', 'p'}:
            return ''
        return self[1].argroles()

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
        in ('s/bp.am zimbabwe/mp economy/cn.s), economy/cn.s is the main
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
        if connector.type()[0] != 'b':
            return concepts

        return self.edges_with_argrole('m')

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
        (e.g. the root of graphbrain/c/1 is graphbrain)."""
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

    def atoms(self):
        """Returns set of atoms contained in edge."""
        return {self}

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
        the role of graphbrain/cp.s/1 is:

            cp.s

        For this case, this function returns:

            ['cp', 's']

        If the atom only has a root, it is assumed to be a concept.
        In this case, this function returns the role with just the
        generic concept type:

            ['c'].
        """
        parts = self[0].split('/')
        if len(parts) < 2:
            return list('c')
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
        type of graphbrain/cp.s/1 is 'cp'.

        If the atom only has a root, it is assumed to be a concept.
        In this case, this function returns the generic concept type: 'c'.
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

        For example, given the edge (+/b a/cn b/cp) and the 'atom_type'
        c, this function returns:
        a/cn
        If the 'atom_type' is 'cp', the it will return:
        b/cp
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
        if self.type()[0] == 'p':
            return self
        return None

    def is_pattern(self):
        """Check if this edge defines a pattern, i.e. if it includes at least
        one pattern matcher.

        Pattern matchers are:
        '*', '@', '&'' and '...'
        """
        return (len(self.parts()) == 1 and
                (self[0][0] in {'*', '@', '&'} or self[0] == '...'))

    def is_full_pattern(self):
        """Check if every atom is a pattern matcher.

        Pattern matchers are:
        '*', '@', '&'' and '...'
        """
        return self.is_pattern()

    def argroles(self):
        """Returns the argument roles string of the edge, if it exists.
        Otherwise returns empty string.
        """
        et = self.type()[0]
        if et not in {'b', 'p'}:
            return ''
        role = self.role()
        if len(role) < 2:
            return ''
        return role[1]

    def edges_with_argrole(self, argrole):
        """Returns the list of edges with the given argument role"""
        return []

    def main_concepts(self):
        """Returns the list of main concepts in an concept edge.
        A main concept is a central concept in a built concept, e.g.:
        in ('s/bp.am zimbabwe/mp economy/cn.s), economy/cn.s is the main
        concept.

        If entity is not an edge, or its connector is not of type builder,
        or the builder does not contain concept role annotations, or no
        concept is annotated as the main one, then an empty list is
        returned.
        """
        return []

    def __add__(self, other):
        if type(other) in {tuple, list}:
            return Hyperedge((self,) + other)
        elif other.is_atom():
            return Hyperedge((self, other))
        else:
            return Hyperedge((self,) + other)
