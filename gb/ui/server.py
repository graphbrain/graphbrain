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
from bottle import route, run, request, get, static_file
from gb.ui import page, home, search, vertex, parser
from gb.ui.hgplugin import HGPlugin


# Static Routes
@get("/static/css/<filepath:re:.*\.css>")
def css(filepath):
    return static_file(filepath, root="resources/css")


@get("/static/font/<filepath:re:.*\.(eot|otf|svg|ttf|woff|woff2?)>")
def font(filepath):
    return static_file(filepath, root="resources/font")


@get("/static/img/<filepath:re:.*\.(jpg|png|gif|ico|svg)>")
def img(filepath):
    return static_file(filepath, root="resources/img")


@get("/static/js/<filepath:re:.*\.js>")
def js(filepath):
    return static_file(filepath, root="resources/js")


# Home page
@route('/')
def home_page(hg):
    return page.html('Home', home.html(hg))


@get('/search')
def search_page(hg):
    query = request.query.query
    return page.html('Search results', search.html(hg, query))


@get('/vertex')
def vertex_page(hg):
    eid = request.query.id
    return page.html('Vertex', vertex.html(hg, eid))


@get('/parser')
def parser_page(hg):
    text = request.query.text
    return page.html('Parser', parser.html(hg, text))


def start_ui(hg):
    hgplugin = HGPlugin(hg)
    bottle.install(hgplugin)
    run(host='localhost', port=8080, debug=True)
