import unittest

from graphbrain.tests.hypergraph import Hypergraph


class TestSQLite(Hypergraph, unittest.TestCase):
    def setUp(self):
        self.hg_str = 'test.db'
        super().setUp()
