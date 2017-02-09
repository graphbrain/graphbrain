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


import inspect


class HGPlugin(object):
    """
    This plugin passes an hypergraph object to route callbacks
    that accept a `hg` keyword argument.
    """

    name = 'hg'
    api = 2

    def __init__(self, hg):
        self.hg = hg

    def apply(self, callback, context):
        # Test if the original callback accepts an 'hg' keyword.
        # Ignore it if it does not need a database handle.
        callback_args = inspect.signature(context.callback).parameters
        if 'hg' not in callback_args:
            return callback

        def wrapper(*args, **kwargs):
            # Add the connection handle as a keyword argument.
            kwargs['hg'] = self.hg

            return callback(*args, **kwargs)

        # Replace the route callback with the wrapped one.
        return wrapper
