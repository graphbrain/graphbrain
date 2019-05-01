import unittest
from graphbrain import *
from graphbrain.hypergraphs.permutations import *


class TestPermutations(unittest.TestCase):
    def test_fact(self):
        self.assertEqual(fact(-10), 0)
        self.assertEqual(fact(0), 0)
        self.assertEqual(fact(1), 1)
        self.assertEqual(fact(2), 2)
        self.assertEqual(fact(3), 6)
        self.assertEqual(fact(4), 24)
        self.assertEqual(fact(5), 120)

    def test_nthperm(self):
        self.assertEqual(nthperm(10, 2), (0, 1, 2, 3, 4, 5, 6, 8, 7, 9))

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

    def test_permutations2(self):
        perm = permutate(('is/pd.sc', ('my/m', 'name/cn.s'), 'mary/cp.s'), 0)
        self.assertEqual(perm, ('is/pd.sc',
                                ('my/m', 'name/cn.s'), 'mary/cp.s'))
        perm = permutate(('is/pd.sc', ('my/m', 'name/cn.s'), 'mary/cp.s'), 1)
        self.assertEqual(perm, ('is/pd.sc',
                                'mary/cp.s', ('my/m', 'name/cn.s')))
        perm = permutate(('is/pd.sc', ('my/m', 'name/cn.s'), 'mary/cp.s'), 2)
        self.assertEqual(perm, (('my/m', 'name/cn.s'),
                                'is/pd.sc', 'mary/cp.s'))
        perm = permutate(('is/pd.sc', ('my/m', 'name/cn.s'), 'mary/cp.s'), 3)
        self.assertEqual(perm, (('my/m', 'name/cn.s'),
                                'mary/cp.s', 'is/pd.sc'))
        perm = permutate(('is/pd.sc', ('my/m', 'name/cn.s'), 'mary/cp.s'), 4)
        self.assertEqual(perm, ('mary/cp.s',
                                'is/pd.sc', ('my/m', 'name/cn.s')))
        perm = permutate(('is/pd.sc', ('my/m', 'name/cn.s'), 'mary/cp.s'), 5)
        self.assertEqual(perm, ('mary/cp.s',
                                ('my/m', 'name/cn.s'), 'is/pd.sc'))

    def test_unpermutate(self):
        edge = unpermutate(('a', 'b'), 0)
        self.assertEqual(edge, ('a', 'b'))
        edge = unpermutate(('b', 'a'), 1)
        self.assertEqual(edge, ('a', 'b'))

        edge = unpermutate(('a', 'b', 'c'), 0)
        self.assertEqual(edge, ('a', 'b', 'c'))
        edge = unpermutate(('a', 'c', 'b'), 1)
        self.assertEqual(edge, ('a', 'b', 'c'))
        edge = unpermutate(('b', 'a', 'c'), 2)
        self.assertEqual(edge, ('a', 'b', 'c'))
        edge = unpermutate(('b', 'c', 'a'), 3)
        self.assertEqual(edge, ('a', 'b', 'c'))
        edge = unpermutate(('c', 'a', 'b'), 4)
        self.assertEqual(edge, ('a', 'b', 'c'))
        edge = unpermutate(('c', 'b', 'a'), 5)
        self.assertEqual(edge, ('a', 'b', 'c'))

        edge = unpermutate(('a', 'b', 'c', 'd'), 0)
        self.assertEqual(edge, ('a', 'b', 'c', 'd'))

        edge = unpermutate(('a', 'b', 'd', 'c'), 1)
        self.assertEqual(edge, ('a', 'b', 'c', 'd'))

    def test_unpermutate2(self):
        edge = unpermutate(('is/pd.sc', ('my/m', 'name/cn.s'), 'mary/cp.s'), 0)
        self.assertEqual(edge, ('is/pd.sc',
                                ('my/m', 'name/cn.s'), 'mary/cp.s'))
        edge = unpermutate(('is/pd.sc', 'mary/cp.s', ('my/m', 'name/cn.s')), 1)
        self.assertEqual(edge, ('is/pd.sc',
                                ('my/m', 'name/cn.s'), 'mary/cp.s'))
        edge = unpermutate((('my/m', 'name/cn.s'), 'is/pd.sc', 'mary/cp.s'), 2)
        self.assertEqual(edge, ('is/pd.sc',
                                ('my/m', 'name/cn.s'), 'mary/cp.s'))
        edge = unpermutate((('my/m', 'name/cn.s'), 'mary/cp.s', 'is/pd.sc'), 3)
        self.assertEqual(edge, ('is/pd.sc',
                                ('my/m', 'name/cn.s'), 'mary/cp.s'))
        edge = unpermutate(('mary/cp.s', 'is/pd.sc', ('my/m', 'name/cn.s')), 4)
        self.assertEqual(edge, ('is/pd.sc',
                                ('my/m', 'name/cn.s'), 'mary/cp.s'))
        edge = unpermutate(('mary/cp.s', ('my/m', 'name/cn.s'), 'is/pd.sc'), 5)
        self.assertEqual(edge, ('is/pd.sc',
                                ('my/m', 'name/cn.s'), 'mary/cp.s'))

    def test_first_permutation(self):
        self.assertEqual(first_permutation(2, [0]), 0)
        self.assertEqual(first_permutation(3, [0]), 0)
        self.assertEqual(first_permutation(4, [0]), 0)
        self.assertEqual(first_permutation(4, [1]), 6)
        self.assertEqual(first_permutation(4, [0, 1]), 0)
        self.assertEqual(first_permutation(4, [0, 2]), 2)
        self.assertEqual(first_permutation(4, [1, 2]), 8)
        self.assertEqual(first_permutation(4, [1, 3]), 10)
        self.assertEqual(first_permutation(4, [0, 2, 4]), 4)
        self.assertEqual(first_permutation(4, [1, 2, 4]), 10)

    def test_do_with_edge_permutations(self):
        output = []

        def accumulate(perm_str):
            output.append(perm_str)

        do_with_edge_permutations(('a', 'b', 'c'), accumulate)
        self.assertEqual(output, ['a b c 0', 'a c b 1', 'b a c 2', 'b c a 3',
                                  'c a b 4', 'c b a 5'])

    def test_perm2edge(self):
        edge = perm2edge(' a b c 0')
        self.assertEqual(edge, ('a', 'b', 'c'))
        edge = perm2edge(' a c b 1')
        self.assertEqual(edge, ('a', 'b', 'c'))
        edge = perm2edge(' b a c 2')
        self.assertEqual(edge, ('a', 'b', 'c'))
        edge = perm2edge(' b c a 3')
        self.assertEqual(edge, ('a', 'b', 'c'))
        edge = perm2edge(' c a b 4')
        self.assertEqual(edge, ('a', 'b', 'c'))
        edge = perm2edge(' c b a 5')
        self.assertEqual(edge, ('a', 'b', 'c'))

    def test_str_plus_1(self):
        self.assertEqual(str_plus_1('graphbrain'), 'graphbraio')
        self.assertEqual(str_plus_1('zzz'), 'zz{')


if __name__ == '__main__':
    unittest.main()
