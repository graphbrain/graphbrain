import unittest
import gb.hypergraph.symbol as sym


class TestSymbol(unittest.TestCase):

    def test_hashed(self):
        self.assertEqual(sym.hashed('graphbrain/1'), '821dd667c0d1e35b')

    def test_sym_type(self):
        self.assertEqual(sym.sym_type('graphbrain/1'), sym.SymbolType.CONCEPT)
        self.assertEqual(sym.sym_type(42), sym.SymbolType.INTEGER)
        self.assertEqual(sym.sym_type(-7.9), sym.SymbolType.FLOAT)
        self.assertEqual(sym.sym_type('http://graphbrain.org'), sym.SymbolType.URL)
        self.assertEqual(sym.sym_type('https://graphbrain.org'), sym.SymbolType.URL)

    def test_parts(self):
        self.assertEqual(sym.parts('graphbrain/1'), ['graphbrain', '1'])
        self.assertEqual(sym.parts('graphbrain'), ['graphbrain'])
        self.assertEqual(sym.parts('http://graphbrain.org'), ['http://graphbrain.org'])
        self.assertEqual(sym.parts(1), [1])
        self.assertEqual(sym.parts(1.), [1.])

    def test_root(self):
        self.assertEqual(sym.root('graphbrain/1'), 'graphbrain')
        self.assertEqual(sym.root('graphbrain'), 'graphbrain')
        self.assertEqual(sym.root('http://graphbrain.org'), 'http://graphbrain.org')
        self.assertEqual(sym.root(1), 1)
        self.assertEqual(sym.root(1.), 1.)

    def test_nspace(self):
        self.assertEqual(sym.nspace('graphbrain/1'), '1')
        self.assertEqual(sym.nspace('graphbrain'), None)
        self.assertEqual(sym.nspace('http://graphbrain.org'), None)
        self.assertEqual(sym.nspace(1), None)
        self.assertEqual(sym.nspace(1.), None)

    def test_is_root(self):
        self.assertFalse(sym.is_root('graphbrain/1'))
        self.assertTrue(sym.is_root('graphbrain'))
        self.assertTrue(sym.is_root('http://graphbrain.org'))
        self.assertTrue(sym.is_root(1))
        self.assertTrue(sym.is_root(1.))

    def test_build(self):
        self.assertEqual(sym.build(['graphbrain', '1']), 'graphbrain/1')

    def test_is_negative(self):
        self.assertTrue(sym.is_negative('~is'))
        self.assertFalse(sym.is_negative('is'))

    def test_negative(self):
        self.assertEqual(sym.negative('~is'), 'is')
        self.assertEqual(sym.negative('is'), '~is')


if __name__ == '__main__':
    unittest.main()
