import unittest
from graphbrain.funs import *


class TestFuns(unittest.TestCase):
    def test_is_atom(self):
        self.assertTrue(is_atom('a'))
        self.assertTrue(is_atom('graphbrain/c'))
        self.assertTrue(is_atom('graphbrain/cn.p/1'))
        self.assertFalse(is_atom(('is/pd.sc', 'graphbrain/cp.s', 'great/c')))

    def test_is_edge(self):
        self.assertFalse(is_edge('a'))
        self.assertFalse(is_edge('graphbrain/c'))
        self.assertFalse(is_edge('graphbrain/cn.p/1'))
        self.assertTrue(is_edge(('is/pd.sc', 'graphbrain/cp.s', 'great/c')))

    def test_atom_parts(self):
        self.assertEqual(atom_parts('graphbrain/c'), ['graphbrain', 'c'])
        self.assertEqual(atom_parts('graphbrain'), ['graphbrain'])
        self.assertEqual(atom_parts('go/p.p.so/1'), ['go', 'p.p.so', '1'])

    def test_root(self):
        self.assertEqual(root('graphbrain/c'), 'graphbrain')
        self.assertEqual(root('go/p.p.so/1'), 'go')

    def test_build_atom(self):
        self.assertEqual(build_atom('graphbrain', 'c'), 'graphbrain/c')
        self.assertEqual(build_atom('go', 'p.p.so', '1'), 'go/p.p.so/1')

    def test_str2atom(self):
        self.assertEqual(str2atom('graph brain/(1).'), 'graph_brain__1__')

    def test_label(self):
        self.assertEqual(label('graph_brain'), 'graph brain')

    def test_split_edge_str(self):
        self.assertEqual(split_edge_str('is graphbrain/1 great/1'),
                         ('is', 'graphbrain/1', 'great/1'))
        self.assertEqual(split_edge_str('size graphbrain/1 7'),
                         ('size', 'graphbrain/1', '7'))
        self.assertEqual(split_edge_str('size graphbrain/1 7.0'),
                         ('size', 'graphbrain/1', '7.0'))
        self.assertEqual(split_edge_str('size graphbrain/1 -7'),
                         ('size', 'graphbrain/1', '-7'))
        self.assertEqual(split_edge_str('size graphbrain/1 -7.0'),
                         ('size', 'graphbrain/1', '-7.0'))
        self.assertEqual(
            split_edge_str('src graphbrain/1 (is graphbrain/1 great/1)'),
            ('src', 'graphbrain/1', '(is graphbrain/1 great/1)'))

    def test_str2ent(self):
        self.assertEqual(str2ent('(is graphbrain/1 great/1)'),
                         ('is', 'graphbrain/1', 'great/1'))
        self.assertEqual(
            str2ent('(src graphbrain/1 (is graphbrain/1 great/1))'),
            ('src', 'graphbrain/1', ('is', 'graphbrain/1', 'great/1')))
        self.assertEqual(str2ent('((is my) graphbrain/1 (super great/1))'),
                         (('is', 'my'), 'graphbrain/1', ('super', 'great/1')))
        self.assertEqual(str2ent('.'), '.')

    def test_edges2str(self):
        s = edges2str((('1', '2'), 'xxx', ('+/b', 'mary/c', 'john/c')))
        self.assertEqual(s, '(1 2) xxx (+/b mary/c john/c)')

    def test_edges2str_roots_only(self):
        s = edges2str((('1', '2'), 'xxx', ('+/b', 'mary/c', 'john/c')),
                      roots_only=True)
        self.assertEqual(s, '(1 2) xxx (+ mary john)')

    def test_ent2str(self):
        self.assertEqual(
            ent2str(('is', 'graphbrain/1', 'great/1')),
            '(is graphbrain/1 great/1)')
        self.assertEqual(
            ent2str(('src', 'graphbrain/1',
                     ('is', 'graphbrain/1', 'great/1'))),
            '(src graphbrain/1 (is graphbrain/1 great/1))')

    def test_ent2str_roots_only(self):
        self.assertEqual(
            ent2str(('is', 'graphbrain/1', 'great/1'), roots_only=True),
            '(is graphbrain great)')
        self.assertEqual(
            ent2str(('src', 'graphbrain/1', ('is', 'graphbrain/1', 'great/1')),
                    roots_only=True),
            '(src graphbrain (is graphbrain great))')

    def test_atoms(self):
        self.assertEqual(atoms(('is', 'graphbrain/1', 'great/1')),
                         {'is', 'graphbrain/1', 'great/1'})
        self.assertEqual(
            atoms(('src', 'graphbrain/2', ('is', 'graphbrain/1', 'great/1'))),
            {'is', 'graphbrain/1', 'great/1', 'src', 'graphbrain/2'})
        self.assertEqual(atoms('graphbrain/1'), {'graphbrain/1'})

    def test_depth(self):
        self.assertEqual(depth('graphbrain/1'), 0)
        self.assertEqual(depth(('is', 'graphbrain/1', 'great/1')), 1)
        self.assertEqual(depth(('is', 'graphbrain/1', ('super', 'great/1'))),
                         2)

    def test_roots(self):
        self.assertEqual(roots('graphbrain/1'), 'graphbrain')
        self.assertEqual(roots(('is', 'graphbrain/1', 'great/1')),
                         ('is', 'graphbrain', 'great'))
        self.assertEqual(roots(('is', 'graphbrain/1', ('super', 'great/1'))),
                         ('is', 'graphbrain', ('super', 'great')))

    def test_contains(self):
        edge = ('is/pd.sc', 'piron/c', ('of/b', 'capital/c', 'piripiri/c'))
        self.assertTrue(contains(edge, 'is/pd.sc'))
        self.assertTrue(contains(edge, 'piron/c'))
        self.assertTrue(contains(edge, ('of/b', 'capital/c', 'piripiri/c')))
        self.assertFalse(contains(edge, 'piripiri/c'))
        self.assertFalse(contains(edge, '1111/c'))

    def test_contains_deep(self):
        edge = ('is/pd.sc', 'piron/c', ('of/b', 'capital/c', 'piripiri/c'))
        self.assertTrue(contains(edge, 'is/pd.sc', deep=True))
        self.assertTrue(contains(edge, 'piron/c', deep=True))
        self.assertTrue(contains(edge, ('of/b', 'capital/c', 'piripiri/c'),
                                 deep=True))
        self.assertTrue(contains(edge, 'piripiri/c', deep=True))
        self.assertFalse(contains(edge, '1111/c', deep=True))

    def test_size(self):
        self.assertEqual(size('graphbrain/1'), 1)
        self.assertEqual(size(('is', 'graphbrain/1', 'great/1')), 3)
        self.assertEqual(size(('is', 'graphbrain/1', ('super', 'great/1'))), 3)
        self.assertEqual(size(('super', 'great/1')), 2)

    def test_subedges(self):
        self.assertEqual(subedges('graphbrain/1'), {'graphbrain/1'})
        self.assertEqual(subedges(('is', 'graphbrain/1', 'great/1')),
                         {'is', 'graphbrain/1', 'great/1',
                          ('is', 'graphbrain/1', 'great/1')})
        self.assertEqual(subedges(('is', 'graphbrain/1',
                                   ('super', 'great/1'))),
                         {'is', 'graphbrain/1', 'super', 'great/1',
                          ('super', 'great/1'),
                          ('is', 'graphbrain/1', ('super', 'great/1'))})


if __name__ == '__main__':
    unittest.main()
