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


from .edge import edge_html
import gb.hypergraph.symbol as sym
import gb.hypergraph.edge as ed


def edges_html(hg, vertex):
    edges = hg.star(vertex)
    html_lines = [edge_html(hg, e, show_degree=True) for e in edges]
    return '\n'.join(html_lines)


def html(hg, eid):
    vertex = ed.str2edge(eid)
    if sym.sym_type(vertex) == sym.SymbolType.EDGE:
        title = edge_html(hg, vertex)
    else:
        title = '<h1>%s</h1>' % sym.symbol2str(eid)
    return """
<div class="container" role="main">
    <div class="page-header">
        %s
        <h4>%s</h4>
    </div>
    %s
</div>
    """ % (title, eid, edges_html(hg, vertex))
