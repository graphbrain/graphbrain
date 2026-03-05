import unittest

from tests.hypergraph import Hypergraph


class TestSQLite(Hypergraph, unittest.TestCase):
    def setUp(self):
        self.hg_str = 'test.db'
        super().setUp()
