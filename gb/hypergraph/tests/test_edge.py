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
import gb.hypergraph.edge as ed


class TestEdge(unittest.TestCase):

    def test_split_edge_str(self):
        self.assertEqual(ed.split_edge_str('(is graphbrain/1 great/1)'), ('is', 'graphbrain/1', 'great/1'))
        self.assertEqual(ed.split_edge_str('(size graphbrain/1 7)'), ('size', 'graphbrain/1', '7'))
        self.assertEqual(ed.split_edge_str('(size graphbrain/1 7.0)'), ('size', 'graphbrain/1', '7.0'))
        self.assertEqual(ed.split_edge_str('(size graphbrain/1 -7)'), ('size', 'graphbrain/1', '-7'))
        self.assertEqual(ed.split_edge_str('(size graphbrain/1 -7.0)'), ('size', 'graphbrain/1', '-7.0'))
        self.assertEqual(ed.split_edge_str('(src graphbrain/1 (is graphbrain/1 great/1))'),
                         ('src', 'graphbrain/1', '(is graphbrain/1 great/1)'))

    def test_edge_str_has_outer_parens(self):
        self.assertTrue(ed.edge_str_has_outer_parens('(is graphbrain/1 great/1)'))
        self.assertFalse(ed.edge_str_has_outer_parens('is graphbrain/1 great/1'))
        self.assertTrue(ed.edge_str_has_outer_parens('((is my) graphbrain/1 (super great/1))'))
        self.assertFalse(ed.edge_str_has_outer_parens('(is my) graphbrain/1 (super great/1)'))

    def test_str2edge(self):
        self.assertEqual(ed.str2edge('(is graphbrain/1 great/1)'), ('is', 'graphbrain/1', 'great/1'))
        self.assertEqual(ed.str2edge('(size graphbrain/1 7)'), ('size', 'graphbrain/1', 7))
        self.assertEqual(ed.str2edge('(size graphbrain/1 7.0)'), ('size', 'graphbrain/1', 7.))
        self.assertEqual(ed.str2edge('(size graphbrain/1 -7)'), ('size', 'graphbrain/1', -7))
        self.assertEqual(ed.str2edge('(size graphbrain/1 -7.0)'), ('size', 'graphbrain/1', -7.))
        self.assertEqual(ed.str2edge('(src graphbrain/1 (is graphbrain/1 great/1))'),
                         ('src', 'graphbrain/1', ('is', 'graphbrain/1', 'great/1')))
        self.assertEqual(ed.str2edge('(is my) graphbrain/1 (super great/1)'),
                         (('is', 'my'), 'graphbrain/1', ('super', 'great/1')))

    def test_edge2str(self):
        self.assertEqual(ed.edge2str(('is', 'graphbrain/1', 'great/1')), '(is graphbrain/1 great/1)')
        self.assertEqual(ed.edge2str(('size', 'graphbrain/1', 7)), '(size graphbrain/1 7)')
        self.assertEqual(ed.edge2str(('size', 'graphbrain/1', 7.)), '(size graphbrain/1 7.0)')
        self.assertEqual(ed.edge2str(('size', 'graphbrain/1', -7.)), '(size graphbrain/1 -7.0)')
        self.assertEqual(ed.edge2str(('src', 'graphbrain/1', ('is', 'graphbrain/1', 'great/1'))),
                         '(src graphbrain/1 (is graphbrain/1 great/1))')

    def test_edge2str_roots(self):
        self.assertEqual(ed.edge2str(('is', 'graphbrain/1', 'great/1'), namespaces=False), '(is graphbrain great)')
        self.assertEqual(ed.edge2str(('size', 'graphbrain/1', 7), namespaces=False), '(size graphbrain 7)')
        self.assertEqual(ed.edge2str(('size', 'graphbrain/1', 7.), namespaces=False), '(size graphbrain 7.0)')
        self.assertEqual(ed.edge2str(('size', 'graphbrain/1', -7.), namespaces=False), '(size graphbrain -7.0)')
        self.assertEqual(ed.edge2str(('src', 'graphbrain/1', ('is', 'graphbrain/1', 'great/1')), namespaces=False),
                         '(src graphbrain (is graphbrain great))')

    def test_is_negative(self):
        self.assertTrue(ed.is_negative(('~is', 'graphbrain/1', 'great/1')))
        self.assertFalse(ed.is_negative(('is', 'graphbrain/1', 'great/1')))

    def test_negative(self):
        self.assertEqual(ed.negative(('~is', 'graphbrain/1', 'great/1')),
                         ('is', 'graphbrain/1', 'great/1'))
        self.assertEqual(ed.negative(('is', 'graphbrain/1', 'great/1')),
                         ('~is', 'graphbrain/1', 'great/1'))

    def test_symbols(self):
        self.assertEqual(ed.symbols(('is', 'graphbrain/1', 'great/1')), {'is', 'graphbrain/1', 'great/1'})
        self.assertEqual(ed.symbols(('src', 'graphbrain/2', ('is', 'graphbrain/1', 'great/1'))),
                         {'is', 'graphbrain/1', 'great/1', 'src', 'graphbrain/2'})
        self.assertEqual(ed.symbols('graphbrain/1'), {'graphbrain/1'})


if __name__ == '__main__':
    unittest.main()
