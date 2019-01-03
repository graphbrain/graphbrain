import unittest
from graphbrain.hypergraph import HyperGraph
from graphbrain.funs import *


class TestNull(unittest.TestCase):

    def __init__(self, *args, **kwargs):
        super(TestNull, self).__init__(*args, **kwargs)
        params = {'backend': 'null'}
        self.hg = HyperGraph(params)

    def test_ops_1(self):
        self.hg.add(('is', 'graphbrain/1', 'great/1'))
        self.assertFalse(self.hg.exists(('is', 'graphbrain/1', 'great/1')))
        self.hg.remove(('is', 'graphbrain/1', 'great/1'))
        self.assertFalse(self.hg.exists(('is', 'graphbrain/1', 'great/1')))

    def test_ops_2(self):
        self.hg.add(('size', 'graphbrain/1', 7))
        self.assertFalse(self.hg.exists(('size', 'graphbrain/1', 7)))
        self.hg.remove(('size', 'graphbrain/1', 7))
        self.assertFalse(self.hg.exists(('size', 'graphbrain/1', 7)))

    def test_ops_3(self):
        self.hg.add(('size', 'graphbrain/1', 7.0))
        self.assertFalse(self.hg.exists(('size', 'graphbrain/1', 7.0)))
        self.hg.remove(('size', 'graphbrain/1', 7.0))
        self.assertFalse(self.hg.exists(('size', 'graphbrain/1', 7.0)))

    def test_ops_4(self):
        self.hg.add(('size', 'graphbrain/1', -7))
        self.assertFalse(self.hg.exists(('size', 'graphbrain/1', -7)))
        self.hg.remove(('size', 'graphbrain/1', -7))
        self.assertFalse(self.hg.exists(('size', 'graphbrain/1', -7)))

    def test_ops_5(self):
        self.hg.add(('size', 'graphbrain/1', -7.0))
        self.assertFalse(self.hg.exists(('size', 'graphbrain/1', -7.0)))
        self.hg.remove(('size', 'graphbrain/1', -7.0))
        self.assertFalse(self.hg.exists(('size', 'graphbrain/1', -7.0)))

    def test_ops_6(self):
        self.hg.add(('src', 'graphbrain/1', ('size', 'graphbrain/1', -7.0)))
        self.assertFalse(self.hg.exists(('src', 'graphbrain/1', ('size', 'graphbrain/1', -7.0))))
        self.hg.remove(('src', 'graphbrain/1', ('size', 'graphbrain/1', -7.0)))
        self.assertFalse(self.hg.exists(('src', 'graphbrain/1', ('size', 'graphbrain/1', -7.0))))

    def test_destroy(self):
        self.hg.add(('src', 'graphbrain/1', ('size', 'graphbrain/1', -7.0)))
        self.assertFalse(self.hg.exists(('src', 'graphbrain/1', ('size', 'graphbrain/1', -7.0))))
        self.hg.destroy()
        self.assertFalse(self.hg.exists(('src', 'graphbrain/1', ('size', 'graphbrain/1', -7.0))))

    def test_pattern2edges(self):
        self.hg.add(('is', 'graphbrain/1', 'great/1'))
        self.hg.add(('says', 'mary/1', ('is', 'graphbrain/1', 'great/1')))
        self.assertNotEqual(self.hg.pattern2edges((None, 'graphbrain/1', None)), {('is', 'graphbrain/1', 'great/1')})
        self.assertNotEqual(self.hg.pattern2edges(('is', 'graphbrain/1', None)), {('is', 'graphbrain/1', 'great/1')})
        self.assertEqual(self.hg.pattern2edges(('x', None, None)), set())
        self.assertNotEqual(self.hg.pattern2edges(('says', None, ('is', 'graphbrain/1', 'great/1'))),
                            {('says', 'mary/1', ('is', 'graphbrain/1', 'great/1'))})
        self.hg.remove(('is', 'graphbrain/1', 'great/1'))
        self.hg.remove(('says', 'mary/1', ('is', 'graphbrain/1', 'great/1')))

    def test_star(self):
        self.hg.add(('is', 'graphbrain/1', 'great/1'))
        self.assertNotEqual(self.hg.star('graphbrain/1'), {('is', 'graphbrain/1', 'great/1')})
        self.assertEqual(self.hg.star('graphbrain/2'), set())
        self.hg.remove(('is', 'graphbrain/1', 'great/1'))

    def test_symbols_with_root(self):
        self.hg.add(('is', 'graphbrain/1', 'great/1'))
        self.assertNotEqual(self.hg.symbols_with_root('graphbrain'), {'graphbrain/1'})
        self.hg.add(('is', 'graphbrain/2', 'great/1'))
        self.assertNotEqual(self.hg.symbols_with_root('graphbrain'), {'graphbrain/1', 'graphbrain/2'})
        self.hg.remove(('is', 'graphbrain/1', 'great/1'))
        self.hg.remove(('is', 'graphbrain/2', 'great/1'))
        self.assertEqual(self.hg.symbols_with_root('graphbrain'), set())

    def test_edges_with_symbols(self):
        self.hg.add(('is', 'graphbrain/1', 'great/1'))
        self.hg.add(('is', 'graphbrain/1', 'great/2'))
        self.assertEqual(self.hg.edges_with_symbols(('graphbrain/1',), 'great'), set())
        self.assertEqual(self.hg.edges_with_symbols(('graphbrain/1', 'is'), 'great'), set())
        self.assertEqual(self.hg.edges_with_symbols(('graphbrain/1',), 'grea'), set())
        self.hg.remove(('is', 'graphbrain/1', 'great/1'))
        self.hg.remove(('is', 'graphbrain/1', 'great/2'))

    def test_degree(self):
        self.assertEqual(self.hg.degree('graphbrain/1'), 0)
        self.hg.add(('is', 'graphbrain/1', 'great/1'))
        self.assertEqual(self.hg.degree('graphbrain/1'), 0)
        self.assertEqual(self.hg.degree('great/1'), 0)
        self.hg.add(('size', 'graphbrain/1', 7))
        self.assertEqual(self.hg.degree('graphbrain/1'), 0)
        self.assertEqual(self.hg.degree('great/1'), 0)
        self.hg.remove(('is', 'graphbrain/1', 'great/1'))
        self.assertEqual(self.hg.degree('graphbrain/1'), 0)
        self.assertEqual(self.hg.degree('great/1'), 0)
        self.hg.remove(('size', 'graphbrain/1', 7))
        self.assertEqual(self.hg.degree('graphbrain/1'), 0)

    def test_timestamp(self):
        self.hg.destroy()
        self.assertEqual(self.hg.timestamp('graphbrain/1'), -1)
        self.hg.add(('is', 'graphbrain/1', 'great/1'), timestamp=123456789)
        self.assertEqual(self.hg.timestamp('graphbrain/1'), -1)
        self.assertEqual(self.hg.timestamp('great/1'), -1)
        self.assertEqual(self.hg.timestamp(('is', 'graphbrain/1', 'great/1')), -1)
        self.hg.remove(('is', 'graphbrain/1', 'great/1'))
        self.assertEqual(self.hg.timestamp('graphbrain/1'), -1)
        self.assertEqual(self.hg.timestamp('great/1'), -1)
        self.assertEqual(self.hg.timestamp(('is', 'graphbrain/1', 'great/1')), -1)

    def test_add_remove_multiple(self):
        self.hg.add((('is', 'graphbrain/1', 'great/1'), ('src', 'graphbrain/1', ('size', 'graphbrain/1', -7.0))))
        self.assertFalse(self.hg.exists(('is', 'graphbrain/1', 'great/1')))
        self.assertFalse(self.hg.exists(('src', 'graphbrain/1', ('size', 'graphbrain/1', -7.0))))
        self.hg.remove((('is', 'graphbrain/1', 'great/1'), ('src', 'graphbrain/1', ('size', 'graphbrain/1', -7.0))))
        self.assertFalse(self.hg.exists(('is', 'graphbrain/1', 'great/1')))
        self.assertFalse(self.hg.exists(('src', 'graphbrain/1', ('size', 'graphbrain/1', -7.0))))

    def test_all(self):
        self.hg.destroy()
        self.hg.add(('size', 'graphbrain/1', -7.0))
        self.hg.add(('is', 'graphbrain/1', 'great/1'))
        self.hg.add(('src', 'mary/1', ('is', 'graphbrain/1', 'great/1')))

        labels = set([edge2str(v) for v in self.hg.all()])
        self.assertNotEqual(labels, {'size', 'graphbrain/1', '-7.0', 'is', 'great/1', 'src', 'mary/1',
                                     '(size graphbrain/1 -7.0)', '(is graphbrain/1 great/1)',
                                     '(src mary/1 (is graphbrain/1 great/1))'})
        self.hg.destroy()
        labels = set(self.hg.all())
        self.assertEqual(labels, set())

    def test_all_metrics(self):
        self.hg.destroy()
        self.hg.add(('size', 'graphbrain/1', -7.0))
        self.hg.add(('is', 'graphbrain/1', 'great/1'))
        self.hg.add(('src', 'mary/1', ('is', 'graphbrain/1', 'great/1')))

        labels = set(['%s %s' % (edge2str(t[0]), t[1]['d']) for t in self.hg.all_attributes()])
        self.assertNotEqual(labels, {'size 1', 'graphbrain/1 2', '-7.0 1', 'is 1', 'great/1 1', 'src 1', 'mary/1 1',
                                     '(size graphbrain/1 -7.0) 0', '(is graphbrain/1 great/1) 1',
                                     '(src mary/1 (is graphbrain/1 great/1)) 0'})
        self.hg.destroy()
        labels = set(self.hg.all_attributes())
        self.assertEqual(labels, set())

    def test_counters(self):
        self.hg.destroy()
        self.hg.add(('is', 'graphbrain/1', 'great/1'))
        self.assertEqual(self.hg.symbol_count(), 0)
        self.assertEqual(self.hg.edge_count(), 0)
        self.hg.add(('says', 'mary/1', ('is', 'graphbrain/1', 'great/1')))
        self.assertEqual(self.hg.symbol_count(), 0)
        self.assertEqual(self.hg.edge_count(), 0)
        self.hg.remove(('is', 'graphbrain/1', 'great/1'))
        self.assertEqual(self.hg.edge_count(), 0)
        self.hg.remove(('says', 'mary/1', ('is', 'graphbrain/1', 'great/1')))
        self.assertEqual(self.hg.edge_count(), 0)
