import unittest
from gb.hypergraph import HyperGraph
import gb.tests.aux_backend as ab


class TestHypergraph(ab.AuxBackend):

    def setUp(self):
        params = {'backend': 'leveldb',
                  'hg': 'test.hg'}
        self.hg = HyperGraph(params)

    def tearDown(self):
        self.hg.close()

if __name__ == '__main__':
    unittest.main()
