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


import sqlite3
from sql import SQL


def exec_or_ignore(cur, query):
    try:
        cur.execute(query)
    except sqlite3.OperationalError:
        pass


class SQLite(SQL):
    """Implements SQLite hypergraph storage."""

    def __init__(self, params):
        file_path = params['file_path']
        SQL.__init__(self, sqlite3.connect(file_path))

    def create_tables(self):
        """Created the tables necessary to store the hypergraph."""
        cur = self.conn.cursor()

        # Vertices table
        exec_or_ignore(cur, 'CREATE TABLE vertices (id TEXT PRIMARY KEY)')
        exec_or_ignore(cur, 'ALTER TABLE vertices ADD COLUMN degree INTEGER DEFAULT 0')
        exec_or_ignore(cur, 'ALTER TABLE vertices ADD COLUMN timestamp INTEGER DEFAULT -1')

        # Edge permutations table
        exec_or_ignore(cur, 'CREATE TABLE perms (id TEXT PRIMARY KEY)')

        self.conn.commit()
        cur.close()
