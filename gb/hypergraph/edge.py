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
        s = s[1:]
        pos += 1

    if point:
        return TokenType.DOUBLE
    else:
        return TokenType.INTEGER


def parsed_token(token):
    """Transform a string token into a value of the correct type."""
    if token[0] == '(':
        return str2edge(token)
    else:
        toktype = token_type(token)
        if toktype == TokenType.STRING:
            return token
        elif toktype == TokenType.INTEGER:
            return int(token)
        elif toktype == TokenType.DOUBLE:
            return float(token)


def split_edge_str(edge_str):
    """Shallow split into tokens of a string representation of an edge,
    with or without outer parenthesis."""

    edge_inner_str = edge_str
    if (edge_str[0] == '(') and (edge_str[-1] == ')'):
        edge_inner_str = edge_str[1:-1]
    stoks = edge_inner_str.split()

    tokens = []
    curtok = None
    depth = 0
    while len(stoks) > 0:
        tok = stoks[0]
        depth = depth + open_pars(tok) - close_pars(tok)
        bottom = depth == 0
        if curtok is None:
            curtok = tok
        else:
            curtok = '%s %s' % (curtok, tok)
        if bottom:
            tokens.append(curtok)
        if bottom:
            curtok = None
        stoks = stoks[1:]

    return tuple(tokens)


def str2edge(edge_str):
    """Convert a string representation of an edge to an edge."""
    tokens = split_edge_str(edge_str)
    elements = tuple(parsed_token(token) for token in tokens)
    if len(elements) > 1:
        return elements
    else:
        return elements[0]


def nodes2str(edge):
    """Convert a collection of nodes to a string representation (no outer parenthesis)."""
    node_strings = []
    for node in edge:
        if sym.sym_type(node) == sym.SymbolType.EDGE:
            node_strings.append(edge2str(node))
        else:
            node_strings.append(str(node))
    return ' '.join(node_strings)


def edge2str(edge):
    """Convert an edge to its string representation."""
    if sym.sym_type(edge) == sym.SymbolType.EDGE:
        return '(%s)' % nodes2str(edge)
    else:
        return str(edge)


def is_negative(edge):
    """Check if edge is negative."""
    return sym.is_negative(edge[0])


def negative(edge):
    """Produces the negative of the given edge."""
    return (sym.negative(edge[0]),) + edge[1:]


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
