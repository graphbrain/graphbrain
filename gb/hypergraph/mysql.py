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


import MySQLdb
import _mysql_exceptions
from gb.hypergraph.sql import SQL


def exec_or_ignore(cur, query):
    try:
        cur.execute(query)
    except _mysql_exceptions.OperationalError:
        pass
    except _mysql_exceptions.ProgrammingError:
        pass


class MySQL(SQL):
    """Implements MySQL hypergraph storage."""

    def __init__(self, params):
        host = params['dbhost']
        user = params['dbuser']
        password = params['dbpass']
        dbname = params['db']
        conn = MySQLdb.connect(host=host, user=user, passwd=password, db=dbname)
        conn.autocommit(False)
        conn.set_character_set('utf8')
        cur = conn.cursor()
        cur.execute('SET NAMES utf8;')
        cur.execute('SET CHARACTER SET utf8;')
        cur.execute('SET character_set_connection=utf8;')
        SQL.__init__(self, conn, '%s')

    def create_tables(self):
        """Created the tables necessary to store the hypergraph."""
        cur = self.conn.cursor()

        # Vertices table
        create_table = '%s %s' % ('CREATE TABLE vertices (id VARCHAR(10000), PRIMARY KEY (id(250)))',
                                  'ENGINE=InnoDB DEFAULT CHARSET=utf8;')
        exec_or_ignore(cur, create_table)
        exec_or_ignore(cur, 'ALTER TABLE vertices ADD COLUMN degree INT DEFAULT 0')
        exec_or_ignore(cur, 'ALTER TABLE vertices ADD COLUMN timestamp BIGINT DEFAULT -1')

        # Edge permutations table
        create_table = '%s %s' % ('CREATE TABLE perms (id VARCHAR(10000), PRIMARY KEY (id(250)))',
                                  'ENGINE=InnoDB DEFAULT CHARSET=utf8;')
        exec_or_ignore(cur, create_table)

        self.conn.commit()
        cur.close()

    # override
    def update_or_insert(self, table, row, vid):
        """Updates columns or inserts a new row in the vertices table"""
        cur = self.open_cursor()

        values = [v for v in row.values()]
        key_str = ','.join([k for k in row.keys()])
        placeholder_str = ','.join([self.ph] * len(row.keys()))
        update_str = ','.join(['%s=%s' % (k, 'VALUES(%s)' % k) for k in row.keys()])
        cur.execute('INSERT INTO %s (%s) values (%s) ON DUPLICATE KEY UPDATE %s'
                    % (table, key_str, placeholder_str, update_str), values)

        self.close_cursor(cur, local=True, commit=True)
