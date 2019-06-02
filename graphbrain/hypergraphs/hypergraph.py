from graphbrain.funs import *


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
        """Returns a generator of all the entities."""
        raise NotImplementedError()

    def all_attributes(self):
        """Returns a generator with a tuple for each entity.
           The first element of the tuple is the entity itself,
           the second is a dictionary of attribute values
           (as strings)."""
        raise NotImplementedError()

    def atom_count(self):
        """Returns total number of atoms."""
        raise NotImplementedError()

    def edge_count(self):
        """Returns total number of edges."""
        raise NotImplementedError()

    def total_degree(self):
        """Returns sum of all degrees."""
        raise NotImplementedError()

    # ============================
    # High-level interface methods
    # ============================

    def all_atoms(self):
        """Returns a generator of all the atoms."""
        for entity in self.all():
            if is_atom(entity):
                yield entity

    def all_edges(self):
        """Returns a generator of all the edges."""
        for entity in self.all():
            if is_edge(entity):
                yield entity

    def exists(self, entity):
        """Checks if the given entity exists."""
        return self._exists(entity)

    def add(self, edge, deep=False):
        """Adds an edge if it does not exist yet,
        returns same edge.

        Keyword argument:
        deep -- recursively add all edges (default False)
        """
        if is_edge(edge):
            if deep:
                for entity in edge:
                    self.add(entity)
            return self._add(edge)
        else:
            return edge

    def remove(self, edge):
        """Removes an edge."""
        if isinstance(edge, (list, tuple)):
            self._remove(edge)

    def pat2ents(self, pattern):
        """Returns generator for all the entities that match a pattern.

        Patterns are themselves edges. They can match families of edges
        by employing special atoms:
            -> '*' represents a general wildcard (matches any entity)
            -> '@' represents an atomic wildcard (matches any atom)
            -> '&' represents an edge wildcard (matches any edge)
            -> '...' at the end indicates an open-ended pattern.

        The pattern can be an edge.
        Examples: ('is/pd', 'graphbrain/c', '@')
                  ('says/pd', '*', '...')

        The pattern can be a string, that must represent an edge.
        Examples: '(is/pd graphbrain/c @)'
                  '(says/pd * ...)'

        Atomic patterns can also be used to match all entities in the
        hypergraph (*), all atoms (@), and all edges (&).
        """
        if pattern == '*':
            return self.all()
        elif pattern == '@':
            return self.all_atoms()
        elif pattern == '&':
            return self.all_edges()
        elif type(pattern) == str:
            entity = str2ent(pattern)
            if is_edge(entity):
                return self.pat2ents(entity)
            else:
                if self.exists(entity):
                    return (entity,)
                else:
                    return ()
        else:
            if (no_constant(pattern)):
                return self.all()
            else:
                return self._pattern2edges(pattern)

    def star(self, center, limit=None):
        """Returns generator of the edges that contain the entity.

        Keyword argument:
        deep -- recursively add all edges (default False)
        """
        return self._star(center, limit=limit)

    def atoms_with_root(self, root):
        """Returns generator of all atoms with the given root."""
        if len(root) == 0:
            return {}
        return self._atoms_with_root(root)

    def edges_with_atoms(self, atoms, root=None):
        """Returns generator of all edges containing the given atoms,
        and optionally a given root.

        Keyword argument:
        root -- edge must also contain an atom with this root
                (default None)
        """
        return self._edges_with_atoms(atoms, root)

    def set_attribute(self, entity, attribute, value):
        """Sets the value of an attribute."""
        return self._set_attribute(entity, attribute, value)

    def inc_attribute(self, entity, attribute):
        """Increments an attribute of an entity."""
        return self._inc_attribute(entity, attribute)

    def dec_attribute(self, entity, attribute):
        """Increments an attribute of an entity."""
        return self._dec_attribute(entity, attribute)

    def get_str_attribute(self, entity, attribute, or_else=None):
        """Returns attribute as string.

        Keyword argument:
        or_else -- value to return if the entity does not have
                   the give attribute. (default None)
        """
        return self._get_str_attribute(entity, attribute, or_else=or_else)

    def get_int_attribute(self, entity, attribute, or_else=None):
        """Returns attribute as integer value.

        or_else -- value to return if the entity does not have
                   the give attribute. (default None)
        """
        return self._get_int_attribute(entity, attribute, or_else=or_else)

    def get_float_attribute(self, entity, attribute, or_else=None):
        """Returns attribute as float value.

        or_else -- value to return if the entity does not have
                   the give attribute. (default None)
        """
        return self._get_float_attribute(entity, attribute, or_else=or_else)

    def degree(self, entity):
        """Returns the degree of an entity."""
        return self._degree(entity)

    def ego(self, center):
        """Returns all atoms directly connected to center
           by hyperedges.
        """
        edges = self.star(center)
        atom_set = set()
        for edge in edges:
            for atom in atoms(edge):
                atom_set.add(atom)
        return atom_set

    def remove_by_pattern(self, pattern):
        """Removes all edges that match the pattern."""
        edges = self.pat2ents(pattern)
        for edge in edges:
            self.remove(edge)

    # ==============================================================
    # Private abstract methods, to be implemented in derived classes
    # ==============================================================

    def _exists(self, edge):
        raise NotImplementedError()

    def _add(self, edge):
        raise NotImplementedError()

    def _remove(self, edge):
        raise NotImplementedError()

    def _pattern2edges(self, pattern):
        raise NotImplementedError()

    def _star(self, center, limit=None):
        raise NotImplementedError()

    def _atoms_with_root(self, root):
        raise NotImplementedError()

    def _edges_with_atoms(self, atoms, root):
        raise NotImplementedError()

    def _set_attribute(self, entity, attribute, value):
        raise NotImplementedError()

    def _inc_attribute(self, entity, attribute):
        raise NotImplementedError()

    def _dec_attribute(self, entity, attribute):
        raise NotImplementedError()

    def _get_str_attribute(self, entity, attribute, or_else=None):
        raise NotImplementedError()

    def _get_int_attribute(self, entity, attribute, or_else=None):
        raise NotImplementedError()

    def _get_float_attribute(self, entity, attribute, or_else=None):
        raise NotImplementedError()

    def _degree(self, entity):
        raise NotImplementedError()
