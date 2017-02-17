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


def edges_html(hg, eid):
    edges = hg.star(eid)
    html_lines = [edge_html(hg, e, show_degree=True) for e in edges]
    return '\n'.join(html_lines)


def html(hg, eid):
    return """
<div class="container" role="main">
    <div class="page-header">
        <h1>Vertex: %s</h1>
    </div>
    %s
</div>
    """ % (eid, edges_html(hg, eid))
