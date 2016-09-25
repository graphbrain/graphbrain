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


import string
import random
import numpy as np


class SymbolType(object):
    def __init__(self):
        pass

    UNKNOWN = 0
    CONCEPT = 1
    EDGE = 2
    INTEGER = 3
    FLOAT = 4
    URL = 5


def hashed(txt):
    """Creates an hash code for a string."""
    s = txt
    h = np.array([[1125899906842597]], dtype=np.uint64)  # prime
    while len(s) > 0:
        c = ord(s[0])
        h = np.dot(np.array([[31]], dtype=np.uint64), (h + np.array([[c]], dtype=np.uint64)))
        s = s[1:]
    return hex(h[0][0])[2:-1]


def random_hash():
    """Creates random hash code."""
    s = ''.join(random.choice(string.ascii_lowercase + string.ascii_uppercase + string.digits) for _ in range(100))
    return hashed(s)


def sym_type(sym):
    """Type of symbol: CONCEPT, EDGE, INTEGER, FLOAT or URL"""
    if isinstance(sym, (list, tuple)):
        return SymbolType.EDGE
    elif isinstance(sym, str):
        if sym[:7] == 'http://':
            return SymbolType.URL
        elif sym[:8] == 'https://':
            return SymbolType.URL
        else:
            return SymbolType.CONCEPT
    elif isinstance(sym, int):
        return SymbolType.INTEGER
    elif isinstance(sym, float):
        return SymbolType.FLOAT
    else:
        return SymbolType.UNKNOWN


def parts(sym):
    """Splits a symbol into its parts.
    All symbol types except CONCEPT only have one part."""
    if sym_type(sym) == SymbolType.CONCEPT:
        return sym.split('/')
    else:
        return [sym]


def root(sym):
    """Extracts the root of a symbol (e.g. the root of graphbrain/1 is graphbrain)"""
    return parts(sym)[0]


def nspace(sym):
    """Extracts the namespace of a symbol (e.g. the namespace of graphbrain/1 is 1)
    Returns None if symbol has no namespace."""
    ps = parts(sym)
    if len(ps) > 1:
        return parts(sym)[1]
    else:
        return None


def is_root(sym):
    """Is the symbol the root of itself?"""
    return sym == root(sym)


def build(pts):
    """Build a concept symbol for a collection of strings."""
    return '/'.join(pts)


def str2symbol(s):
    """Converts a string into a valid symbol"""
    sym = s.lower()
    sym = sym.replace("/", "_")
    sym = sym.replace(" ", "_")
    sym = sym.replace("(", "_")
    sym = sym.replace(")", "_")
    return sym


def symbol2str(sym):
    """Converts a symbol into a string representation."""
    stype = sym_type(sym)
    if stype == SymbolType.CONCEPT:
        return root(sym).replace('_', ' ')
    elif stype == SymbolType.URL:
        return sym
    else:
        return str(sym)


def new_meaning(symb, prefix=''):
    """Creates a new symbol for the given root.
    If given symbols is not a root, return it unchanged."""
    if is_root(symb):
        return build([symb, '%s%s' % (prefix, random_hash())])
    else:
        return symb


def is_negative(symb):
    """Check if symbol is negative."""
    return symb[0] == '~'


def negative(symb):
    """Produces the negative of the given symbol."""
    if is_negative(symb):
        return symb[1:]
    else:
        return '~%s' % symb
