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


import unittest
import gb.hypergraph.sql as sql


class TestSQL(unittest.TestCase):

    def test_perms(self):
        t = ('a', 'b', 'c')
        self.assertEqual(sql.nthperm(t, 0), ('a', 'b', 'c'))
        self.assertEqual(sql.nthperm(t, 1), ('a', 'c', 'b'))
        self.assertEqual(sql.nthperm(t, 2), ('b', 'a', 'c'))
        self.assertEqual(sql.nthperm(t, 3), ('b', 'c', 'a'))
        self.assertEqual(sql.nthperm(t, 4), ('c', 'a', 'b'))
        self.assertEqual(sql.nthperm(t, 5), ('c', 'b', 'a'))

    def test_unperm(self):
        self.assertEqual(sql.unpermutate(('a', 'b', 'c'), 0), ('a', 'b', 'c'))
        self.assertEqual(sql.unpermutate(('a', 'c', 'b'), 1), ('a', 'b', 'c'))
        self.assertEqual(sql.unpermutate(('b', 'a', 'c'), 2), ('a', 'b', 'c'))
        self.assertEqual(sql.unpermutate(('b', 'c', 'a'), 3), ('a', 'b', 'c'))
        self.assertEqual(sql.unpermutate(('c', 'a', 'b'), 4), ('a', 'b', 'c'))
        self.assertEqual(sql.unpermutate(('c', 'b', 'a'), 5), ('a', 'b', 'c'))

if __name__ == '__main__':
    unittest.main()
