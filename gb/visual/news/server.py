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


import bottle
from bottle import route, run, get, static_file, request
from gb.hypergraph.hypergraph import HyperGraph
from gb.visual.news import page, home, node, node_json
from gb.ui.hgplugin import HGPlugin


# Static Routes
@get("/static/css/<filepath:re:.*\.css>")
def css(filepath):
    return static_file(filepath, root="resources/css")


@get("/static/fonts/<filepath:re:.*\.(eot|otf|svg|ttf|woff|woff2?)>")
def font(filepath):
    return static_file(filepath, root="resources/fonts")


@get("/static/img/<filepath:re:.*\.(jpg|png|gif|ico|svg)>")
def img(filepath):
    return static_file(filepath, root="resources/img")


@get("/static/js/<filepath:re:.*\.js>")
def js(filepath):
    return static_file(filepath, root="js/news")


@route('/')
def home_page(hg):
    return page.html('Actors', home.html(hg))


@get('/node/<root>/<namespace>')
def node_page(root, namespace):
    symbol = root + '/' + namespace
    return page.html('Node', node.html(symbol), extra_head=node.head())


@get('/node_json/<root>/<namespace>')
def node_json_page(hg, root, namespace):
    symbol = root + '/' + namespace
    return node_json.json_str(hg, symbol)


def start_server(hg):
    hgplugin = HGPlugin(hg)
    bottle.install(hgplugin)
    run(host='localhost', port=8080, debug=True)


if __name__ == '__main__':
    hgr = HyperGraph({'backend': 'leveldb', 'hg': 'infer.hg'})
    start_server(hgr)
