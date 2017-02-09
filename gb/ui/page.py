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


def css():
    return """
    body {
        background-color: #FFF;
    }
    input {
        width: 100%;
        font-family:"Arial Black", Gadget, sans-serif;
        font-size:x-large;
    }
    input[type=submit] {
        margin-top:2em;
    }
    h3 {
        font-family:"Arial Black", Gadget, sans-serif;
        font-size:x-large;
        color: #555;
        margin-bottom:0px;
    }
    .last-event {
        font-family:"Arial Black", Gadget, sans-serif;
        font-size:x-large;
        color: #33A;
        margin-top:2em;
    }
    """


def html(title, body):
    return """
    <!DOCTYPE html>
    <html lang="en">

    <head>
        <meta charset="utf-8">
        <title>
            %s
        </title>
        <style TYPE="text/css">
        <!--
        %s
        -->
        </style>
    </head>

    <body>
        %s
    </body>

    </html>
    """ % (title, css(), body)
