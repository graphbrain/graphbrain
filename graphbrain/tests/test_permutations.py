import unittest
from graphbrain import *
from graphbrain.hypergraphs.permutations import *


class TestPermutations(unittest.TestCase):
    def test_permutations(self):
        perm = permutate(('a', 'b'), 0)
        self.assertEqual(perm, ('a', 'b'))
        perm = permutate(('a', 'b'), 1)
        self.assertEqual(perm, ('b', 'a'))

        perm = permutate(('a', 'b', 'c'), 0)
        self.assertEqual(perm, ('a', 'b', 'c'))
        perm = permutate(('a', 'b', 'c'), 1)
        self.assertEqual(perm, ('a', 'c', 'b'))
        perm = permutate(('a', 'b', 'c'), 2)
        self.assertEqual(perm, ('b', 'a', 'c'))
        perm = permutate(('a', 'b', 'c'), 3)
        self.assertEqual(perm, ('b', 'c', 'a'))
        perm = permutate(('a', 'b', 'c'), 4)
        self.assertEqual(perm, ('c', 'a', 'b'))
        perm = permutate(('a', 'b', 'c'), 5)
        self.assertEqual(perm, ('c', 'b', 'a'))

        perm = permutate(('a', 'b', 'c', 'd'), 0)
        self.assertEqual(perm, ('a', 'b', 'c', 'd'))

        perm = permutate(('a', 'b', 'c', 'd'), 1)
        self.assertEqual(perm, ('a', 'b', 'd', 'c'))


if __name__ == '__main__':
    unittest.main()
