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
import gb.hypergraph.hypergraph as hyperg
import gb.hypergraph.edge as ed


class TestHypergraph(unittest.TestCase):

    def ops_test_1(self, hg):
        hg.add(('is', 'graphbrain/1', 'great/1'))
        self.assertTrue(hg.exists(('is', 'graphbrain/1', 'great/1')))
        hg.remove(('is', 'graphbrain/1', 'great/1'))
        self.assertFalse(hg.exists(('is', 'graphbrain/1', 'great/1')))

    def ops_test_2(self, hg):
        hg.add(('size', 'graphbrain/1', 7))
        self.assertTrue(hg.exists(('size', 'graphbrain/1', 7)))
        hg.remove(('size', 'graphbrain/1', 7))
        self.assertFalse(hg.exists(('size', 'graphbrain/1', 7)))

    def ops_test_3(self, hg):
        hg.add(('size', 'graphbrain/1', 7.0))
        self.assertTrue(hg.exists(('size', 'graphbrain/1', 7.0)))
        hg.remove(('size', 'graphbrain/1', 7.0))
        self.assertFalse(hg.exists(('size', 'graphbrain/1', 7.0)))

    def ops_test_4(self, hg):
        hg.add(('size', 'graphbrain/1', -7))
        self.assertTrue(hg.exists(('size', 'graphbrain/1', -7)))
        hg.remove(('size', 'graphbrain/1', -7))
        self.assertFalse(hg.exists(('size', 'graphbrain/1', -7)))

    def ops_test_5(self, hg):
        hg.add(('size', 'graphbrain/1', -7.0))
        self.assertTrue(hg.exists(('size', 'graphbrain/1', -7.0)))
        hg.remove(('size', 'graphbrain/1', -7.0))
        self.assertFalse(hg.exists(('size', 'graphbrain/1', -7.0)))

    def ops_test_6(self, hg):
        hg.add(('src', 'graphbrain/1', ('size', 'graphbrain/1', -7.0)))
        self.assertTrue(hg.exists(('src', 'graphbrain/1', ('size', 'graphbrain/1', -7.0))))
        hg.remove(('src', 'graphbrain/1', ('size', 'graphbrain/1', -7.0)))
        self.assertFalse(hg.exists(('src', 'graphbrain/1', ('size', 'graphbrain/1', -7.0))))

    def destroy_test(self, hg):
        hg.add(('src', 'graphbrain/1', ('size', 'graphbrain/1', -7.0)))
        self.assertTrue(hg.exists(('src', 'graphbrain/1', ('size', 'graphbrain/1', -7.0))))
        hg.destroy()
        self.assertFalse(hg.exists(('src', 'graphbrain/1', ('size', 'graphbrain/1', -7.0))))

    def pattern2edges_test(self, hg):
        hg.add(('is', 'graphbrain/1', 'great/1'))
        hg.add(('says', 'mary/1', ('is', 'graphbrain/1', 'great/1')))
        self.assertEqual(hg.pattern2edges((None, 'graphbrain/1', None)), {('is', 'graphbrain/1', 'great/1')})
        self.assertEqual(hg.pattern2edges(('is', 'graphbrain/1', None)), {('is', 'graphbrain/1', 'great/1')})
        self.assertEqual(hg.pattern2edges(('x', None, None)), set())
        self.assertEqual(hg.pattern2edges(('says', None, ('is', 'graphbrain/1', 'great/1'))),
                         {('says', 'mary/1', ('is', 'graphbrain/1', 'great/1'))})
        hg.remove(('is', 'graphbrain/1', 'great/1'))
        hg.remove(('says', 'mary/1', ('is', 'graphbrain/1', 'great/1')))

    def star_test(self, hg):
        hg.add(('is', 'graphbrain/1', 'great/1'))
        self.assertEqual(hg.star('graphbrain/1'), {('is', 'graphbrain/1', 'great/1')})
        self.assertEqual(hg.star('graphbrain/2'), set())
        hg.remove(('is', 'graphbrain/1', 'great/1'))

    def symbols_with_root_test(self, hg):
        hg.add(('is', 'graphbrain/1', 'great/1'))
        self.assertEqual(hg.symbols_with_root('graphbrain'), {'graphbrain/1'})
        hg.add(('is', 'graphbrain/2', 'great/1'))
        self.assertEqual(hg.symbols_with_root('graphbrain'), {'graphbrain/1', 'graphbrain/2'})
        hg.remove(('is', 'graphbrain/1', 'great/1'))
        hg.remove(('is', 'graphbrain/2', 'great/1'))
        self.assertEqual(hg.symbols_with_root('graphbrain'), set())

    def degree_test(self, hg):
        self.assertEqual(hg.degree('graphbrain/1'), 0)
        hg.add(('is', 'graphbrain/1', 'great/1'))
        self.assertEqual(hg.degree('graphbrain/1'), 1)
        self.assertEqual(hg.degree('great/1'), 1)
        hg.add(('size', 'graphbrain/1', 7))
        self.assertEqual(hg.degree('graphbrain/1'), 2)
        self.assertEqual(hg.degree('great/1'), 1)
        hg.remove(('is', 'graphbrain/1', 'great/1'))
        self.assertEqual(hg.degree('graphbrain/1'), 1)
        self.assertEqual(hg.degree('great/1'), 0)
        hg.remove(('size', 'graphbrain/1', 7))
        self.assertEqual(hg.degree('graphbrain/1'), 0)

    def timestamp_test(self, hg):
        hg.destroy()
        self.assertEqual(hg.timestamp('graphbrain/1'), -1)
        hg.add(('is', 'graphbrain/1', 'great/1'), timestamp=123456789)
        self.assertEqual(hg.timestamp('graphbrain/1'), 123456789)
        self.assertEqual(hg.timestamp('great/1'), 123456789)
        self.assertEqual(hg.timestamp(('is', 'graphbrain/1', 'great/1')), 123456789)
        hg.remove(('is', 'graphbrain/1', 'great/1'))
        self.assertEqual(hg.timestamp('graphbrain/1'), 123456789)
        self.assertEqual(hg.timestamp('great/1'), 123456789)
        self.assertEqual(hg.timestamp(('is', 'graphbrain/1', 'great/1')), -1)

    def add_remove_multiple_test(self, hg):
        hg.add((('is', 'graphbrain/1', 'great/1'), ('src', 'graphbrain/1', ('size', 'graphbrain/1', -7.0))))
        self.assertTrue(hg.exists(('is', 'graphbrain/1', 'great/1')))
        self.assertTrue(hg.exists(('src', 'graphbrain/1', ('size', 'graphbrain/1', -7.0))))
        hg.remove((('is', 'graphbrain/1', 'great/1'), ('src', 'graphbrain/1', ('size', 'graphbrain/1', -7.0))))
        self.assertFalse(hg.exists(('is', 'graphbrain/1', 'great/1')))
        self.assertFalse(hg.exists(('src', 'graphbrain/1', ('size', 'graphbrain/1', -7.0))))

    def batch_exec_test(self, hg):
        def f1(x):
            x.add(('is', 'graphbrain/1', 'great/1'))

        def f2(x):
            x.add(('src', 'graphbrain/1', ('size', 'graphbrain/1', -7.0)))

        hg.batch_exec((f1, f2))
        self.assertTrue(hg.exists(('is', 'graphbrain/1', 'great/1')))
        self.assertTrue(hg.exists(('src', 'graphbrain/1', ('size', 'graphbrain/1', -7.0))))

        def f1(x):
            x.remove(('is', 'graphbrain/1', 'great/1'))

        def f2(x):
            x.remove(('src', 'graphbrain/1', ('size', 'graphbrain/1', -7.0)))

        hg.batch_exec((f1, f2))
        self.assertFalse(hg.exists(('is', 'graphbrain/1', 'great/1')))
        self.assertFalse(hg.exists(('src', 'graphbrain/1', ('size', 'graphbrain/1', -7.0))))

    def f_all_test(self, hg):
        hg.destroy()
        hg.add(('size', 'graphbrain/1', -7.0))
        hg.add(('is', 'graphbrain/1', 'great/1'))
        hg.add(('src', 'mary/1', ('is', 'graphbrain/1', 'great/1')))

        def f(x):
            return '%s %s' % (ed.edge2str(x['vertex']), x['degree'])

        labels = set(hg.f_all(f))
        self.assertEqual(labels, {'size 1', 'graphbrain/1 2', '-7.0 1', 'is 1', 'great/1 1', 'src 1', 'mary/1 1',
                                  '(size graphbrain/1 -7.0) 0', '(is graphbrain/1 great/1) 1',
                                  '(src mary/1 (is graphbrain/1 great/1)) 0'})
        hg.destroy()
        labels = set(hg.f_all(f))
        self.assertEqual(labels, set())

    def test_ops(self):
        params = {'backend': 'sqlite',
                  'file_path': 'test.db'}
        hg = hyperg.HyperGraph(params)
        self.ops_test_1(hg)
        self.ops_test_2(hg)
        self.ops_test_3(hg)
        self.ops_test_4(hg)
        self.ops_test_5(hg)
        self.ops_test_6(hg)
        self.destroy_test(hg)
        self.pattern2edges_test(hg)
        self.star_test(hg)
        self.symbols_with_root_test(hg)
        self.degree_test(hg)
        self.timestamp_test(hg)
        self.add_remove_multiple_test(hg)
        self.batch_exec_test(hg)
        self.f_all_test(hg)


if __name__ == '__main__':
    unittest.main()
