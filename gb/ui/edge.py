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


import hashlib
import gb.hypergraph.symbol as sym


SYMBOL_CLASSES = ('btn-primary', 'btn-success', 'btn-info', 'btn-warning', 'btn-danger')


def symbol_to_int(symbol):
    hasher = hashlib.sha1()
    hasher.update(symbol.encode('utf-8'))
    return int(hasher.hexdigest(), 16)


def symbol_html(symbol, rel):
    label = sym.symbol2str(symbol)
    if rel:
        return '<div class="rel"><a href="/vertex?id=%s">%s</a></div><div class="arrow"></div>'\
               % (symbol, label)
    else:
        extra_class = SYMBOL_CLASSES[symbol_to_int(symbol) % 5]
        return '<button type="button" class="btn %s symbol"><a class="symbol" href="/vertex?id=%s">%s</a></button>'\
               % (extra_class, symbol, label)


def edge_to_visual(hg, edge):
    rels = edge[0]
    entities = edge[1:]
    if sym.sym_type(rels) != sym.SymbolType.EDGE:
        rels = (rels,)
    visual_edge = []
    for i in range(len(entities)):
        visual_edge.append(edge_html(hg, entities[i], show_degree=False, outer=False, rel=False))
        if len(rels) > i:
            visual_edge.append(edge_html(hg, rels[i], show_degree=False, outer=False, rel=True))
    return visual_edge


def edge_html(hg, edge, show_degree=False, outer=True, rel=False):
    if sym.sym_type(edge) == sym.SymbolType.EDGE:
        html_edge = '<div class="hyperedge">%s</div>' % ' '.join(edge_to_visual(hg, edge))
        if outer:
            extra_html = ''
            if show_degree:
                degree = hg.degree(edge)
                extra_html = '<span class="badge">%s</span>' % degree
            html_edge = '<div class="outer-hyperedge">%s%s</div>' % (html_edge, extra_html)
        return html_edge
    else:
        return symbol_html(edge, rel)
