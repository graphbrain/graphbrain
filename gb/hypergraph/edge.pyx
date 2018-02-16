#   Copyright (c) 2016 CNRS - Centre national de la recherche scientifique.
#   All rights reserved.
#
#   Written by Telmo Menezes <telmo@telmomenezes.com>
#
#   This file is part of GraphBrain.
#
#   GraphBrain is free software: you can redistribute it and/or modify
#   it under the terms of the GNU Affero General Public License as published by
#   the Free Software Foundation, either version 3 of the License, or
#   (at your option) any later version.
#
#   GraphBrain is distributed in the hope that it will be useful,
#   but WITHOUT ANY WARRANTY; without even the implied warranty of
#   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#   GNU Affero General Public License for more details.
#
#   You should have received a copy of the GNU Affero General Public License
#   along with GraphBrain.  If not, see <http://www.gnu.org/licenses/>.


import gb.hypergraph.symbol as sym


class TokenType(object):
    def __init__(self):
        pass

    INTEGER = 0
    DOUBLE = 1
    STRING = 2


def open_pars(s):
    """Number of consecutive open parenthesis at the beginning of the string."""
    pos = 0
    while s[pos] == '(':
        pos += 1
    return pos


def close_pars(s):
    """Number of consecutive close parenthesis at the end of the string."""
    pos = -1
    while s[pos] == ')':
        pos -= 1
    return -pos - 1


def token_type(token):
    """Determine the type of a string token: STRING, INTEGER or DOUBLE."""

    s = token
    pos = 0
    point = False
    numbers = False

    while len(s) > 0:
        c = s[0]
        if c == '-':
            if pos > 0 or len(token) == 1:
                return TokenType.STRING
        elif c == '.':
            if point:
                return TokenType.STRING
            else:
                point = True
        elif c < '0' or c > '9':
            return TokenType.STRING
        else:
            numbers = True
        s = s[1:]
        pos += 1

    if not numbers:
        return TokenType.STRING

    if point:
        return TokenType.DOUBLE
    else:
        return TokenType.INTEGER


def parsed_token(token):
    """Transform a string token into a value of the correct type."""
    if edge_str_has_outer_parens(token):
        return str2edge(token)
    else:
        toktype = token_type(token)
        if toktype == TokenType.STRING:
            return token
        elif toktype == TokenType.INTEGER:
            return int(token)
        elif toktype == TokenType.DOUBLE:
            return float(token)


def edge_str_has_outer_parens(str edge_str):
    """Check if string representation of edge is delimited by outer parenthesis."""
    if len(edge_str) < 2:
        return False
    return edge_str[0] == '('


def split_edge_str(str edge_str):
    """Shallow split into tokens of a string representation of an edge, without outer parenthesis."""
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


def str2edge(str edge_str):
    """Convert a string representation of an edge to an edge."""

    cdef str edge_inner_str = edge_str
    if edge_str_has_outer_parens(edge_str):
        edge_inner_str = edge_str[1:-1]

    tokens = split_edge_str(edge_inner_str)
    if not tokens:
        return None
    elements = tuple(parsed_token(token) for token in tokens)
    if len(elements) > 1:
        return elements
    elif len(elements) > 0:
        return elements[0]
    else:
        return None


def nodes2str(edge, namespaces=True):
    """Convert a collection of nodes to a string representation (no outer parenthesis)."""
    node_strings = []
    for node in edge:
        if sym.sym_type(node) == sym.SymbolType.EDGE:
            edge_str = edge2str(node, namespaces)
        else:
            if namespaces:
                edge_str = str(node).strip()
            else:
                edge_str = str(sym.root(node)).strip()
        if edge_str != '':
                node_strings.append(edge_str)
    return ' '.join(node_strings)


def edge2str(edge, namespaces=True):
    """Convert an edge to its string representation."""
    if sym.sym_type(edge) == sym.SymbolType.EDGE:
        return '(%s)' % nodes2str(edge, namespaces)
    else:
        if namespaces:
            return str(edge).strip()
        else:
            return str(sym.root(edge)).strip()


def symbols(edge):
    """Return set of symbols contained in edge."""
    if sym.sym_type(edge) == sym.SymbolType.EDGE:
        symbs = set()
        for entity in edge:
            for symb in symbols(entity):
                symbs.add(symb)
        return symbs
    else:
        return {edge}


def depth(edge):
    """Returns maximal depth of the edge, a symbol has depth 0"""
    if sym.sym_type(edge) == sym.SymbolType.EDGE:
        max_d = 0
        for item in edge:
            d = depth(item)
            if d > max_d:
                max_d = d
        return max_d + 1
    else:
        return 0


def without_namespaces(edge):
    """Returns edge stripped of namespaces"""
    if sym.sym_type(edge) == sym.SymbolType.EDGE:
        return tuple(without_namespaces(item) for item in edge)
    else:
        return sym.root(edge)


def contains(edge, concept, deep=False):
    if sym.is_edge(edge):
        for x in edge:
            if x == concept:
                return True
            if deep:
                if contains(x, concept, True):
                    return True
        return False
    else:
        return edge == concept


def size(edge):
    """size of edge, if symbol size is 1"""
    if sym.is_edge(edge):
        return len(edge)
    return 1


def subedges(edge):
    """all the subedges contained in the edge, including atoms and itself"""
    edges = {edge}
    if sym.is_edge(edge):
        for item in edge:
            edges = edges.union(subedges(item))
    return edges
