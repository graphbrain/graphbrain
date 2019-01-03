import unittest
from graphbrain.hypergraph import HyperGraph
from graphbrain.funs import *


class TestLevelDB(unittest.TestCase):

    def setUp(self):
        params = {'backend': 'leveldb',
                  'hg': 'test.hg'}
        self.hg = HyperGraph(params)

    def tearDown(self):
        self.hg.close()

    def test_ops_1(self):
        self.hg.add(('is', 'graphbrain/1', 'great/1'))
        self.assertTrue(self.hg.exists(('is', 'graphbrain/1', 'great/1')))
        self.hg.remove(('is', 'graphbrain/1', 'great/1'))
        self.assertFalse(self.hg.exists(('is', 'graphbrain/1', 'great/1')))

    def test_ops_2(self):
        self.hg.add(('size', 'graphbrain/1', 7))
        self.assertTrue(self.hg.exists(('size', 'graphbrain/1', 7)))
        self.hg.remove(('size', 'graphbrain/1', 7))
        self.assertFalse(self.hg.exists(('size', 'graphbrain/1', 7)))

    def test_ops_3(self):
        self.hg.add(('size', 'graphbrain/1', 7.0))
        self.assertTrue(self.hg.exists(('size', 'graphbrain/1', 7.0)))
        self.hg.remove(('size', 'graphbrain/1', 7.0))
        self.assertFalse(self.hg.exists(('size', 'graphbrain/1', 7.0)))

    def test_ops_4(self):
        self.hg.add(('size', 'graphbrain/1', -7))
        self.assertTrue(self.hg.exists(('size', 'graphbrain/1', -7)))
        self.hg.remove(('size', 'graphbrain/1', -7))
        self.assertFalse(self.hg.exists(('size', 'graphbrain/1', -7)))

    def test_ops_5(self):
        self.hg.add(('size', 'graphbrain/1', -7.0))
        self.assertTrue(self.hg.exists(('size', 'graphbrain/1', -7.0)))
        self.hg.remove(('size', 'graphbrain/1', -7.0))
        self.assertFalse(self.hg.exists(('size', 'graphbrain/1', -7.0)))

    def test_ops_6(self):
        self.hg.add(('src', 'graphbrain/1', ('size', 'graphbrain/1', -7.0)))
        self.assertTrue(self.hg.exists(('src', 'graphbrain/1', ('size', 'graphbrain/1', -7.0))))
        self.hg.remove(('src', 'graphbrain/1', ('size', 'graphbrain/1', -7.0)))
        self.assertFalse(self.hg.exists(('src', 'graphbrain/1', ('size', 'graphbrain/1', -7.0))))

    def test_destroy(self):
        self.hg.add(('src', 'graphbrain/1', ('size', 'graphbrain/1', -7.0)))
        self.assertTrue(self.hg.exists(('src', 'graphbrain/1', ('size', 'graphbrain/1', -7.0))))
        self.hg.destroy()
        self.assertFalse(self.hg.exists(('src', 'graphbrain/1', ('size', 'graphbrain/1', -7.0))))

    def test_pattern2edges(self):
        self.hg.add(('is', 'graphbrain/1', 'great/1'))
        self.hg.add(('says', 'mary/1'))
        self.hg.add(('says', 'mary/1', ('is', 'graphbrain/1', 'great/1')))
        self.hg.add(('says', 'mary/1', ('is', 'graphbrain/1', 'great/1'), 'extra/1'))
        self.assertEqual(self.hg.pattern2edges((None, 'graphbrain/1', None)), {('is', 'graphbrain/1', 'great/1')})
        self.assertEqual(self.hg.pattern2edges(('is', 'graphbrain/1', None)), {('is', 'graphbrain/1', 'great/1')})
        self.assertEqual(self.hg.pattern2edges(('x', None, None)), set())
        self.assertEqual(self.hg.pattern2edges(('says', None, ('is', 'graphbrain/1', 'great/1'))),
                         {('says', 'mary/1', ('is', 'graphbrain/1', 'great/1'))})
        self.hg.remove(('is', 'graphbrain/1', 'great/1'))
        self.hg.remove(('says', 'mary/1'))
        self.hg.remove(('says', 'mary/1', ('is', 'graphbrain/1', 'great/1')))
        self.hg.remove(('says', 'mary/1', ('is', 'graphbrain/1', 'great/1'), 'extra/1'))

    def test_pattern2edges_open_ended(self):
        self.hg.add(('is', 'graphbrain/1', 'great/1'))
        self.hg.add(('says', 'mary/1'))
        self.hg.add(('says', 'mary/1', ('is', 'graphbrain/1', 'great/1')))
        self.hg.add(('says', 'mary/1', ('is', 'graphbrain/1', 'great/1'), 'extra/1'))
        self.assertEqual(self.hg.pattern2edges((None, 'graphbrain/1', None), open_ended=True),
                         {('is', 'graphbrain/1', 'great/1')})
        self.assertEqual(self.hg.pattern2edges(('is', 'graphbrain/1', None), open_ended=True),
                         {('is', 'graphbrain/1', 'great/1')})
        self.assertEqual(self.hg.pattern2edges(('x', None, None), open_ended=True),
                         set())
        self.assertEqual(self.hg.pattern2edges(('says', None, ('is', 'graphbrain/1', 'great/1')), open_ended=True),
                         {('says', 'mary/1', ('is', 'graphbrain/1', 'great/1')),
                          ('says', 'mary/1', ('is', 'graphbrain/1', 'great/1'), 'extra/1')})
        self.hg.remove(('is', 'graphbrain/1', 'great/1'))
        self.hg.remove(('says', 'mary/1'))
        self.hg.remove(('says', 'mary/1', ('is', 'graphbrain/1', 'great/1')))
        self.hg.remove(('says', 'mary/1', ('is', 'graphbrain/1', 'great/1'), 'extra/1'))

    def test_star(self):
        self.hg.add(('is', 'graphbrain/1', 'great/1'))
        self.assertEqual(self.hg.star('graphbrain/1'), {('is', 'graphbrain/1', 'great/1')})
        self.assertEqual(self.hg.star('graphbrain/2'), set())
        self.hg.remove(('is', 'graphbrain/1', 'great/1'))
        self.hg.add(('says', 'mary/1', ('is', 'graphbrain/1', 'great/1')))
        self.assertEqual(self.hg.star(('is', 'graphbrain/1', 'great/1')),
                         {('says', 'mary/1', ('is', 'graphbrain/1', 'great/1'))})

    def test_star_limit(self):
        self.hg.destroy()
        self.hg.add(('is', 'graphbrain/1', 'great/1'))
        self.hg.add(('is', 'graphbrain/1', 'great/2'))
        self.hg.add(('is', 'graphbrain/1', 'great/3'))
        self.assertEqual(len(self.hg.star('graphbrain/1')), 3)
        self.assertEqual(len(self.hg.star('graphbrain/1', limit=1)), 1)
        self.assertEqual(len(self.hg.star('graphbrain/1', limit=2)), 2)
        self.assertEqual(len(self.hg.star('graphbrain/1', limit=10)), 3)

    def test_symbols_with_root(self):
        self.hg.add(('is', 'graphbrain/1', 'great/1'))
        self.assertEqual(self.hg.symbols_with_root('graphbrain'), {'graphbrain/1'})
        self.hg.add(('is', 'graphbrain/2', 'great/1'))
        self.assertEqual(self.hg.symbols_with_root('graphbrain'), {'graphbrain/1', 'graphbrain/2'})
        self.hg.remove(('is', 'graphbrain/1', 'great/1'))
        self.hg.remove(('is', 'graphbrain/2', 'great/1'))

    def test_edges_with_symbols(self):
        self.hg.add(('is', 'graphbrain/1', 'great/1'))
        self.hg.add(('is', 'graphbrain/1', 'great/2'))
        self.assertEqual(self.hg.edges_with_symbols(('graphbrain/1',), 'great'),
                         {('is', 'graphbrain/1', 'great/1'), ('is', 'graphbrain/1', 'great/2')})
        self.assertEqual(self.hg.edges_with_symbols(('graphbrain/1', 'is'), 'great'),
                         {('is', 'graphbrain/1', 'great/1'), ('is', 'graphbrain/1', 'great/2')})
        self.assertEqual(self.hg.edges_with_symbols(('graphbrain/1',), 'grea'), set())
        self.hg.remove(('is', 'graphbrain/1', 'great/1'))
        self.hg.remove(('is', 'graphbrain/1', 'great/2'))

    def test_attributes_vertex(self):
        self.hg.destroy()
        self.hg.add(('is', 'graphbrain/1', 'great/1'))
        self.assertEqual(self.hg.get_int_attribute('graphbrain/1', 'foo'), None)
        self.assertEqual(self.hg.get_int_attribute('graphbrain/1', 'foo', 0), 0)
        self.hg.set_attribute('graphbrain/1', 'foo', 66)
        self.assertEqual(self.hg.get_int_attribute('graphbrain/1', 'foo'), 66)
        self.hg.inc_attribute('graphbrain/1', 'foo')
        self.assertEqual(self.hg.get_int_attribute('graphbrain/1', 'foo'), 67)
        self.hg.dec_attribute('graphbrain/1', 'foo')
        self.assertEqual(self.hg.get_int_attribute('graphbrain/1', 'foo'), 66)
        self.hg.set_attribute('graphbrain/1', 'bar', -.77)
        self.assertEqual(self.hg.get_float_attribute('graphbrain/1', 'bar'), -.77)
        self.hg.set_attribute('graphbrain/1', 'label', 'x0 x0 | test \\ test')
        self.assertEqual(self.hg.get_str_attribute('graphbrain/1', 'label'), 'x0 x0   test   test')

    def test_attributes_edge(self):
        self.hg.destroy()
        self.hg.add(('is', 'graphbrain/1', 'great/1'))
        self.assertEqual(self.hg.get_int_attribute(('is', 'graphbrain/1', 'great/1'), 'foo'), None)
        self.assertEqual(self.hg.get_int_attribute(('is', 'graphbrain/1', 'great/1'), 'foo', 0), 0)
        self.hg.set_attribute(('is', 'graphbrain/1', 'great/1'), 'foo', 66)
        self.assertEqual(self.hg.get_int_attribute(('is', 'graphbrain/1', 'great/1'), 'foo'), 66)
        self.hg.inc_attribute(('is', 'graphbrain/1', 'great/1'), 'foo')
        self.assertEqual(self.hg.get_int_attribute(('is', 'graphbrain/1', 'great/1'), 'foo'), 67)
        self.hg.dec_attribute(('is', 'graphbrain/1', 'great/1'), 'foo')
        self.assertEqual(self.hg.get_int_attribute(('is', 'graphbrain/1', 'great/1'), 'foo'), 66)
        self.hg.set_attribute(('is', 'graphbrain/1', 'great/1'), 'bar', -.77)
        self.assertEqual(self.hg.get_float_attribute(('is', 'graphbrain/1', 'great/1'), 'bar'), -.77)
        self.hg.set_attribute(('is', 'graphbrain/1', 'great/1'), 'label', 'x0 x0 | test \\ test')
        self.assertEqual(self.hg.get_str_attribute(('is', 'graphbrain/1', 'great/1'), 'label'), 'x0 x0   test   test')

    def test_degree(self):
        self.hg.destroy()
        self.assertEqual(self.hg.degree('graphbrain/1'), 0)
        self.hg.add(('is', 'graphbrain/1', 'great/1'))
        self.assertEqual(self.hg.degree('graphbrain/1'), 1)
        self.assertEqual(self.hg.degree('great/1'), 1)
        self.hg.add(('size', 'graphbrain/1', 7))
        self.assertEqual(self.hg.degree('graphbrain/1'), 2)
        self.assertEqual(self.hg.degree('great/1'), 1)
        self.hg.remove(('is', 'graphbrain/1', 'great/1'))
        self.assertEqual(self.hg.degree('graphbrain/1'), 1)
        self.assertEqual(self.hg.degree('great/1'), 0)
        self.hg.remove(('size', 'graphbrain/1', 7))
        self.assertEqual(self.hg.degree('graphbrain/1'), 0)

    def test_timestamp(self):
        self.hg.destroy()
        self.assertEqual(self.hg.timestamp('graphbrain/1'), -1)
        self.hg.add(('is', 'graphbrain/1', 'great/1'), timestamp=123456789)
        self.assertEqual(self.hg.timestamp('graphbrain/1'), 123456789)
        self.assertEqual(self.hg.timestamp('great/1'), 123456789)
        self.assertEqual(self.hg.timestamp(('is', 'graphbrain/1', 'great/1')), 123456789)
        self.hg.remove(('is', 'graphbrain/1', 'great/1'))
        self.assertEqual(self.hg.timestamp('graphbrain/1'), 123456789)
        self.assertEqual(self.hg.timestamp('great/1'), 123456789)
        self.assertEqual(self.hg.timestamp(('is', 'graphbrain/1', 'great/1')), -1)

    def test_all(self):
        self.hg.destroy()
        self.hg.add(('size', 'graphbrain/1', -7.0))
        self.hg.add(('is', 'graphbrain/1', 'great/1'))
        self.hg.add(('src', 'mary/1', ('is', 'graphbrain/1', 'great/1')))

        labels = set([edge2str(v) for v in self.hg.all()])
        self.assertEqual(labels, {'size', 'graphbrain/1', '-7.0', 'is', 'great/1', 'src', 'mary/1',
                                  '(size graphbrain/1 -7.0)', '(is graphbrain/1 great/1)',
                                  '(src mary/1 (is graphbrain/1 great/1))'})
        self.hg.destroy()
        labels = set(self.hg.all())
        self.assertEqual(labels, set())

    def test_all_attributes(self):
        self.hg.destroy()
        self.hg.add(('size', 'graphbrain/1', -7.0))
        self.hg.add(('is', 'graphbrain/1', 'great/1'))
        self.hg.add(('src', 'mary/1', ('is', 'graphbrain/1', 'great/1')))

        labels = set(['%s %s' % (edge2str(t[0]), t[1]['d']) for t in self.hg.all_attributes()])
        self.assertEqual(labels, {'size 1', 'graphbrain/1 2', '-7.0 1', 'is 1', 'great/1 1', 'src 1', 'mary/1 1',
                                  '(size graphbrain/1 -7.0) 0', '(is graphbrain/1 great/1) 1',
                                  '(src mary/1 (is graphbrain/1 great/1)) 0'})
        self.hg.destroy()
        labels = set(self.hg.all_attributes())
        self.assertEqual(labels, set())

    def test_counters(self):
        self.hg.destroy()
        self.hg.add(('is', 'graphbrain/1', 'great/1'))
        self.assertEqual(self.hg.symbol_count(), 3)
        self.assertEqual(self.hg.edge_count(), 1)
        self.assertEqual(self.hg.total_degree(), 3)
        self.hg.add(('says', 'mary/1', ('is', 'graphbrain/1', 'great/1')))
        self.assertEqual(self.hg.symbol_count(), 5)
        self.assertEqual(self.hg.edge_count(), 2)
        self.assertEqual(self.hg.total_degree(), 6)
        self.hg.remove(('is', 'graphbrain/1', 'great/1'))
        self.assertEqual(self.hg.edge_count(), 1)
        self.assertEqual(self.hg.total_degree(), 3)
        self.hg.remove(('says', 'mary/1', ('is', 'graphbrain/1', 'great/1')))
        self.assertEqual(self.hg.edge_count(), 0)
        self.assertEqual(self.hg.total_degree(), 0)


if __name__ == '__main__':
    unittest.main()
