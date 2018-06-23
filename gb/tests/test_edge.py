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
from gb.funs import *


class TestEdge(unittest.TestCase):

    def test_split_edge_str(self):
        self.assertEqual(split_edge_str('is graphbrain/1 great/1'), ('is', 'graphbrain/1', 'great/1'))
        self.assertEqual(split_edge_str('size graphbrain/1 7'), ('size', 'graphbrain/1', '7'))
        self.assertEqual(split_edge_str('size graphbrain/1 7.0'), ('size', 'graphbrain/1', '7.0'))
        self.assertEqual(split_edge_str('size graphbrain/1 -7'), ('size', 'graphbrain/1', '-7'))
        self.assertEqual(split_edge_str('size graphbrain/1 -7.0'), ('size', 'graphbrain/1', '-7.0'))
        self.assertEqual(split_edge_str('src graphbrain/1 (is graphbrain/1 great/1)'),
                         ('src', 'graphbrain/1', '(is graphbrain/1 great/1)'))

    def test_str2edge(self):
        self.assertEqual(str2edge('(is graphbrain/1 great/1)'), ('is', 'graphbrain/1', 'great/1'))
        self.assertEqual(str2edge('(size graphbrain/1 7)'), ('size', 'graphbrain/1', 7))
        self.assertEqual(str2edge('(size graphbrain/1 7.0)'), ('size', 'graphbrain/1', 7.))
        self.assertEqual(str2edge('(size graphbrain/1 -7)'), ('size', 'graphbrain/1', -7))
        self.assertEqual(str2edge('(size graphbrain/1 -7.0)'), ('size', 'graphbrain/1', -7.))
        self.assertEqual(str2edge('(src graphbrain/1 (is graphbrain/1 great/1))'),
                         ('src', 'graphbrain/1', ('is', 'graphbrain/1', 'great/1')))
        self.assertEqual(str2edge('((is my) graphbrain/1 (super great/1))'),
                         (('is', 'my'), 'graphbrain/1', ('super', 'great/1')))
        self.assertEqual(str2edge('.'), '.')

    def test_edge2str(self):
        self.assertEqual(edge2str(('is', 'graphbrain/1', 'great/1')), '(is graphbrain/1 great/1)')
        self.assertEqual(edge2str(('size', 'graphbrain/1', 7)), '(size graphbrain/1 7)')
        self.assertEqual(edge2str(('size', 'graphbrain/1', 7.)), '(size graphbrain/1 7.0)')
        self.assertEqual(edge2str(('size', 'graphbrain/1', -7.)), '(size graphbrain/1 -7.0)')
        self.assertEqual(edge2str(('src', 'graphbrain/1', ('is', 'graphbrain/1', 'great/1'))),
                         '(src graphbrain/1 (is graphbrain/1 great/1))')

    def test_edge2str_roots(self):
        self.assertEqual(edge2str(('is', 'graphbrain/1', 'great/1'), namespaces=False), '(is graphbrain great)')
        self.assertEqual(edge2str(('size', 'graphbrain/1', 7), namespaces=False), '(size graphbrain 7)')
        self.assertEqual(edge2str(('size', 'graphbrain/1', 7.), namespaces=False), '(size graphbrain 7.0)')
        self.assertEqual(edge2str(('size', 'graphbrain/1', -7.), namespaces=False), '(size graphbrain -7.0)')
        self.assertEqual(edge2str(('src', 'graphbrain/1', ('is', 'graphbrain/1', 'great/1')), namespaces=False),
                         '(src graphbrain (is graphbrain great))')

    def test_symbols(self):
        self.assertEqual(edge_symbols(('is', 'graphbrain/1', 'great/1')), {'is', 'graphbrain/1', 'great/1'})
        self.assertEqual(edge_symbols(('src', 'graphbrain/2', ('is', 'graphbrain/1', 'great/1'))),
                         {'is', 'graphbrain/1', 'great/1', 'src', 'graphbrain/2'})
        self.assertEqual(edge_symbols('graphbrain/1'), {'graphbrain/1'})

    def test_max_depth(self):
        self.assertEqual(edge_depth('graphbrain/1'), 0)
        self.assertEqual(edge_depth(('is', 'graphbrain/1', 'great/1')), 1)
        self.assertEqual(edge_depth(('is', 'graphbrain/1', ('super', 'great/1'))), 2)

    def test_without_namespaces(self):
        self.assertEqual(without_namespaces('graphbrain/1'), 'graphbrain')
        self.assertEqual(without_namespaces(('is', 'graphbrain/1', 'great/1')), ('is', 'graphbrain', 'great'))
        self.assertEqual(without_namespaces(('is', 'graphbrain/1', ('super', 'great/1'))),
                         ('is', 'graphbrain', ('super', 'great')))

    def test_size(self):
        self.assertEqual(edge_size('graphbrain/1'), 1)
        self.assertEqual(edge_size(('is', 'graphbrain/1', 'great/1')), 3)
        self.assertEqual(edge_size(('is', 'graphbrain/1', ('super', 'great/1'))), 3)
        self.assertEqual(edge_size(('super', 'great/1')), 2)

    def test_subedges(self):
        self.assertEqual(subedges('graphbrain/1'), {'graphbrain/1'})
        self.assertEqual(subedges(('is', 'graphbrain/1', 'great/1')),
                         {'is', 'graphbrain/1', 'great/1', ('is', 'graphbrain/1', 'great/1')})
        self.assertEqual(subedges(('is', 'graphbrain/1', ('super', 'great/1'))),
                         {'is', 'graphbrain/1', 'super', 'great/1', ('super', 'great/1'),
                          ('is', 'graphbrain/1', ('super', 'great/1'))})

    def test_is_concept(self):
        self.assertTrue(is_concept(('+/gb', 'of/1', 'user/1' 'graphbrain/1')))
        self.assertFalse(is_concept(('is', 'graphbrain/1', 'great/1')))
        self.assertTrue(is_concept('graphbrain/1'))


if __name__ == '__main__':
    unittest.main()
