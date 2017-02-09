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
from bottle import route, run, request, post, redirect, get, static_file
from gb.ui import page, home
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
    return page.html('GraphBrain', home.html(hg))


# @post('/new_event')
# def new_event(db):
#     name = request.forms.get('name')
#     quantity = request.forms.get('quantity')
#     value = request.forms.get('value')
#     details = request.forms.get('details')
#     newevent.add(db, name, quantity, value, details)
#     redirect('/')


def start_ui(hg):
    hgplugin = HGPlugin(hg)
    bottle.install(hgplugin)
    run(host='localhost', port=8080, debug=True)
