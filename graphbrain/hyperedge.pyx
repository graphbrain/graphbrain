argrole_order = {
    'm': -1,
    's': 0,
    'p': 1,
    'a': 2,
    'c': 3,
    'o': 4,
    'i': 5,
    't': 6,
    'j': 7,
    'x': 8,
    'r': 9,
    '?': 10
}


def str2atom(s):
    """Converts a string into a valid atom."""
    atom = s.lower() 

    atom = atom.replace('%', '%25')
    atom = atom.replace('/', '%2f')
    atom = atom.replace(' ', '%20')
    atom = atom.replace('(', '%28')
    atom = atom.replace(')', '%29')
    atom = atom.replace('.', '%2e')
    atom = atom.replace('*', '%2a')
    atom = atom.replace('&', '%26')
    atom = atom.replace('@', '%40')
    atom = atom.replace('\n', '%0a')
    atom = atom.replace('\r', '%0d')

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


def _parsed_token(token):
    if _edge_str_has_outer_parens(token):
        return hedge(token)
    else:
        return Atom((token,))


def hedge(source):
    """Create a hyperedge."""
    cdef str edge_str
    cdef str edge_inner_str
    if type(source) in {tuple, list}:
        return Hyperedge(tuple(hedge(item) for item in source))
    elif type(source) is str:
        edge_str = source.strip().replace('\n', ' ')
        edge_inner_str = edge_str

        parens = _edge_str_has_outer_parens(edge_str)
        if parens:
            edge_inner_str = edge_str[1:-1]

        tokens = split_edge_str(edge_inner_str)
        if not tokens:
            return None
        edges = tuple(_parsed_token(token) for token in tokens)
        if len(edges) > 1 or type(edges[0]) == Hyperedge:
            return Hyperedge(edges)
        elif len(edges) > 0:
            return Atom(edges[0], parens)
        else:
            return None
    elif type(source) in {Hyperedge, Atom, UniqueAtom}:
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

    @property
    def atom(self):
        """True if edge is an atom."""
        return False

    @property
    def not_atom(self):
        """True if edge is not an atom."""
        return True

    @property
    def t(self):
        """ Edge type.
        (this porperty is a shortcut for Hyperedge.type())
        """
        return self.type()

    @property
    def mt(self):
        """ Edge main type.
        (this porperty is a shortcut for Hyperedge.mtype())
        """
        return self.mtype()
    
    @property
    def ct(self):
        """ Edge connector type.
        (this porperty is a shortcut for Hyperedge.connector_type())
        """
        return self.connector_type()

    @property
    def cmt(self):
        """ Edge connector main type.
        (this porperty is a shortcut for Hyperedge.mconnector_type())
        """
        return self.connector_mtype()

    def is_atom(self):
        """
        .. deprecated:: 0.6.0
            Please use the properties .atom and .not_atom instead.

        Checks if edge is an atom.
        """
        return False

    def to_str(self, roots_only=False):
        """Converts edge to its string representation.

        Keyword argument:
        roots_only -- only the roots of the atoms will be used to create
        the string representation.
        """
        s = ' '.join([edge.to_str(roots_only=roots_only) for edge in self if edge])
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

    def inner_atom(self):
        """The inner atom inside of a modifier structure.

        For example, condider:
        (red/M shoes/C)
        The inner atom is:
        shoes/C
        Or, the more complex case:
        ((and/J slow/M steady/M) go/P)
        Yields:
        gp/P

        This method should not be used on structures that contain more than
        one inner atom, for example concepts constructed with builders or
        relations.

        The inner atom of an atom is itself.
        """
        return self[1].inner_atom()

    def connector_atom(self):
        """The inner atom of the connector.

        For example, condider:
        (does/M (not/M like/P.so) john/C chess/C)
        The connector atom is:
        like/P.so

        The connector atom of an atom is None.
        """
        return self[0].inner_atom()

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

    def size(self):
        """The size of an edge is its total number of atoms, at all depths."""
        return sum([edge.size() for edge in self])

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

    def replace_atom(self, old, new, unique=False):
        """Returns edge built by replacing every instance of 'old' in
        this edge with 'new'.

        Keyword argument:
        unique -- match only the exact same instance of the atom, i.e.
        UniqueAtom(self) == UniqueAtom(old) (default: False)
        """
        return Hyperedge(tuple(item.replace_atom(old, new, unique=unique) for item in self))

    def simplify(self, subtypes=False, argroles=False, namespaces=True):
        """Returns a version of the edge with simplified atoms, for example
        removing subtypes, subroles or namespaces.

        Keyword arguments:
        subtypes -- include subtypes (default: False).
        argroles --include argroles (default: False).
        namespaces -- include namespaces (default: True).
        """
        return hedge([subedge.simplify(subtypes=subtypes,
                                       argroles=argroles,
                                       namespaces=namespaces)
                      for subedge in self])

    def type(self):
        """Returns the type of this edge as a string.
        Type inference is performed.
        """
        ptype = self[0].type()
        if ptype[0] == 'P':
            outter_type = 'R'
        elif ptype[0] == 'M':
            return self[1].type()
        elif ptype[0] == 'T':
            outter_type = 'S'
        elif ptype[0] == 'B':
            outter_type = 'C'
        elif ptype[0] == 'J':
            return self[1].mtype()
        else:
            raise RuntimeError('Edge is malformed, type cannot be determined: {}'.format(str(self)))

        return '{}{}'.format(outter_type, ptype[1:])

    def connector_type(self):
        """Returns the type of the edge's connector.
        If the edge has no connector (i.e. it's an atom), then None is
        returned.
        """
        return self[0].type()

    def mtype(self):
        """Returns the main type of this edge as a string of one character.
        Type inference is performed.
        """
        return self.type()[0]

    def connector_mtype(self):
        """Returns the main type of the edge's connector.
        If the edge has no connector (i.e. it's an atom), then None is
        returned.
        """
        ct = self.connector_type()
        if ct:
            return ct[0]
        else:
            return None

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

    def argroles(self):
        """Returns the argument roles string of the edge, if it exists.
        Otherwise returns empty string.
        
        Argument roles can be return for the entire edge that they apply to,
        which can be a relation (R) or a concept (C). For example:

        ((not/M is/P.sc) bob/C sad/C) has argument roles "sc",
        (of/B.ma city/C berlin/C) has argument roles "ma".

        Argument roles can also be returned for the connectors that define 
        the outer edge, which can be of type predicate (P) or builder (B). For
        example:

        (not/M is/P.sc) has argument roles "sc",
        of/B.ma has argument roles "ma".
        """
        et = self.mtype()
        if et in {'R', 'C'} and self[0].mtype() in {'B', 'P'}:
            return self[0].argroles()
        if et not in {'B', 'P'}:
            return ''
        return self[1].argroles()

    def replace_argroles(self, argroles):
        """Returns an edge with the argroles of the connector atom replaced
        with the provided string.
        Returns same edge if the atom does not contain a role part."""
        st = self.mtype()
        if st in {'C', 'R'}:
            new_edge = [self[0].replace_argroles(argroles)]
            new_edge += self[1:]
            return Hyperedge(new_edge)
        elif st in {'P', 'B'}:
            new_edge = [self[0], self[1].replace_argroles(argroles)]
            new_edge += list(self[2:])
            return Hyperedge(new_edge)
        return self

    def insert_argrole(self, argrole, pos):
        """Returns an edge with the given argrole inserted at the specified
        position in the argroles of the connector atom.
        Same restrictions as in replace_argroles() apply."""
        st = self.mtype()
        if st in {'C', 'R'}:
            new_edge = [self[0].insert_argrole(argrole, pos)]
            new_edge += self[1:]
            return Hyperedge(new_edge)
        elif st in {'P', 'B'}:
            new_edge = [self[0], self[1].insert_argrole(argrole, pos)]
            new_edge += list(self[2:])
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

        argroles = connector.argroles()
        if len(argroles) > 0 and argroles[0] == '{':
            argroles = argroles[1:-1]
        argroles = argroles.replace(',', '')
        for pos, role in enumerate(argroles):
            if role == argrole:
                if pos < len(self) - 1:
                    edges.append(self[pos + 1])
        return edges

    def main_concepts(self):
        """Returns the list of main concepts in an concept edge.
        A main concept is a central concept in a built concept, e.g.:
        in ('s/Bp.am zimbabwe/Cp economy/Cn.s), economy/Cn.s is the main
        concept.

        If entity is not an edge, or its connector is not of type builder,
        or the builder does not contain concept role annotations, or no
        concept is annotated as the main one, then an empty list is
        returned.
        """
        if self[0].mtype() == 'B':
            return self.edges_with_argrole('m')
        return []

    def replace_main_concept(self, new_main):
        """TODO: document and test"""
        if self.mtype() != 'C':
            return None
        if self[0].mtype() == 'M':
            return hedge((self[0], new_main))
        elif self[0].mtype() == 'B':
            if len(self) == 3:
                if self[0].argroles() == 'ma':
                    return hedge((self[0], new_main, self[2]))
                elif self[0].argroles() == 'am':
                    return hedge((self[0], self[1], new_main))
        return None

    def check_correctness(self):
        output = {}
        errors = []

        ct = self[0].mtype()
        # check if connector has valid type
        if ct not in {'P', 'M', 'B', 'T', 'J'}:
            errors.append('connector has incorrect type: {}'.format(ct))
        # check if modifier structure is correct
        if ct == 'M':
            if len(self) != 2:
                errors.append('modifiers can only have one argument')
        # check if builder structure is correct
        elif ct == 'B':
            if len(self) != 3:
                errors.append('builders can only have two arguments')
            for arg in self[1:]:
                at = arg.mtype()
                if at != 'C':
                    e = 'builder argument {} has incorrect type: {}'.format(arg.to_str(), at)
                    errors.append(e)
        # check if trigger structure is correct
        elif ct == 'T':
            if len(self) != 2:
                errors.append('triggers can only have one arguments')
            for arg in self[1:]:
                at = arg.mtype()
                if at not in {'C', 'R'}:
                    e = 'trigger argument {} has incorrect type: {}'.format(arg.to_str(), at)
                    errors.append(e)
        # check if predicate structure is correct
        elif ct == 'P':
            for arg in self[1:]:
                at = arg.mtype()
                if at not in {'C', 'R', 'S'}:
                    e = 'predicate argument {} has incorrect type: {}'.format(arg.to_str(), at)
                    errors.append(e)
        # check if conjunction structure is correct
        elif ct == 'J':
            if len(self) < 3:
                errors.append('conjunctions must have at least two arguments')

        if len(errors) > 0:
            output[self] = errors

        for subedge in self:
            output.update(subedge.check_correctness())

        return output

    def normalized(self):
        edge = self
        conn = edge[0]
        ar = conn.argroles()
        if ar != '':
            if ar[0] == '{':
                ar = ar[1:-1]
            roles_edges = zip(ar, edge[1:])
            roles_edges = sorted(roles_edges, key=lambda role_edge: argrole_order[role_edge[0]])
            edge = hedge([conn] + list(role_edge[1] for role_edge in roles_edges))
        return hedge([subedge.normalized() for subedge in edge])

    def __add__(self, other):
        if type(other) in {tuple, list}:
            return Hyperedge(super(Hyperedge, self).__add__(other))
        elif other.atom:
            return Hyperedge(super(Hyperedge, self).__add__((other,)))
        else:
            return Hyperedge(super(Hyperedge, self).__add__(other))

    def __str__(self):
        return self.to_str()

    def __repr__(self):
        return self.to_str()


