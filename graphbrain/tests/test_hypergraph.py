import unittest
from graphbrain.hypergraph import HyperGraph
import graphbrain.constants as const


class TestHypergraph(unittest.TestCase):

    def setUp(self):
        params = {'backend': 'leveldb',
                  'hg': 'test.hg'}
        self.hg = HyperGraph(params)

    def tearDown(self):
        self.hg.close()

    def test_ego(self):
        self.hg.add(('is', 'graphbrain/1', 'great/1'))
        self.hg.add(('is', 'graphbrain/1', 'great/2'))
        self.assertEqual(self.hg.ego('graphbrain/1'), {'graphbrain/1', 'is', 'great/1', 'great/2'})
        self.hg.remove(('is', 'graphbrain/1', 'great/1'))
        self.hg.remove(('is', 'graphbrain/1', 'great/2'))
        self.hg.add_belief("mary/1", ("is", "graphbrain/1", "great/1"))
        self.assertEqual(self.hg.ego('mary/1'), {'mary/1', 'src/gb', 'graphbrain/1', 'is', 'great/1'})
        self.hg.remove_belief("mary/1", ("is", "graphbrain/1", "great/1"))

    def test_beliefs(self):
        self.hg.add_belief("mary/1", ("is", "graphbrain/1", "great/1"))
        self.assertEqual(self.hg.sources(("is", "graphbrain/1", "great/1")), {"mary/1"})
        self.assertTrue(self.hg.is_belief(("is", "graphbrain/1", "great/1")))
        self.hg.add(('is', 'graphbrain/1', 'great/2'))
        self.assertFalse(self.hg.is_belief(("is", "graphbrain/1", "great/2")))
        self.hg.add_belief("john/1", ("is", "graphbrain/1", "great/1"))
        self.assertEqual(self.hg.sources(("is", "graphbrain/1", "great/1")), {"mary/1", "john/1"})
        self.hg.remove_belief("mary/1", ("is", "graphbrain/1", "great/1"))
        self.assertTrue(self.hg.exists(("is", "graphbrain/1", "great/1")))
        self.hg.remove_belief("john/1", ("is", "graphbrain/1", "great/1"))
        self.assertFalse(self.hg.exists(("is", "graphbrain/1", "great/1")))

    def test_timestamp_beliefs(self):
        self.hg.destroy()
        self.assertEqual(self.hg.timestamp("graphbrain/1"), -1)
        self.hg.add_belief("mary/1", ("is", "graphbrain/1", "great/1"), timestamp=123456789)
        self.assertEqual(self.hg.timestamp("graphbrain/1"), 123456789)
        self.assertEqual(self.hg.timestamp("great/1"), 123456789)
        self.assertEqual(self.hg.timestamp("mary/1"), 123456789)
        self.assertEqual(self.hg.timestamp(("is", "graphbrain/1", "great/1")), 123456789)
        self.assertEqual(self.hg.timestamp((const.source, ("is", "graphbrain/1", "great/1"), "mary/1")), 123456789)
        self.hg.add_belief("john/1", ("is", "graphbrain/1", "great/1"))
        self.assertEqual(self.hg.timestamp("graphbrain/1"), 123456789)
        self.assertEqual(self.hg.timestamp("great/1"), 123456789)
        self.assertEqual(self.hg.timestamp("john/1"), -1)
        self.assertEqual(self.hg.timestamp(("is", "graphbrain/1", "great/1")), 123456789)
        self.assertEqual(self.hg.timestamp((const.source, ("is", "graphbrain/1", "great/1"), "john/1")), -1)
        self.hg.remove_belief("mary/1", ("is", "graphbrain/1", "great/1"))
        self.assertEqual(self.hg.timestamp("graphbrain/1"), 123456789)
        self.assertEqual(self.hg.timestamp("great/1"), 123456789)
        self.assertEqual(self.hg.timestamp("mary/1"), 123456789)
        self.assertEqual(self.hg.timestamp(("is", "graphbrain/1", "great/1")), 123456789)
        self.assertEqual(self.hg.timestamp((const.source, ("is", "graphbrain/1", "great/1"), "mary/1")), -1)
        self.hg.remove_belief("john/1", ("is", "graphbrain/1", "great/1"))
        self.assertEqual(self.hg.timestamp("graphbrain/1"), 123456789)
        self.assertEqual(self.hg.timestamp("great/1"), 123456789)
        self.assertEqual(self.hg.timestamp(("is", "graphbrain/1", "great/1")), -1)
        self.assertEqual(self.hg.timestamp((const.source, ("is", "graphbrain/1", "great/1"), "john/1")), -1)


if __name__ == '__main__':
    unittest.main()
