from abc import ABC

from graphbrain.hypergraph import Hypergraph
from graphbrain.memory.permutations import do_with_edge_permutations
from graphbrain.patterns import match_pattern


class KeyValue(Hypergraph):
    """Common class for key-value based hypergraph storage."""

    def __init__(self, locator_string):
        super().__init__()
        self.locator_string = locator_string

    # ===================================
    # Implementation of interface methods
    # ===================================

    def name(self):
        return self.locator_string

    def add_with_attributes(self, edge, attributes):
        self.begin_transaction()
        key = self._edge2key(edge)
        self._add_key(key, attributes)
        if edge.not_atom:
            self._write_edge_permutations(edge)
        self.end_transaction()
        return edge

    # ==============================================================
    # Private abstract methods, to be implemented in derived classes
    # ==============================================================

    def _edge2key(self, edge):
        raise NotImplementedError()

    def _exists_key(self, key):
        """Checks if the given key exists."""
        raise NotImplementedError()

    def _add_key(self, key, attributes):
        """Adds the given edge, given its key."""
        raise NotImplementedError()

    def _attribute_key(self, key):
        raise NotImplementedError()

    def _write_edge_permutation(self, perm):
        """Writes a given permutation."""
        raise NotImplementedError()

    def _remove_edge_permutation(self, perm):
        """Removes a given permutation."""
        raise NotImplementedError()

    def _remove_key(self, key):
        raise NotImplementedError()

    def _match_structure(self, pattern, strict):
        raise NotImplementedError()

    # ==========================================
    # Implementation of private abstract methods
    # ==========================================

    def _exists(self, edge):
        return self._exists_key(self._edge2key(edge))

    def _set_attribute_key(self, key, attribute, value):
        """Sets the value of an attribute by key."""
        exists = self._exists_key(key)
        if exists:
            exists = True
            attributes = self._attribute_key(key)
            attributes[attribute] = value
        else:
            attributes = {'p': 0, 'd': 0, 'dd': 0, attribute: value}
        self._add_key(key, attributes)
        return exists

    def _inc_attribute_key(self, key, attribute):
        """Increments an attribute of an edge."""
        if self._exists_key(key):
            attributes = self._attribute_key(key)
            if attribute in attributes:
                cur_value = int(attributes[attribute])
                attributes[attribute] = cur_value + 1
            else:
                attributes[attribute] = 1
            self._add_key(key, attributes)
            return True
        else:
            return False

    def _dec_attribute_key(self, key, attribute):
        """Decrements an attribute of an edge."""
        if self._exists_key(key):
            attributes = self._attribute_key(key)
            cur_value = int(attributes[attribute])
            attributes[attribute] = cur_value - 1
            self._add_key(key, attributes)
            return True
        else:
            return False

    def _inc_degrees(self, edge, depth=0):
        if depth > 0:
            key = self._edge2key(edge)
            if not self._exists_key(key):
                d = 1 if depth == 1 else 0
                self._add_key(key, {'p': 0, 'd': d, 'dd': 1})
            else:
                if depth == 1:
                    self._inc_attribute_key(key, 'd')
                self._inc_attribute_key(key, 'dd')
        if edge.not_atom:
            for child in edge:
                self._inc_degrees(child, depth + 1)

    def _dec_degrees(self, edge, depth=0):
        if depth > 0:
            key = self._edge2key(edge)
            if depth == 1:
                self._dec_attribute_key(key, 'd')
            self._dec_attribute_key(key, 'dd')
        if edge.not_atom:
            for child in edge:
                self._dec_degrees(child, depth + 1)

    def _write_edge_permutations(self, edge):
        """Writes all permutations of the edge."""
        do_with_edge_permutations(edge, self._write_edge_permutation)

    def _remove_edge_permutations(self, edge):
        """Removes all permutations of the edge."""
        do_with_edge_permutations(edge, self._remove_edge_permutation)

    def _add(self, edge, primary):
        self.begin_transaction()
        key = self._edge2key(edge)
        if not self._exists_key(key):
            if primary:
                self._add_key(key, {'p': 1, 'd': 0, 'dd': 0})
                self._inc_degrees(edge)
            else:
                self._add_key(key, {'p': 0, 'd': 0, 'dd': 0})
            if edge.not_atom:
                self._write_edge_permutations(edge)
        # if an edge is to be added as primary, but it already exists as
        # non-primary, then make it primary and update the degrees
        elif primary and not self._is_primary(edge):
            self._set_attribute_key(key, 'p', 1)
            self._inc_degrees(edge)
        self.end_transaction()
        return edge

    def _remove(self, edge, deep):
        self.begin_transaction()
        primary = self.is_primary(edge)

        if edge.not_atom:
            if deep:
                for child in edge:
                    self._remove(child, deep=True)
            else:
                if primary:
                    self._dec_degrees(edge)

        key = self._edge2key(edge)
        if self._exists_key(key):
            if edge.not_atom:
                self._remove_edge_permutations(edge)
            self._remove_key(key)
        self.end_transaction()

    def _is_primary(self, edge):
        return self._get_int_attribute(edge, 'p') == 1

    def _set_primary(self, edge, value):
        self._set_attribute(edge, 'p', 1 if value else 0)

    def _search(self, pattern, strict=True):
        for result in self._match(pattern, strict=strict):
            yield result[0]

    def _match(self, pattern, strict=True, curvars=None):
        if curvars is None:
            curvars = {}
        for edge in self._match_structure(pattern, strict):
            results = match_pattern(edge, pattern, curvars=curvars, hg=self)
            if len(results) > 0:
                yield edge, results

    def _set_attribute(self, edge, attribute, value):
        self.begin_transaction()
        key = self._edge2key(edge)
        result = self._set_attribute_key(key, attribute, value)
        self.end_transaction()
        return result

    def _inc_attribute(self, edge, attribute):
        self.begin_transaction()
        key = self._edge2key(edge)
        result = self._inc_attribute_key(key, attribute)
        self.end_transaction()
        return result

    def _dec_attribute(self, edge, attribute):
        self.begin_transaction()
        key = self._edge2key(edge)
        result = self._dec_attribute_key(key, attribute)
        self.end_transaction()
        return result

    def _get_str_attribute(self, edge, attribute, or_else=None):
        key = self._edge2key(edge)
        return self._get_str_attribute_key(key, attribute, or_else)

    def _get_int_attribute(self, edge, attribute, or_else=None):
        key = self._edge2key(edge)
        return self._get_int_attribute_key(key, attribute, or_else)

    def _get_float_attribute(self, edge, attribute, or_else=None):
        key = self._edge2key(edge)
        return self._get_float_attribute_key(key, attribute, or_else)

    def _degree(self, edge):
        return self.get_int_attribute(edge, 'd', 0)

    def _deep_degree(self, edge):
        return self.get_int_attribute(edge, 'dd', 0)

    # =====================
    # Local private methods
    # =====================

    def _get_str_attribute_key(self, key, attribute, or_else=None):
        if self._exists_key(key):
            attributes = self._attribute_key(key)
            if attribute in attributes:
                return attributes[attribute]
            else:
                return or_else
        else:
            return or_else

    def _get_int_attribute_key(self, key, attribute, or_else=None):
        if self._exists_key(key):
            attributes = self._attribute_key(key)
            if attribute in attributes:
                return int(attributes[attribute])
            else:
                return or_else
        else:
            return or_else

    def _get_float_attribute_key(self, key, attribute, or_else=None):
        if self._exists_key(key):
            attributes = self._attribute_key(key)
            if attribute in attributes:
                return float(attributes[attribute])
            else:
                return or_else
        else:
            return or_else
