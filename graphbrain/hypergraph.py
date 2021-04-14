import graphbrain.constants as const
from graphbrain.hyperedge import hedge, Hyperedge, build_atom
from graphbrain.logic import eval_rule


class Hypergraph(object):
    """Hypergraph interface."""

    # ================================================================
    # Interface abstract methods, to be implemented in derived classes
    # ================================================================

    def close(self):
        """Closes the hypergraph."""
        raise NotImplementedError()

    def name(self):
        """Returns name of the hypergraph."""
        raise NotImplementedError()

    def destroy(self):
        """Erase the entire hypergraph."""
        raise NotImplementedError()

    def all(self):
        """Returns a generator of all the edges."""
        raise NotImplementedError()

    def all_attributes(self):
        """Returns a generator with a tuple for each edge.
           The first element of the tuple is the edge itself,
           the second is a dictionary of attribute values
           (as strings)."""
        raise NotImplementedError()

    def atom_count(self):
        """Returns total number of atoms."""
        raise NotImplementedError()

    def edge_count(self):
        """Returns total number of edges."""
        raise NotImplementedError()

    def primary_atom_count(self):
        """Returns number of primary atoms."""
        raise NotImplementedError()

    def primary_edge_count(self):
        """Returns number of primary edges."""
        raise NotImplementedError()

    # ============================
    # High-level interface methods
    # ============================

    def all_atoms(self):
        """Returns a generator of all the atoms."""
        for edge in self.all():
            if edge.is_atom():
                yield edge

    def all_non_atoms(self):
        """Returns a generator of all the edges that are not atoms."""
        for edge in self.all():
            if not edge.is_atom():
                yield edge

    def exists(self, edge):
        """Checks if the given edge exists."""
        return self._exists(hedge(edge))

    def add(self, edge, primary=True, count=False):
        """Adds an edge if it does not exist yet, returns same edge.
        All children are recursively added as non-primary edge, for
        indexing purposes.

        Edges can be passed in both Hyperedge or string format.

        Keyword arguments:
        primary -- edge is primary, meaning, for example, that it counts
        towards degrees. Non-primary edges are used for indexing purposes,
        for example to make it easy to find the subedges contained in primary
        edges when performing queries.
        count -- an integer counter attribute is added to the edge. If the
        edge already exists, the counter is incremented.
        """
        if isinstance(edge, Hyperedge):
            if edge.is_atom():
                return edge
            else:
                # recursively add all sub-edges as non-primary edges.
                for child in edge:
                    self.add(child, primary=False)
                # add entity itself
                self._add(edge, primary=primary)

                # increment counter if requested
                if count:
                    self.inc_attribute(edge, 'count')

                return edge
        elif edge:
            return self.add(hedge(edge), primary=primary, count=count)
        return None

    def remove(self, edge, deep=False):
        """Removes an edge.

        Keyword argument:
        deep -- recursively remove all subedges (default False)
        """
        self._remove(hedge(edge), deep=deep)

    def is_primary(self, edge):
        """Check if an edge is primary."""
        return self._is_primary(hedge(edge))

    def set_primary(self, edge, value):
        """Make edge primary if value is True, make it non-primary
        otherwise.
        """
        self._set_primary(hedge(edge), value)

    def search(self, pattern, strict=True):
        """Returns generator for all the edges that match a pattern.

        Patterns are themselves edges. They can match families of edges
        by employing special atoms:
        -> '*' represents a general wildcard (matches any edge)
        -> '.' represents an atomic wildcard (matches any atom)
        -> '(*)' represents a non-atomic wildcard (matches any non-atom)
        -> '...' at the end indicates an open-ended pattern.

        The pattern can be a string, that must represent an edge.
        Examples: '(is/Pd graphbrain/C .)'
        '(says/pd * ...)'

        Atomic patterns can also be used to match all edges in the
        hypergraph: *, all atoms: ., and all non-atoms: (*).

        Keyword argument:
        strict -- strictly match the search pattern, or allow for more general
        atoms to match target atome (e.g. plays/P matches plays/Pd.so in
        non-strict mode, but only exactl plays/Pd.so matches it in strict mode)
        Non-strict mode is slower. (default True)
        """
        pattern = hedge(pattern)

        if pattern.is_atom() and len(pattern.parts()) == 1:
            if pattern.parens:
                return self.all_non_atoms()
            elif pattern[0][0] == '*':
                return self.all()
            elif pattern[0][0] == '.':
                return self.all_atoms()

        return self._search(pattern, strict=strict)

    def match(self, pattern, strict=True, curvars={}):
        pattern = hedge(pattern)
        return self._match(pattern, strict=strict, curvars=curvars)

    def eval(self, rule):
        return eval_rule(self, rule)

    def count(self, pattern, strict=True):
        """Number of edges that match a pattern.
        See search() method for an explanation of patterns.
        """
        pattern = hedge(pattern)

        if pattern.is_atom() and len(pattern.parts()) == 1:
            if pattern.parens:
                return self.edge_count() - self.atom_count()
            elif pattern[0][0] == '*':
                return self.edge_count()
            elif pattern[0][0] == '.':
                return self.atom_count()

        n = 0
        for _ in self._search(pattern, strict=strict):
            n += 1
        return n

    def star(self, center, limit=None):
        """Returns generator of the edges that contain the center.

        Keyword argument:
        limit -- maximum number of results to return, infinite if None
        """
        return self._star(hedge(center), limit=limit)

    def atoms_with_root(self, root):
        """Returns generator of all atoms with the given root."""
        if len(root) == 0:
            return {}
        return self._atoms_with_root(root)

    def edges_with_edges(self, edges, root=None):
        """Returns generator of all edges containing the given edges,
        and optionally a given root.

        Keyword argument:
        root -- edge must also contain an atom with this root (default None)
        """
        return self._edges_with_edges(edges, root)

    def set_attribute(self, edge, attribute, value):
        """Sets the value of an attribute."""
        return self._set_attribute(hedge(edge), attribute, value)

    def inc_attribute(self, edge, attribute):
        """Increments an attribute of an entity, sets initial value to 1
        if attribute does not exist.
        """
        return self._inc_attribute(hedge(edge), attribute)

    def dec_attribute(self, edge, attribute):
        """Increments an attribute of an entity."""
        return self._dec_attribute(hedge(edge), attribute)

    def get_str_attribute(self, edge, attribute, or_else=None):
        """Returns attribute as string.

        Keyword argument:
        or_else -- value to return if the entity does not have the given
        attribute. (default None)
        """
        return self._get_str_attribute(hedge(edge), attribute, or_else=or_else)

    def get_int_attribute(self, edge, attribute, or_else=None):
        """Returns attribute as integer value.

        or_else -- value to return if the entity does not have
                   the give attribute. (default None)
        """
        return self._get_int_attribute(hedge(edge), attribute, or_else=or_else)

    def get_float_attribute(self, edge, attribute, or_else=None):
        """Returns attribute as float value.

        or_else -- value to return if the entity does not have
                   the give attribute. (default None)
        """
        return self._get_float_attribute(hedge(edge), attribute,
                                         or_else=or_else)

    def degree(self, edge):
        """Returns the degree of an entity."""
        return self._degree(hedge(edge))

    def deep_degree(self, edge):
        """Returns the deep degree of an entity."""
        return self._deep_degree(hedge(edge))

    def ego(self, center):
        """Returns all atoms directly connected to center
           by hyperedges.
        """
        edges = self.star(center)
        atom_set = set()
        for edge in edges:
            for atom in edge.atoms():
                atom_set.add(atom)
        return atom_set

    def remove_by_pattern(self, pattern):
        """Removes all edges that match the pattern."""
        edges = self.search(pattern)
        for edge in edges:
            self.remove(edge)

    def root_degrees(self, edge):
        """Finds all the atoms that share the same root as the given edge
        and computes the sum of both their degrees and deep degrees.
        These two sums are returned.

        If the parameter edge is non-atomic, this function simply returns
        the degree and deep degree of that edge.
        """
        if edge.is_atom():
            atoms = tuple(self.atoms_with_root(edge.root()))
            d = sum([self.degree(atom) for atom in atoms])
            dd = sum([self.deep_degree(atom) for atom in atoms])
            return d, dd
        else:
            return self.degree(edge), self.deep_degree(edge)

    def sum_degree(self, edges):
        """Returns sum of the degrees of all edges contained in the parameter.
        """
        return sum([self.degree(edge) for edge in edges])

    def sum_deep_degree(self, edges):
        """Returns sum of the deep degrees of all edges contained in the
        parameter.
        """
        return sum([self.deep_degree(edge) for edge in edges])

    def add_to_sequence(self, name, pos, edge):
        """Adds 'edge' to sequence 'name' at position 'pos'."""
        seq_atom = build_atom(name, [])
        return self.add((const.sequence_pred, seq_atom, str(pos), edge))

    def sequence(self, name):
        """Returns an iterator for a sequence of hyperedges, given the name
        of the sequence.
        """

        pos = 0
        stop = False
        while not stop:
            iter = self.search((const.sequence_pred, name, str(pos), '*'))
            next_edge = next(iter, None)
            if next_edge:
                yield next_edge[3]
                pos += 1
            else:
                stop = True

    def sequences(self):
        """Returns an iterator for all the sequence names present in the
        hypergraph.
        """
        for edge in self.search((const.sequence_pred, '*', '0', '*')):
            yield edge[1]

    # ==============================================================
    # Private abstract methods, to be implemented in derived classes
    # ==============================================================

    def _exists(self, edge):
        raise NotImplementedError()

    def _add(self, edge, primary):
        raise NotImplementedError()

    def _remove(self, edge, deep):
        raise NotImplementedError()

    def _is_primary(self, edge):
        raise NotImplementedError()

    def _set_primary(self, edge, value):
        raise NotImplementedError()

    def _search(self, pattern):
        raise NotImplementedError()

    def _match(self, pattern, curvars={}):
        raise NotImplementedError()

    def _star(self, center, limit=None):
        raise NotImplementedError()

    def _atoms_with_root(self, root):
        raise NotImplementedError()

    def _edges_with_edges(self, edges, root):
        raise NotImplementedError()

    def _set_attribute(self, edge, attribute, value):
        raise NotImplementedError()

    def _inc_attribute(self, edge, attribute):
        raise NotImplementedError()

    def _dec_attribute(self, edge, attribute):
        raise NotImplementedError()

    def _get_str_attribute(self, edge, attribute, or_else=None):
        raise NotImplementedError()

    def _get_int_attribute(self, edge, attribute, or_else=None):
        raise NotImplementedError()

    def _get_float_attribute(self, edge, attribute, or_else=None):
        raise NotImplementedError()

    def _degree(self, edge):
        raise NotImplementedError()

    def _deep_degree(self, edge):
        raise NotImplementedError()
