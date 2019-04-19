from graphbrain.funs import *


class Hypergraph(object):
    """Hypergraph interface."""

    # ================================================================
    # Interface abstract methods, to be implemented in derived classes
    # ================================================================

    def close(self):
        raise NotImplementedError()

    def name(self):
        raise NotImplementedError()

    def destroy(self):
        """Erase the hypergraph."""
        raise NotImplementedError()

    def all(self):
        """Returns a lazy sequence of all the edges in the hypergraph."""
        raise NotImplementedError()

    def all_attributes(self):
        """Returns a lazy sequence of tuples, one per edge in the hypergraph.
           The first element of the tuple is the edge itself,
           the second is a dictionary of attribute values (as strings)."""
        raise NotImplementedError()

    def atom_count(self):
        """Total number of atoms in the hypergraph"""
        raise NotImplementedError()

    def edge_count(self):
        """Total number of edge in the hypergraph"""
        raise NotImplementedError()

    def total_degree(self):
        """Total degree of the hypergraph"""
        raise NotImplementedError()

    def _exists(self, edge):
        """Checks if the given edge exists in the hypergraph."""
        raise NotImplementedError()

    def _add(self, edge):
        """Adds edge to the hypergraph if it does not exist yet.
           Returns same edge."""
        raise NotImplementedError()

    def _remove(self, edge):
        """Removes and edge from the hypergraph."""
        raise NotImplementedError()

    def _pattern2edges(self, pattern, open_ended=False):
        """Return all the edges that match a pattern.
        A pattern is a collection of entity ids and wildcards (None)."""
        raise NotImplementedError()

    def _star(self, center, limit=None):
        """Return all the edges that contain a given entity.
        Entity can be atomic or an edge."""
        raise NotImplementedError()

    def _atoms_with_root(self, root):
        """Find all atoms with the given root."""
        raise NotImplementedError()

    def _edges_with_atoms(self, atoms, root):
        """Find all edges containing the given atoms,
           and optionally a given root"""
        raise NotImplementedError()

    def _set_attribute(self, entity, attribute, value):
        """Sets the value of an attribute."""
        raise NotImplementedError()

    def _inc_attribute(self, entity, attribute):
        """Increments an attribute of an entity."""
        raise NotImplementedError()

    def _dec_attribute(self, entity, attribute):
        """Increments an attribute of an entity."""
        raise NotImplementedError()

    def _get_str_attribute(self, entity, attribute, or_else=None):
        """Returns attribute as string."""
        raise NotImplementedError()

    def _get_int_attribute(self, entity, attribute, or_else=None):
        """Returns attribute as integer value."""
        raise NotImplementedError()

    def _get_float_attribute(self, entity, attribute, or_else=None):
        """Returns attribute as float value."""
        raise NotImplementedError()

    def _degree(self, entity):
        """Returns the degree of an entity."""
        raise NotImplementedError()
    # ================================
    # End of intrface abstract mehtods
    # ================================

    # ==================
    # High-level methods
    # ==================
    def exists(self, edge):
        """Checks if the given edge exists in the hypergraph."""
        return self._exists(edge)

    def add(self, edge, deep=False):
        """Adds edge to the hypergraph if it does not exist yet.
           Returns same edge."""
        if is_edge(edge):
            if deep:
                for entity in edge:
                    self.add(entity)
            return self._add(edge)
        else:
            return edge

    def remove(self, edge):
        """Removes and edge from the hypergraph."""
        if isinstance(edge, (list, tuple)):
            self._remove(edge)

    def pattern2edges(self, pattern, open_ended=False):
        """Return generator for all the edges that match a pattern.
           A pattern is a collection of entity ids and wildcards.
           Wildcards are represented by None.
           Pattern example: ('is/p', None, None)"""
        return self._pattern2edges(pattern, open_ended=open_ended)

    def star(self, center, limit=None):
        """Return all the edges that contain a given entity.
        Entity can be atomic or an edge."""
        return self._star(center, limit=limit)

    def atoms_with_root(self, root):
        """Find all atoms with the given root."""
        if len(root) == 0:
            return {}
        return self._atoms_with_root(root)

    def edges_with_atoms(self, atoms, root=None):
        """Find all edges containing the given atoms, and
           optionally a given root"""
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
        """Returns attribute as string."""
        return self._get_str_attribute(entity, attribute, or_else=or_else)

    def get_int_attribute(self, entity, attribute, or_else=None):
        """Returns attribute as integer value."""
        return self._get_int_attribute(entity, attribute, or_else=or_else)

    def get_float_attribute(self, entity, attribute, or_else=None):
        """Returns attribute as float value."""
        return self._get_float_attribute(entity, attribute, or_else=or_else)

    def degree(self, entity):
        """Returns the degree of an entity."""
        return self._degree(entity)

    def ego(self, center):
        """Returns all atoms directly connected to center by hyperedges."""
        edges = self.star(center)
        atom_set = set()
        for edge in edges:
            for atom in atoms(edge):
                atom_set.add(atom)
        return atom_set

    def remove_by_pattern(self, pattern):
        """Removes from the hypergraph all edges that match the pattern."""
        edges = self.pattern2edges(pattern)
        for edge in edges:
            self.remove(edge)
