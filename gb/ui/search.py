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


def search_result(hg, symbol):
    degree = hg.degree(symbol)
    return '<p><a href="/vertex?id=%s">%s</a> [degree: %s]</p>' % (symbol, symbol, degree)


def search_results(hg, query):
    symbols = hg.symbols_with_root(sym.str2symbol(query))
    html_lines = [search_result(hg, symbol) for symbol in symbols]
    return '\n'.join(html_lines)


def html(hg, query):
    return """
<div class="container" role="main">
    <div class="page-header">
        <h1>Search results for '%s'</h1>
      </div>
    <p>%s</p>
</div>
    """ % (query, search_results(hg, query))
