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

    def test_replace_atom_part(self):
        self.assertEqual(replace_atom_part('graphbrain/1', 0, 'x'), 'x/1')
        self.assertEqual(replace_atom_part('xxx/1/yyy', 1, '77'), 'xxx/77/yyy')

    def test_str2atom(self):
        self.assertEqual(str2atom('graph brain/(1).'), 'graph_brain__1__')

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

    def test_label(self):
        self.assertEqual(label('some_thing/cn.s/.'), 'some thing')
        self.assertEqual(label(('red/m', 'shoes/c')), 'red shoes')
        self.assertEqual(label(('of/b', 'capital/c', 'germany/c')),
                         'capital of germany')
        self.assertEqual(label(('+/b/.', 'capital/c', 'germany/c')),
                         'capital germany')
        self.assertEqual(label(('of/b', 'capital/c', 'west/c', 'germany/c')),
                         'capital of west germany')
        self.assertEqual(label(('of/b', 'capital/c',
                                ('and/b', 'belgium/c', 'europe/c'))),
                         'capital of belgium and europe')

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

    def test_edge_matches_pattern_simple(self):
        self.assertTrue(edge_matches_pattern(('a', 'b'), ('a', 'b')))
        self.assertFalse(edge_matches_pattern(('a', 'b'), ('a', 'a')))

    def test_edge_matches_pattern_wildcard(self):
        self.assertTrue(edge_matches_pattern(('is/pd.sc',
                                              'graphbrain/cp.s', 'great/c'),
                                             ('is/pd.sc',
                                              'graphbrain/cp.s', '*')))
        self.assertFalse(edge_matches_pattern(('was/pd.sc',
                                               'graphbrain/cp.s', 'great/c'),
                                              ('is/pd.sc',
                                               'graphbrain/cp.s', '*')))

    def test_edge_matches_pattern_atomic_wildcard(self):
        self.assertTrue(edge_matches_pattern(('is/pd.sc',
                                              'graphbrain/cp.s', 'great/c'),
                                             ('is/pd.sc',
                                              'graphbrain/cp.s', '@')))
        self.assertFalse(edge_matches_pattern(('was/pd.sc',
                                               'graphbrain/cp.s', 'great/c'),
                                              ('is/pd.sc',
                                               'graphbrain/cp.s', '@')))

        self.assertFalse(edge_matches_pattern(('is/pd.sc', 'graphbrain/cp.s',
                                               ('fairly/m', 'great/c')),
                                              ('is/pd.sc',
                                               'graphbrain/cp.s', '@')))

    def test_edge_matches_pattern_edge_wildcard(self):
        self.assertTrue(edge_matches_pattern(('is/pd.sc', 'graphbrain/cp.s',
                                              ('fairly/m', 'great/c')),
                                             ('is/pd.sc',
                                              'graphbrain/cp.s', '&')))
        self.assertFalse(edge_matches_pattern(('is/pd.sc',
                                               'graphbrain/cp.s', 'great/c'),
                                              ('is/pd.sc',
                                               'graphbrain/cp.s', '&')))

    def test_edge_matches_pattern_open_ended(self):
        self.assertTrue(edge_matches_pattern(('is/pd.sc',
                                              'graphbrain/cp.s', 'great/c'),
                                             ('is/pd.sc',
                                              'graphbrain/cp.s', '*', '...')))
        self.assertTrue(edge_matches_pattern(('is/pd.sc', 'graphbrain/cp.s',
                                              'great/c', 'extra/c'),
                                             ('is/pd.sc',
                                              'graphbrain/cp.s', '*', '...')))
        self.assertFalse(edge_matches_pattern(('is/pd.sc', 'humanity/cp.s',
                                               'great/c', 'extra/c'),
                                              ('is/pd.sc',
                                               'graphbrain/cp.s', '*', '...')))

    def test_nest(self):
        self.assertEqual(nest('a', 'b'), ('b', 'a'))
        self.assertEqual(nest(('a', 'b'), 'c'), ('c', ('a', 'b')))
        self.assertEqual(nest(('a', 'b'), ('c', 'd'), before=True),
                         ('c', 'd', ('a', 'b')))
        self.assertEqual(nest(('a', 'b'), ('c', 'd'), before=False),
                         (('c', ('a', 'b'), 'd')))

    def test_parens(self):
        self.assertEqual(parens('a'), ('a',))
        self.assertEqual(parens(('a')), ('a',))
        self.assertEqual(parens(('a', 'b')), ('a', 'b'))

    def test_insert_first_argument(self):
        self.assertEqual(insert_first_argument('a', 'b'), ('a', 'b'))
        self.assertEqual(insert_first_argument(('a', 'b'), ('c', 'd')),
                         ('a', ('c', 'd'), 'b'))

    def test_connect(self):
        self.assertEqual(connect(('a', 'b'), ('c', 'd')),
                         ('a', 'b', 'c', 'd'))
        self.assertEqual(connect(('a', 'b'), ()), ('a', 'b'))

    def test_sequence(self):
        self.assertEqual(sequence(('a', 'b'), 'c',
                                  before=True),
                         ('c', 'a', 'b'))
        self.assertEqual(sequence(('a', 'b'), 'c',
                                  before=False),
                         ('a', 'b', 'c'))
        self.assertEqual(sequence(('a', 'b'), ('c', 'd'),
                                  before=True),
                         ('c', 'd', 'a', 'b'))
        self.assertEqual(sequence(('a', 'b'), ('c', 'd'),
                                  before=False),
                         ('a', 'b', 'c', 'd'))
        self.assertEqual(sequence(('a', 'b'), ('c', 'd'),
                                  before=True, flat=False),
                         (('c', 'd'), ('a', 'b')))
        self.assertEqual(sequence(('a', 'b'), ('c', 'd'),
                                  before=False, flat=False),
                         (('a', 'b'), ('c', 'd')))

    def test_apply_fun_to_atom(self):
        def fun(atom):
            return '{}/c'.format(atom)

        self.assertEqual(apply_fun_to_atom(fun, 'x', 'x'), 'x/c')
        self.assertEqual(apply_fun_to_atom(fun, 'x', ('a', 'b', 'x')),
                         ('a', 'b', 'x/c'))
        self.assertEqual(apply_fun_to_atom(fun, 'x', ('a', 'b', 'c')),
                         ('a', 'b', 'c'))
        self.assertEqual(apply_fun_to_atom(fun, 'x', ('a', 'x', ('b', 'x'))),
                         ('a', 'x/c', ('b', 'x/c')))

    def test_replace_atom(self):
        def fun(atom):
            return '{}/c'.format(atom)

        self.assertEqual(replace_atom('x', 'x', 'x/c'), 'x/c')
        self.assertEqual(replace_atom(('a', 'b', 'x'), 'x', 'x/c'),
                         ('a', 'b', 'x/c'))
        self.assertEqual(replace_atom(('a', 'b', 'c'), 'x', 'x/c'),
                         ('a', 'b', 'c'))
        self.assertEqual(replace_atom(('a', 'x', ('b', 'x')), 'x', 'x/c'),
                         ('a', 'x/c', ('b', 'x/c')))

    def test_atom_role(self):
        self.assertEqual(atom_role('graphbrain/cp.s/1'), ['cp', 's'])
        self.assertEqual(atom_role('graphbrain'), ['c'])

    def test_atom_type(self):
        self.assertEqual(atom_type('graphbrain/cp.s/1'), 'cp')
        self.assertEqual(atom_type('graphbrain'), 'c')

    def test_entity_type(self):
        self.assertEqual(entity_type('graphbrain/cp.s/1'), 'cp')
        self.assertEqual(entity_type('graphbrain'), 'c')
        self.assertEqual(entity_type(('is/pd.so',
                                      'graphbrain/cp.s', 'great/c')), 'rd')
        self.assertEqual(entity_type(('red/m', 'shoes/cn.p')), 'cn')
        self.assertEqual(entity_type(('before/tt', 'noon/c')), 'st')
        self.assertEqual(entity_type(('very/w', 'large/m')), 'm')
        self.assertEqual(entity_type((('very/w', 'large/m'), 'shoes/cn.p')),
                         'cn')
        self.assertEqual(entity_type(('will/a', 'be/pd.sc')), 'pd')
        self.assertEqual(entity_type((('will/a', 'be/pd.sc'),
                                      'john/cp.s', 'rich/c')), 'rd')
        self.assertEqual(entity_type(('play/x', 'piano/cn.s')), 'd')

    def test_connector_type(self):
        self.assertEqual(connector_type('graphbrain/cp.s/1'), 'cp')
        self.assertEqual(connector_type('graphbrain'), 'c')
        self.assertEqual(connector_type(('is/pd.so',
                                         'graphbrain/cp.s', 'great/c')), 'pd')
        self.assertEqual(connector_type(('red/m', 'shoes/cn.p')), 'm')
        self.assertEqual(connector_type(('before/tt', 'noon/c')), 'tt')
        self.assertEqual(connector_type(('very/w', 'large/m')), 'w')
        self.assertEqual(connector_type((('very/w', 'large/m'), 'shoes/cn.p')),
                         'm')
        self.assertEqual(connector_type(('will/a', 'be/pd.sc')), 'a')
        self.assertEqual(connector_type((('will/a', 'be/pd.sc'),
                                         'john/cp.s', 'rich/c')), 'pd')
        self.assertEqual(connector_type(('play/x', 'piano/cn.s')), 'x')

    def test_atom_with_type(self):
        self.assertEqual(atom_with_type(('+/b', 'a/cn', 'b/cp'), 'c'), 'a/cn')
        self.assertEqual(atom_with_type(('+/b', 'a/c', 'b/cp'), 'cp'), 'b/cp')
        self.assertEqual(atom_with_type(('+/b', 'a/c', 'b/cp'), 'p'), None)
        self.assertEqual(atom_with_type('a/cn', 'c'), 'a/cn')
        self.assertEqual(atom_with_type('a/cn', 'cn'), 'a/cn')
        self.assertEqual(atom_with_type('a/cn', 'cp'), None)
        self.assertEqual(atom_with_type('a/cn', 'p'), None)

    def test_predicate(self):
        self.assertEqual(predicate('graphbrain/cp.s/1'), None)
        self.assertEqual(predicate('graphbrain'), None)
        self.assertEqual(predicate(('is/pd.so', 'graphbrain/cp.s', 'great/c')),
                         'is/pd.so')
        self.assertEqual(predicate(('red/m', 'shoes/cn.p')), None)
        self.assertEqual(predicate(('before/tt', 'noon/c')), None)
        self.assertEqual(predicate(('very/w', 'large/m')), None)
        self.assertEqual(predicate((('very/w', 'large/m'), 'shoes/cn.p')),
                         None)
        self.assertEqual(predicate(('will/a', 'be/pd.sc')), 'be/pd.sc')
        self.assertEqual(predicate((('will/a', 'be/pd.sc'),
                                    'john/cp.s', 'rich/c')), 'be/pd.sc')
        self.assertEqual(predicate(('play/x', 'piano/cn.s')), None)

    def test_rel_arg_role(self):
        self.assertEqual(rel_arg_role(('is/pd.so',
                                       'graphbrain/cp.s', 'great/c'), 0), 's')
        self.assertEqual(rel_arg_role(('is/pd.so',
                                       'graphbrain/cp.s', 'great/c'), 1), 'o')
        self.assertEqual(rel_arg_role(('is/pd.so',
                                       'graphbrain/cp.s', 'great/c'), 2),
                         None)
        self.assertEqual(rel_arg_role(('is/b',
                                       'graphbrain/cp.s', 'great/c'), 0),
                         None)


if __name__ == '__main__':
    unittest.main()
