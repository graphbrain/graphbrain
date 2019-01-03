import unittest
from graphbrain.funs import *


class TestSymbol(unittest.TestCase):

    def test_hashed(self):
        self.assertEqual(hashed('graphbrain/1'), '821dd667c0d1e35b')

    def test_sym_type(self):
        self.assertEqual(symbol_type('graphbrain/1'), SymbolType.CONCEPT)
        self.assertEqual(symbol_type(42), SymbolType.INTEGER)
        self.assertEqual(symbol_type(-7.9), SymbolType.FLOAT)
        self.assertEqual(symbol_type('http://graphbrain.org'), SymbolType.URL)
        self.assertEqual(symbol_type('https://graphbrain.org'), SymbolType.URL)

    def test_parts(self):
        self.assertEqual(symbol_parts('graphbrain/1'), ['graphbrain', '1'])
        self.assertEqual(symbol_parts('graphbrain'), ['graphbrain'])
        self.assertEqual(symbol_parts('http://graphbrain.org'), ['http://graphbrain.org'])
        self.assertEqual(symbol_parts(1), [1])
        self.assertEqual(symbol_parts(1.), [1.])

    def test_root(self):
        self.assertEqual(symbol_root('graphbrain/1'), 'graphbrain')
        self.assertEqual(symbol_root('graphbrain'), 'graphbrain')
        self.assertEqual(symbol_root('http://graphbrain.org'), 'http://graphbrain.org')
        self.assertEqual(symbol_root(1), 1)
        self.assertEqual(symbol_root(1.), 1.)

    def test_nspace(self):
        self.assertEqual(symbol_namespace('graphbrain/1'), '1')
        self.assertEqual(symbol_namespace('graphbrain'), None)
        self.assertEqual(symbol_namespace('http://graphbrain.org'), None)
        self.assertEqual(symbol_namespace(1), None)
        self.assertEqual(symbol_namespace(1.), None)

    def test_is_root(self):
        self.assertFalse(is_root('graphbrain/1'))
        self.assertTrue(is_root('graphbrain'))
        self.assertTrue(is_root('http://graphbrain.org'))
        self.assertTrue(is_root(1))
        self.assertTrue(is_root(1.))

    def test_build(self):
        self.assertEqual(build_symbol('graphbrain', '1'), 'graphbrain/1')


if __name__ == '__main__':
    unittest.main()
