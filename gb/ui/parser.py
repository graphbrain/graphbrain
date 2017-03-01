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


from gb.knowledge.extractor import Extractor
from .edge import edge_html


extractor = None


def get_extractor(hg):
    global extractor
    if extractor is None:
        extractor = Extractor(hg)
    return extractor


def edges_html(hg, edges):
    html_list = [edge_html(hg, edge) for edge in edges]
    return '\n'.join(html_list)


def html(hg, text):
    extra_html = ''
    if text != '':
        results = get_extractor(hg).read_text(text)
        for result in results:
            extra_html += '<h3>%s</h3>' % result.sentence
            extra_html += edges_html(hg, (result.main_edge,))
            extra_html += edges_html(hg, result.edges)

    return """
<div class="container" role="main">
    <div class="page-header">
        <h1>Text parser</h1>
    </div>
    <form action="/parser" method="get">
        <textarea class="form-control" name="text" rows="5">%s</textarea>
        <br />
        <button type="submit" class="btn btn-success">Parse</button>
    </form>
    %s
</div>
    """ % (text, extra_html)
