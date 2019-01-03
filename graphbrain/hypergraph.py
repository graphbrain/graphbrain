import logging
from graphbrain.funs import *
import graphbrain.constants as const
from graphbrain.backends.null import Null
from graphbrain.backends.leveldb import LevelDB


def init_hypergraph(hg, backend='leveldb'):
    params = {'backend': backend, 'hg': hg}
    return HyperGraph(params)


class HyperGraph(object):
    """Hypergraph operations."""

    def __init__(self, params):
        backend = params['backend']
        if backend == 'none':
            pass
        elif backend == 'null':
            self.backend = Null()
        elif backend == 'leveldb':
            self.backend = LevelDB(params)
        else:
            raise RuntimeError('Unkown hypergraph backend: %s' % backend)

    def close(self):
        self.backend.close()

    def name(self):
        return self.backend.name()

    def exists(self, vertex):
        """Checks if the given edge exists in the hypergraph."""
        logging.debug('[hypergraph exists()] %s' % edge2str(vertex))
        return self.backend.exists(vertex)

    def add(self, edge, timestamp=-1):
        """Adds and edge to the hypergraph if it does not exist yet."""
        logging.debug('[hypergraph add()] %s ts: %s' % (edge2str(edge), timestamp))
        if isinstance(edge, (list, tuple)):
            return self.backend.add(edge, timestamp)
        else:
            return edge

    def remove(self, edge):
        """Removes and edge from the hypergraph."""
        logging.debug('[hypergraph remove()] %s' % edge2str(edge))
        if isinstance(edge, (list, tuple)):
            self.backend.remove(edge)

    def pattern2edges(self, pattern, open_ended=False):
        """Return all the edges that match a pattern.
        A pattern is a collection of entity ids and wildcards (None)."""
        logging.debug('[hypergraph pattern2edges()] %s' % edge2str(pattern))
        return self.backend.pattern2edges(pattern, open_ended)

    def star(self, center, limit=None):
        """Return all the edges that contain a given entity.
        Entity can be atomic or an edge."""
        logging.debug('[hypergraph star()] %s' % edge2str(center))
        return self.backend.star(center, limit=limit)

    def symbols_with_root(self, root):
        """Find all edge_symbols with the given root."""
        logging.debug('[hypergraph symbols_with_root()] %s' % edge2str(root))
        if len(root) == 0:
            return {}
        return self.backend.symbols_with_root(root)

    def edges_with_symbols(self, symbols, root=None):
        """Find all edges containing the given edge_symbols, and optionally a given root"""
        logging.debug('[hypergraph edges_with_symbols()] %s root: %s' % (symbols, edge2str(root)))
        return self.backend.edges_with_symbols(symbols, root)

    def destroy(self):
        """Erase the hypergraph."""
        logging.debug('[hypergraph destroy()]')
        self.backend.destroy()

    def set_attribute(self, vertex, attribute, value):
        """Sets the value of an attribute."""
        logging.debug('[hypergraph set_attribute()] %s %s=%s' % (edge2str(vertex), attribute, value))
        return self.backend.set_attribute(vertex, attribute, value)

    def inc_attribute(self, vertex, attribute):
        """Increments an attribute of a vertex."""
        logging.debug('[hypergraph inc_attribute()] %s attribute: %s' % (edge2str(vertex), attribute))
        return self.backend.inc_attribute(vertex, attribute)

    def dec_attribute(self, vertex, attribute):
        """Increments an attribute of a vertex."""
        logging.debug('[hypergraph dec_attribute()] %s attribute: %s' % (edge2str(vertex), attribute))
        return self.backend.dec_attribute(vertex, attribute)

    def get_str_attribute(self, vertex, attribute, or_else=None):
        """Returns attribute as string."""
        logging.debug('[hypergraph get_str_attribute()] %s attribute: %s; or_else: %s'
                      % (edge2str(vertex), attribute, or_else))
        return self.backend.get_str_attribute(vertex, attribute, or_else)

    def get_int_attribute(self, vertex, attribute, or_else=None):
        """Returns attribute as integer value."""
        logging.debug('[hypergraph get_int_attribute()] %s attribute: %s; or_else: %s'
                      % (edge2str(vertex), attribute, or_else))
        return self.backend.get_int_attribute(vertex, attribute, or_else)

    def get_float_attribute(self, vertex, attribute, or_else=None):
        """Returns attribute as float value."""
        logging.debug('[hypergraph get_float_attribute()] %s attribute: %s; or_else: %s'
                      % (edge2str(vertex), attribute, or_else))
        return self.backend.get_float_attribute(vertex, attribute, or_else)

    def degree(self, vertex):
        """Returns the degree of a vertex."""
        logging.debug('[hypergraph degree()] %s' % edge2str(vertex))
        return self.backend.degree(vertex)

    def timestamp(self, vertex):
        """Returns the timestamp of a vertex."""
        logging.debug('[hypergraph timestamp()] %s' % edge2str(vertex))
        return self.backend.timestamp(vertex)

    def remove_by_pattern(self, pattern):
        """Removes from the hypergraph all edges that match the pattern."""
        logging.debug('[hypergraph remove_by_pattern()] %s' % edge2str(pattern))
        edges = self.pattern2edges(pattern)
        for edge in edges:
            self.remove(edge)

    def ego(self, center):
        """Returns all edge_symbols directly connected to centre by hyperedges."""
        logging.debug('[hypergraph ego()] %s' % edge2str(center))
        edges = self.star(center)
        symbols = set()
        for edge in edges:
            for symbol in edge_symbols(edge):
                symbols.add(symbol)
        return symbols

    def add_belief(self, source, edge, timestamp=-1):
        """A belif is a fact with a source. The fact is created as a normal edge
           if it does not exist yet. Another edge is created to assign the fact to
           the source."""
        logging.debug('[hypergraph add_belief()] %s source: %s; ts: %s' % (edge2str(edge), source, timestamp))
        self.add(edge, timestamp)
        self.add((const.source, edge, source), timestamp)

    def sources(self, edge):
        """Set of sources (nodes) that support a statement (edge)."""
        logging.debug('[hypergraph sources()] %s' % edge2str(edge))
        edges = self.pattern2edges((const.source, edge, None))
        sources = [edge[2] for edge in edges]
        return set(sources)

    def is_belief(self, edge):
        """Check if hyperedge is a belief (has sources)."""
        return len(self.pattern2edges((const.source, edge, None))) > 0

    def remove_belief(self, source, edge):
        """A belif is a fact with a source. The link from the source to the fact
           is removed. If no more sources support the fact, then the fact is also
           removed."""
        logging.debug('[hypergraph remove_belief()] %s source: %s' % (edge2str(edge), source))
        self.remove((const.source, edge, source))
        if len(self.sources(edge)) == 0:
            self.remove(edge)

    def all(self):
        """Returns a lazy sequence of all the vertices in the hypergraph."""
        logging.debug('[hypergraph all()]')
        return self.backend.all()

    def all_attributes(self):
        """Returns a lazy sequence with a tuple for each vertex in the hypergraph.
           The first element of the tuple is the vertex itself,
           the second is a dictionary of attribute values (as strings)."""
        logging.debug('[hypergraph all_attributes()]')
        return self.backend.all_attributes()

    def symbol_count(self):
        """Total number of edge_symbols in the hypergraph"""
        logging.debug('[hypergraph symbol_count()]')
        return self.backend.symbol_count()

    def edge_count(self):
        """Total number of edge in the hypergraph"""
        logging.debug('[hypergraph edge_count()]')
        return self.backend.edge_count()

    def total_degree(self):
        """Total degree of the hypergraph"""
        logging.debug('[hypergraph total_degree()]')
        return self.backend.total_degree()

    def has_label(self, edge):
        edges = self.pattern2edges([const.has_label, edge, None])
        return len(edges) > 0

    def get_label(self, edge):
        edges = self.pattern2edges([const.has_label, edge, None])
        if len(edges) > 0:
            label_symbol = edges.pop()[2]
            if not is_edge(label_symbol):
                return symbol2str(label_symbol)
        return symbol2str(edge)
