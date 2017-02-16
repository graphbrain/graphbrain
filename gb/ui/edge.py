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


def symbol_html(symbol):
    return '<a href="/vertex?id=%s">%s</a>' % (symbol, symbol)


def edge_html(hg, edge):
    if sym.sym_type(edge) == sym.SymbolType.EDGE:
        degree = hg.degree(edge)
        html_symbols = [edge_html(hg, symbol) for symbol in edge]
        html_edge = '(%s)' % ' '.join(html_symbols)
        return '<p>%s [degree: %s]</p>' % (html_edge, degree)
    else:
        return symbol_html(edge)
