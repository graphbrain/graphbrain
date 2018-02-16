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
import gb.hypergraph.symbol as sym


class TestSymbol(unittest.TestCase):

    def test_hashed(self):
        self.assertEqual(sym.hashed('graphbrain/1'), '821dd667c0d1e35b')

    def test_sym_type(self):
        self.assertEqual(sym.sym_type('graphbrain/1'), sym.SymbolType.CONCEPT)
        self.assertEqual(sym.sym_type(42), sym.SymbolType.INTEGER)
        self.assertEqual(sym.sym_type(-7.9), sym.SymbolType.FLOAT)
        self.assertEqual(sym.sym_type('http://graphbrain.org'), sym.SymbolType.URL)
        self.assertEqual(sym.sym_type('https://graphbrain.org'), sym.SymbolType.URL)

    def test_parts(self):
        self.assertEqual(sym.parts('graphbrain/1'), ['graphbrain', '1'])
        self.assertEqual(sym.parts('graphbrain'), ['graphbrain'])
        self.assertEqual(sym.parts('http://graphbrain.org'), ['http://graphbrain.org'])
        self.assertEqual(sym.parts(1), [1])
        self.assertEqual(sym.parts(1.), [1.])

    def test_root(self):
        self.assertEqual(sym.root('graphbrain/1'), 'graphbrain')
        self.assertEqual(sym.root('graphbrain'), 'graphbrain')
        self.assertEqual(sym.root('http://graphbrain.org'), 'http://graphbrain.org')
        self.assertEqual(sym.root(1), 1)
        self.assertEqual(sym.root(1.), 1.)

    def test_nspace(self):
        self.assertEqual(sym.nspace('graphbrain/1'), '1')
        self.assertEqual(sym.nspace('graphbrain'), None)
        self.assertEqual(sym.nspace('http://graphbrain.org'), None)
        self.assertEqual(sym.nspace(1), None)
        self.assertEqual(sym.nspace(1.), None)

    def test_is_root(self):
        self.assertFalse(sym.is_root('graphbrain/1'))
        self.assertTrue(sym.is_root('graphbrain'))
        self.assertTrue(sym.is_root('http://graphbrain.org'))
        self.assertTrue(sym.is_root(1))
        self.assertTrue(sym.is_root(1.))

    def test_build(self):
        self.assertEqual(sym.build('graphbrain', '1'), 'graphbrain/1')


if __name__ == '__main__':
    unittest.main()