class Atom(Hyperedge):
    """Atomic hyperedge."""
    def __new__(cls, edge, parens=False):
        atom = super(Hyperedge, cls).__new__(cls, tuple(edge))
        atom.parens = parens
        return atom

    @property
    def atom(self):
        """True if edge is an atom."""
        return True

    @property
    def not_atom(self):
        """True if edge is not an atom."""
        return False    

    def is_atom(self):
        """
        .. deprecated:: 0.6.0
            Please use the properties .atom and .not_atom instead.

        Checks if edge is an atom.
        """
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
            atom_str = self.root()
        else:
            atom_str = str(self[0])
        if self.parens:
            return '({})'.format(atom_str)
        else:
            return atom_str

    def label(self):
        """Generate human-readable label from entity."""
        label = self.root()

        label = label.replace('%25', '%')
        label = label.replace('%2f', '/')
        label = label.replace('%20', ' ')
        label = label.replace('%28', '(')
        label = label.replace('%29', ')')
        label = label.replace('%2e', '.')
        label = label.replace('%2a', '*')
        label = label.replace('%26', '&')
        label = label.replace('%40', '@')

        return label

    def inner_atom(self):
        """The inner atom inside of a modifier structure.

        For example, condider:
        (red/M shoes/C)
        The inner atom is:
        shoes/C
        Or, the more complex case:
        ((and/J slow/M steady/M) go/P)
        Yields:
        gp/P

        This method should not be used on structures that contain more than
        one inner atom, for example concepts constructed with builders or
        relations.

        The inner atom of an atom is itself.
        """
        return self

    def connector_atom(self):
        """The inner atom of the connector.

        For example, condider:
        (does/M (not/M like/P.so) john/C chess/C)
        The connector atom is:
        like/P.so

        The connector atom of an atom is None.
        """
        return None

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

    def size(self):
        """The size of an edge is its total number of atoms, at all depths."""
        return 1

    def depth(self):
        """Returns maximal depth of edge, an atom has depth 0."""
        return 0

    def roots(self):
        """Returns edge with root-only atoms."""
        return Atom((self.root(),))

    def contains(self, needle, deep=False):
        """Checks if 'needle' is contained in edge.

        Keyword argument:
        deep -- search recursively (default: False)"""
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

    def replace_atom(self, old, new, unique=False):
        """Returns edge built by replacing every instance of 'old' in
        this edge with 'new'.

        Keyword argument:
        unique -- match only the exact same instance of the atom, i.e.
        UniqueAtom(self) == UniqueAtom(old) (default: False)
        """
        if unique:
            if UniqueAtom(self) == UniqueAtom(old):
                return new
        else:
            if self == old:
                return new
        return self

    def role(self):
        """Returns the role of this atom as a list of the subrole strings.

        The role of an atom is its second part, right after the root.
        A dot notation is used to separate the subroles. For example,
        the role of graphbrain/Cp.s/1 is:

            Cp.s

        For this case, this function returns:

            ['Cp', 's']

        If the atom only has a root, it is assumed to be a conjunction.
        In this case, this function returns the role with just the
        generic conjunction type:

            ['J'].
        """
        parts = self[0].split('/')
        if len(parts) < 2:
            return list('J')
        else:
            return parts[1].split('.')

    def simplify(self, subtypes=False, argroles=False, namespaces=True):
        """Returns a simplified version of the atom, for example removing
        subtypes, subroles or namespaces.

        Keyword arguments:
        subtypes -- include subtype (default: False).
        argroles --include argroles (default: False).
        namespaces -- include namespaces (default: True).
        """
        parts = self.parts()

        if len(parts) < 2:
            return self

        if subtypes:
            role = self.type()
        else:
            role = self.mtype()

        if argroles:
            ar = self.argroles()
            if len(ar) > 0:
                role = '{}.{}'.format(role, ar)

        parts[1] = role

        if len(parts) > 2 and not namespaces:
            parts = parts[:2]

        atom_str = '/'.join(parts)
        return Atom((atom_str,))

    def type(self):
        """Returns the type of the atom.

        The type of an atom is its first subrole. For example, the
        type of graphbrain/Cp.s/1 is 'Cp'.

        If the atom only has a root, it is assumed to be a conjunction.
        In this case, this function returns the generic conjunction type: 'J'.
        """
        return self.role()[0]

    def connector_type(self):
        """Returns the type of the edge's connector.
        If the edge has no connector (i.e. it's an atom), then None is
        returned.
        """
        return None

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

    def argroles(self):
        """Returns the argument roles string of the edge, if it exists.
        Otherwise returns empty string.
        
        Argument roles can be return for the entire edge that they apply to,
        which can be a relation (R) or a concept (C). For example:

        ((not/M is/P.sc) bob/C sad/C) has argument roles "sc",
        (of/B.ma city/C berlin/C) has argument roles "ma".

        Argument roles can also be returned for the connectors that define 
        the outer edge, which can be of type predicate (P) or builder (B). For
        example:

        (not/M is/P.sc) has argument roles "sc",
        of/B.ma has argument roles "ma".
        """
        et = self.mtype()
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

    def replace_main_concept(self, new_main):
        """TODO: document and test"""
        if self.mtype() != 'C':
            return None

        return new_main

    def check_correctness(self):
        output = {}
        errors = []

        at = self.mtype()
        if at not in {'C', 'P', 'M', 'B', 'T', 'J'}:
            errors.append('{} is not a valid atom type'.format(at))

        if len(errors) > 0:
            output[self] = errors

        return output

    def normalized(self):
        if self.mtype() in {'B', 'P'}:
            ar = self.argroles()
            if len(ar) > 0:
                if ar[0] == '{':
                    ar = ar[1:-1]
                    unordered = True
                else:
                    unordered = False
                ar = ''.join(sorted(ar, key=lambda argrole: argrole_order[argrole]))
                if unordered:
                    ar = '{{{}}}'.format(ar)
                return self.replace_argroles(ar)
        return self

    def __add__(self, other):
        if type(other) in {tuple, list}:
            return Hyperedge((self,) + other)
        elif other.atom:
            return Hyperedge((self, other))
        else:
            return Hyperedge((self,) + other)


class UniqueAtom(Atom):
    def __init__(self, atom):
        self.atom_obj = atom

    def __hash__(self):
        return id(self.atom_obj)

    def __eq__(self, other):
        return id(self.atom_obj) == id(other.atom_obj)


def unique(edge):
    if edge.atom:
        if type(edge) == UniqueAtom:
            return edge
        else:
            return UniqueAtom(edge)
    else:
        return hedge([unique(subedge) for subedge in edge])


def non_unique(edge):
    if edge.atom:
        if type(edge) == UniqueAtom:
            return edge.atom_obj
        else:
            return edge
    else:
        return hedge([non_unique(subedge) for subedge in edge])
