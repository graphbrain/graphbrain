from graphbrain.hyperedge import *


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
        return self._exists(edge)

    def add(self, edge, primary=True):
        """Adds an edge if it does not exist yet, returns same edge.
        All children are recursively added as non-primary edge, for
        indexing purposes.

        Edges can be passed in both Hyperedge or string format.

        Keyword argument:
        primary -- edge is primary, meaning, for example, that it counts
                   towards degrees. Non-primary edges are used for
                   indexing purposes, for example to make it easy to find
                   the subedges contained in primary edges when performing
                   queries.
        """
        if isinstance(edge, Hyperedge):
            if edge.is_atom():
                return edge
            else:
                # recursively add all sub-edges as non-primary edges.
                for child in edge:
                    self.add(child, primary=False)
                # add entity itself
                return self._add(edge, primary=primary)
        else:
            return self.add(hedge(edge), primary=primary)

    def remove(self, edge, deep=False):
        """Removes an edge.

        Keyword argument:
        deep -- recursively remove all subedges (default False)
        """
        self._remove(edge, deep=deep)

    def is_primary(self, edge):
        """Check if an edge is primary."""
        return self._is_primary(edge)

    def set_primary(self, edge, value):
        """Make edge primary if value is True, make it non-primary
        otherwise.
        """
        self._set_primary(edge, value)

    def pat2edges(self, pattern):
        """Returns generator for all the edges that match a pattern.

        Patterns are themselves edges. They can match families of edges
        by employing special atoms:
            -> '*' represents a general wildcard (matches any edge)
            -> '@' represents an atomic wildcard (matches any atom)
            -> '&' represents a non-atomic wildcard (matches any non-atom)
            -> '...' at the end indicates an open-ended pattern.

        The pattern can be a string, that must represent an edge.
        Examples: '(is/pd graphbrain/c @)'
                  '(says/pd * ...)'

        Atomic patterns can also be used to match all edges in the
        hypergraph (*), all atoms (@), and all non-atoms (&).
        """
        if pattern == '*':
            return self.all()
        elif pattern == '@':
            return self.all_atoms()
        elif pattern == '&':
            return self.all_non_atoms()
        elif type(pattern) == str:
            edge = hedge(pattern)
            if not edge.is_atom():
                return self.pat2edges(edge)
            else:
                if self.exists(edge):
                    return (edge,)
                else:
                    return ()
        else:
            if (pattern.full_pattern()):
                return self.all()
            else:
                return self._pattern2edges(pattern)

    def star(self, center, limit=None):
        """Returns generator of the edges that contain the center.

        Keyword argument:
        deep -- recursively add all edges (default False)
        """
        return self._star(center, limit=limit)

    def atoms_with_root(self, root):
        """Returns generator of all atoms with the given root."""
        if len(root) == 0:
            return {}
        return self._atoms_with_root(root)

    def edges_with_edges(self, edges, root=None):
        """Returns generator of all edges containing the given edges,
        and optionally a given root.

        Keyword argument:
        root -- edge must also contain an atom with this root
                (default None)
        """
        return self._edges_with_edges(edges, root)

    def set_attribute(self, edge, attribute, value):
        """Sets the value of an attribute."""
        return self._set_attribute(edge, attribute, value)

    def inc_attribute(self, edge, attribute):
        """Increments an attribute of an entity."""
        return self._inc_attribute(edge, attribute)

    def dec_attribute(self, edge, attribute):
        """Increments an attribute of an entity."""
        return self._dec_attribute(edge, attribute)

    def get_str_attribute(self, edge, attribute, or_else=None):
        """Returns attribute as string.

        Keyword argument:
        or_else -- value to return if the entity does not have
                   the give attribute. (default None)
        """
        return self._get_str_attribute(edge, attribute, or_else=or_else)

    def get_int_attribute(self, edge, attribute, or_else=None):
        """Returns attribute as integer value.

        or_else -- value to return if the entity does not have
                   the give attribute. (default None)
        """
        return self._get_int_attribute(edge, attribute, or_else=or_else)

    def get_float_attribute(self, edge, attribute, or_else=None):
        """Returns attribute as float value.

        or_else -- value to return if the entity does not have
                   the give attribute. (default None)
        """
        return self._get_float_attribute(edge, attribute, or_else=or_else)

    def degree(self, edge):
        """Returns the degree of an entity."""
        return self._degree(edge)

    def deep_degree(self, edge):
        """Returns the deep degree of an entity."""
        return self._deep_degree(edge)

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
        edges = self.pat2edges(pattern)
        for edge in edges:
            self.remove(edge)

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

    def _pattern2edges(self, pattern):
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
