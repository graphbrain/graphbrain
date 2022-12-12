import unittest

from graphbrain.tests.hypergraph import Hypergraph


class TestLevelDB(Hypergraph, unittest.TestCase):
    def setUp(self):
        self.hg_str = 'test.hg'
        super().setUp()
