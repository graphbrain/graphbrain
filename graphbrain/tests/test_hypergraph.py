import unittest
from graphbrain import *


class TestHypergraph(unittest.TestCase):

    def setUp(self):
        self.hg = hypergraph('test.hg')

    def tearDown(self):
        self.hg.close()

    def test_ego(self):
        self.hg.destroy()
        self.hg.add(('is', 'graphbrain/1', 'great/1'))
        self.hg.add(('is', 'graphbrain/1', 'great/2'))
        self.assertEqual(self.hg.ego('graphbrain/1'),
                         {'graphbrain/1', 'is', 'great/1', 'great/2'})
        self.hg.remove(('is', 'graphbrain/1', 'great/1'))
        self.hg.remove(('is', 'graphbrain/1', 'great/2'))

    def test_add_deep(self):
        self.hg.destroy()
        self.hg.add(('says', 'mary', ('is', 'graphbrain/1', 'great/1')),
                    deep=True)
        self.assertTrue(self.hg.exists(('is', 'graphbrain/1', 'great/1')))
        self.assertTrue(
            self.hg.exists(('says', 'mary',
                            ('is', 'graphbrain/1', 'great/1'))))
        self.hg.remove(('is', 'graphbrain/1', 'great/1'))
        self.hg.remove(('says', 'mary', ('is', 'graphbrain/1', 'great/1')))


if __name__ == '__main__':
    unittest.main()
