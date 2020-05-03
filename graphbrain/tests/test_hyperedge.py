import unittest
from graphbrain.hyperedge import *


class TestHyperedge(unittest.TestCase):
    def test_hedge(self):
        self.assertEqual(hedge('(is graphbrain/1 great/1)').to_str(),
                         '(is graphbrain/1 great/1)')
        self.assertEqual(
            hedge('(src graphbrain/1 (is graphbrain/1 great/1))').to_str(),
            '(src graphbrain/1 (is graphbrain/1 great/1))')
        self.assertEqual(hedge('((is my) brain/1 (super great/1))').to_str(),
                         '((is my) brain/1 (super great/1))')
        self.assertEqual(hedge('.'), ('.',))

    def test_is_atom(self):
        self.assertTrue(hedge('a').is_atom())
        self.assertTrue(hedge('graphbrain/C').is_atom())
        self.assertTrue(hedge('graphbrain/Cn.p/1').is_atom())
        self.assertFalse(hedge('(is/Pd.sc graphbrain/Cp.s great/C)').is_atom())

    def test_atom_parts(self):
        self.assertEqual(hedge('graphbrain/C').parts(), ['graphbrain', 'C'])
        self.assertEqual(hedge('graphbrain').parts(), ['graphbrain'])
        self.assertEqual(hedge('go/P.so/1').parts(), ['go', 'P.so', '1'])

    def test_root(self):
        self.assertEqual(hedge('graphbrain/C').root(), 'graphbrain')
        self.assertEqual(hedge('go/P.so/1').root(), 'go')

    def test_build_atom(self):
        self.assertEqual(build_atom('graphbrain', 'C'), hedge('graphbrain/C'))
        self.assertEqual(build_atom('go', 'P.so', '1'), hedge('go/P.so/1'))

    def test_replace_atom_part(self):
        self.assertEqual(hedge('graphbrain/C').replace_atom_part(0, 'x'),
                         hedge('x/C'))
        self.assertEqual(hedge('xxx/1/yyy').replace_atom_part(1, '77'),
                         hedge('xxx/77/yyy'))

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

    def test_edges2str(self):
        s = edges2str((hedge('(1 2)'), hedge('xxx'),
                       hedge('(+/B mary/C john/C)')))
        self.assertEqual(s, '(1 2) xxx (+/B mary/C john/C)')

    def test_edges2str_roots_only(self):
        s = edges2str((hedge('(1 2)'), hedge('xxx'),
                       hedge('(+/B mary/C john/C)')),
                      roots_only=True)
        self.assertEqual(s, '(1 2) xxx (+ mary john)')

    def test_to_str(self):
        self.assertEqual(
            hedge('(is graphbrain/C great/C)').to_str(),
            '(is graphbrain/C great/C)')
        self.assertEqual(
            hedge('(src graphbrain/C (is graphbrain/C great/C))').to_str(),
            '(src graphbrain/C (is graphbrain/C great/C))')

    def test_ent2str_roots_only(self):
        self.assertEqual(
            hedge('(is graphbrain/C great/C)').to_str(roots_only=True),
            '(is graphbrain great)')
        self.assertEqual(
            hedge('(src graphbrain/C '
                  '(is graphbrain/C great/C))').to_str(roots_only=True),
            '(src graphbrain (is graphbrain great))')

    def test_label(self):
        self.assertEqual(hedge('some_thing/Cn.s/.').label(), 'some thing')
        self.assertEqual(hedge('(red/M shoes/C)').label(), 'red shoes')
        self.assertEqual(hedge('(of/B capital/C germany/C)').label(),
                         'capital of germany')
        self.assertEqual(hedge('(+/B/. capital/C germany/C)').label(),
                         'capital germany')
        self.assertEqual(hedge('(of/B capital/C west/C germany/C)').label(),
                         'capital of west germany')
        self.assertEqual(hedge('(of/B capital/C '
                               '(and/B belgium/C europe/C))').label(),
                         'capital of belgium and europe')

    def test_atoms(self):
        self.assertEqual(hedge('(is graphbrain/1 great/1)').atoms(),
                         {hedge('is'), hedge('graphbrain/1'),
                          hedge('great/1')})
        self.assertEqual(
            hedge('(src graphbrain/2 (is graphbrain/1 great/1))').atoms(),
            {hedge('is'), hedge('graphbrain/1'), hedge('great/1'),
             hedge('src'), hedge('graphbrain/2')})
        self.assertEqual(hedge('graphbrain/1').atoms(),
                         {hedge('graphbrain/1')})
        edge = hedge('(the/Md (of/Br mayor/Cc (the/Md city/Cs)))')
        self.assertEqual(edge.atoms(),
                         {hedge('the/Md'), hedge('of/Br'), hedge('mayor/Cc'),
                          hedge('city/Cs')})

    def test_all_atoms(self):
        self.assertEqual(hedge('(is graphbrain/1 great/1)').all_atoms(),
                         [hedge('is'), hedge('graphbrain/1'),
                          hedge('great/1')])
        self.assertEqual(
            hedge('(src graphbrain/2 (is graphbrain/1 great/1))').all_atoms(),
            [hedge('src'), hedge('graphbrain/2'), hedge('is'),
             hedge('graphbrain/1'), hedge('great/1')])
        self.assertEqual(hedge('graphbrain/1').all_atoms(),
                         [hedge('graphbrain/1')])
        edge = hedge('(the/Md (of/Br mayor/Cc (the/Md city/Cs)))')
        self.assertEqual(edge.all_atoms(),
                         [hedge('the/Md'), hedge('of/Br'), hedge('mayor/Cc'),
                          hedge('the/Md'), hedge('city/Cs')])

    def test_depth(self):
        self.assertEqual(hedge('graphbrain/1').depth(), 0)
        self.assertEqual(hedge('(is graphbrain/1 great/1)').depth(), 1)
        self.assertEqual(hedge('(is graphbrain/1 (super great/1))').depth(), 2)

    def test_roots(self):
        self.assertEqual(hedge('graphbrain/1').roots(), hedge('graphbrain'))
        self.assertEqual(hedge('(is graphbrain/1 great/1)').roots(),
                         hedge('(is graphbrain great)'))
        self.assertEqual(hedge('(is graphbrain/1 (super great/1))').roots(),
                         hedge('(is graphbrain (super great))'))

    def test_contains(self):
        edge = hedge('(is/Pd.sc piron/C (of/B capital/C piripiri/C))')
        self.assertTrue(edge.contains(hedge('is/Pd.sc')))
        self.assertTrue(edge.contains(hedge('piron/C')))
        self.assertTrue(edge.contains(hedge('(of/B capital/C piripiri/C)')))
        self.assertFalse(edge.contains(hedge('piripiri/C')))
        self.assertFalse(edge.contains(hedge('1111/C')))

    def test_contains_deep(self):
        edge = hedge('(is/Pd.sc piron/C (of/B capital/C piripiri/C))')
        self.assertTrue(edge.contains(hedge('is/Pd.sc'), deep=True))
        self.assertTrue(edge.contains(hedge('piron/C'), deep=True))
        self.assertTrue(edge.contains(hedge('(of/B capital/C piripiri/C)'),
                                      deep=True))
        self.assertTrue(edge.contains(hedge('piripiri/C'), deep=True))
        self.assertFalse(edge.contains(hedge('1111/C'), deep=True))

    def test_subedges(self):
        self.assertEqual(hedge('graphbrain/1').subedges(),
                         {hedge('graphbrain/1')})
        self.assertEqual(hedge('(is graphbrain/1 great/1)').subedges(),
                         {hedge('is'), hedge('graphbrain/1'), hedge('great/1'),
                          hedge('(is graphbrain/1 great/1)')})
        self.assertEqual(hedge('(is graphbrain/1 (super great/1))').subedges(),
                         {hedge('is'), hedge('graphbrain/1'), hedge('super'),
                          hedge('great/1'), hedge('(super great/1)'),
                          hedge('(is graphbrain/1 (super great/1))')})

    def test_match_pattern_simple(self):
        self.assertEqual(match_pattern('(a b)', '(a b)'), [{}])
        self.assertEqual(match_pattern('(a b)', '(a a)'), [])

    def test_match_pattern_wildcard(self):
        self.assertEqual(match_pattern('(is/Pd graphbrain/Cp.s great/C)',
                                       '(is/Pd graphbrain/Cp.s *X)'),
                         [{'X': hedge('great/C')}])
        self.assertEqual(match_pattern('(is/Pd graphbrain/Cp.s great/C)',
                                       '(is/Pd graphbrain/Cp.s *)'),
                         [{}])
        self.assertEqual(match_pattern('(was/Pd graphbrain /Cp.s great/C)',
                                       '(is/Pd graphbrain/Cp.s *X)'),
                         [])

    def test_match_pattern_atomic_wildcard(self):
        self.assertEqual(match_pattern('(is/Pd graphbrain/Cp.s great/C)',
                                       '(is/Pd graphbrain/Cp.s @PROP)'),
                         [{'PROP': hedge('great/C')}])
        self.assertEqual(match_pattern('(is/Pd graphbrain/Cp.s great/C)',
                                       '(is/Pd graphbrain/Cp.s @)'),
                         [{}])
        self.assertEqual(match_pattern('(was/Pd graphbrain/Cp.s great/C)',
                                       '(is/Pd graphbrain/Cp.s @PROP)'),
                         [])
        self.assertEqual(match_pattern('(is/Pd graphbrain/Cp.s '
                                       '(fairly/M great/C))',
                                       '(is/Pd graphbrain/Cp.s @PROP)'),
                         [])

    def test_match_pattern_non_atomic_wildcard(self):
        self.assertEqual(match_pattern('(is/Pd graphbrain/Cp.s '
                                       '(fairly/M great/C))',
                                       '(is/Pd graphbrain/Cp.s &PROP)'),
                         [{'PROP': hedge('(fairly/M great/C)')}])
        self.assertEqual(match_pattern('(is/Pd graphbrain/Cp.s '
                                       '(fairly/M great/C))',
                                       '(is/Pd graphbrain/Cp.s &)'),
                         [{}])
        self.assertEqual(match_pattern('(was/Pd graphbrain/Cp.s '
                                       '(fairly/M great/C))',
                                       '(is/Pd graphbrain/Cp.s &PROP)'),
                         [])
        self.assertEqual(match_pattern('(is/Pd graphbrain/Cp.s great/C)',
                                       '(is/Pd graphbrain/Cp.s &PROP)'),
                         [])

    def test_match_pattern_open_ended(self):
        self.assertEqual(match_pattern('(is/Pd graphbrain/Cp.s great/C)',
                                       '(is/Pd graphbrain/Cp.s *X ...)'),
                         [{'X': hedge('great/C')}])
        self.assertEqual(match_pattern('(is/Pd graphbrain/Cp.s great/C)',
                                       '(is/Pd graphbrain/Cp.s * ...)'),
                         [{}])
        self.assertEqual(match_pattern('(was/Pd graphbrain /Cp.s great/C)',
                                       '(is/Pd graphbrain/Cp.s *X ...)'),
                         [])
        self.assertEqual(match_pattern('(is/Pd graphbrain/Cp.s great/C)',
                                       '(is/Pd @OBJ ...)'),
                         [{'OBJ': hedge('graphbrain/Cp.s')}])
        self.assertEqual(match_pattern('(is/Pd graphbrain/Cp.s great/C)',
                                       '(is/Pd @OBJ)'),
                         [])

    def test_match_pattern_argroles(self):
        self.assertEqual(match_pattern('(is/Pd.sc graphbrain/Cp.s great/C)',
                                       '(is/Pd.sc graphbrain/Cp.s *X)'),
                         [{'X': hedge('great/C')}])
        self.assertEqual(match_pattern('(is/Pd.cs great/C graphbrain/Cp.s)',
                                       '(is/Pd.sc graphbrain/Cp.s *X)'),
                         [{'X': hedge('great/C')}])
        self.assertEqual(match_pattern('(is/Pd.sc graphbrain/Cp.s great/C)',
                                       '(is/Pd.scx graphbrain/Cp.s *X *Y)'),
                         [])
        self.assertEqual(match_pattern(
            '(is/Pd.xcs today/C great/C graphbrain/Cp.s)',
            '(is/Pd.sc graphbrain/Cp.s *X ...)'),
            [{'X': hedge('great/C')}])
        self.assertEqual(match_pattern(
            '(is/Pd.xcs today/C great/C graphbrain/Cp.s)',
            '(is/Pd.sc graphbrain/Cp.s *X)'),
            [])
        self.assertEqual(match_pattern('(is/Pd.sc graphbrain/Cp.s great/C)',
                                       '(is/Pd.sc graphbrain/Cp.s *)'),
                         [{}])
        self.assertEqual(match_pattern('(was/Pd.sc graphbrain /Cp.s great/C)',
                                       '(is/Pd.sc graphbrain/Cp.s *X)'),
                         [])

    def test_match_pattern_argroles_vars(self):
        self.assertEqual(match_pattern('(is/Pd.sc graphbrain/Cp.s great/C)',
                                       '(is/Pd.sc graphbrain/Cp.s PROP)'),
                         [{'PROP': hedge('great/C')}])
        self.assertEqual(match_pattern('(is/Pd.cs great/C graphbrain/Cp.s)',
                                       '(is/Pd.sc graphbrain/Cp.s X)'),
                         [{'X': hedge('great/C')}])
        self.assertEqual(match_pattern('(is/Pd.sc graphbrain/Cp.s great/C)',
                                       '(is/Pd.scx graphbrain/Cp.s X Y)'),
                         [])
        self.assertEqual(match_pattern(
            '(is/Pd.xcs today/C great/C graphbrain/Cp.s)',
            '(is/Pd.sc graphbrain/Cp.s X ...)'),
            [{'X': hedge('great/C')}])
        self.assertEqual(match_pattern(
            '(is/Pd.xcs today/C great/C graphbrain/Cp.s)',
            '(is/Pd.sc graphbrain/Cp.s X)'),
            [])
        self.assertEqual(match_pattern('(is/Pd.sc graphbrain/Cp.s great/C)',
                                       '(is/Pd.sc graphbrain/Cp.s XYZ)'),
                         [{'XYZ': hedge('great/C')}])
        self.assertEqual(match_pattern('(was/Pd.sc graphbrain /Cp.s great/C)',
                                       '(is/Pd.sc graphbrain/Cp.s X)'),
                         [])

    def test_match_pattern_argroles_unknown(self):
        self.assertEqual(
            match_pattern('(is/Pd.scx graphbrain/Cp.s great/C today/C)',
                          '(is/Pd.sc graphbrain/Cp.s PROP ...)'),
            [{'PROP': hedge('great/C')}])
        self.assertEqual(
            match_pattern('(is/Pd.scx graphbrain/Cp.s great/C today/C)',
                          '(is/Pd.sc graphbrain/Cp.s PROP EXTRA)'),
            [{'PROP': hedge('great/C'),
             'EXTRA': hedge('today/C')}])
        self.assertEqual(
            match_pattern(
                '(is/Pd.scx graphbrain/Cp.s great/C today/C (after/J x/C))',
                '(is/Pd.sc graphbrain/Cp.s PROP EXTRA)'),
            [])
        self.assertEqual(
            match_pattern(
                '(is/Pd.scx graphbrain/Cp.s great/C today/C (after/J x/C))',
                '(is/Pd.sc graphbrain/Cp.s PROP EXTRA ...)'),
            [{'PROP': hedge('great/C'), 'EXTRA': hedge('today/C')},
             {'PROP': hedge('great/C'), 'EXTRA': hedge('(after/J x/C)')}])
        self.assertEqual(
            match_pattern(
                '(is/Pd.scx graphbrain/Cp.s great/C today/C (after/J x/C))',
                '(is/Pd.sc graphbrain/Cp.s PROP &EXTRA1 EXTRA2)'),
            [{'PROP': hedge('great/C'),
              'EXTRA1': hedge('(after/J x/C)'),
              'EXTRA2': hedge('today/C')}])
        self.assertEqual(
            match_pattern(
                '(is/Pd.scx graphbrain/Cp.s great/C (after/J x/C) today/C)',
                '(is/Pd.sc graphbrain/Cp.s PROP &EXTRA1 EXTRA2)'),
            [{'PROP': hedge('great/C'),
              'EXTRA1': hedge('(after/J x/C)'),
              'EXTRA2': hedge('today/C')}])

    def test_match_pattern_repeated_vars(self):
        self.assertEqual(
            match_pattern(
                '(is/P graphbrain/Cp.s great/C today/C (after/J x/C))',
                '(is/P graphbrain/Cp.s PROP EXTRA EXTRA)'),
            [])
        self.assertEqual(
            match_pattern(
                '(is/P graphbrain/Cp.s great/C today/C today/C)',
                '(is/P graphbrain/Cp.s PROP EXTRA EXTRA)'),
            [{'PROP': hedge('great/C'),
             'EXTRA': hedge('today/C')}])

    def test_match_pattern_argroles_repeated_vars(self):
        self.assertEqual(
            match_pattern(
                '(is/Pd.scx graphbrain/Cp.s great/C today/C (after/J x/C))',
                '(is/Pd.sc graphbrain/Cp.s PROP EXTRA EXTRA)'),
            [])
        self.assertEqual(
            match_pattern(
                '(is/Pd.scx graphbrain/Cp.s great/C today/C today/C)',
                '(is/Pd.sc graphbrain/Cp.s PROP EXTRA EXTRA)'),
            [{'PROP': hedge('great/C'),
             'EXTRA': hedge('today/C')}])

    def test_match_pattern_repeated_vars_external(self):
        self.assertEqual(
            match_pattern(
                '(is/P graphbrain/Cp.s great/C today/C)',
                '(is/P graphbrain/Cp.s PROP EXTRA)',
                curvars={'PROP': hedge('great/C')}),
            [{'PROP': hedge('great/C'),
             'EXTRA': hedge('today/C')}])
        self.assertEqual(
            match_pattern(
                '(is/P graphbrain/Cp.s great/C today/C)',
                '(is/P graphbrain/Cp.s PROP EXTRA)',
                curvars={'PROP': hedge('error/C')}),
            [])

    def test_match_pattern_argroles_repeated_vars_external(self):
        self.assertEqual(
            match_pattern(
                '(is/Pd.scx graphbrain/Cp.s great/C today/C)',
                '(is/Pd.sc graphbrain/Cp.s PROP EXTRA)',
                curvars={'PROP': hedge('great/C')}),
            [{'PROP': hedge('great/C'),
              'EXTRA': hedge('today/C')}])
        self.assertEqual(
            match_pattern(
                '(is/Pd.scx graphbrain/Cp.s great/C today/C)',
                '(is/P.sc graphbrain/Cp.s PROP EXTRA)',
                curvars={'PROP': hedge('error/C')}),
            [])

    def test_match_pattern_deep(self):
        self.assertEqual(
            match_pattern(
                '(is/P graphbrain/Cp.s great/C today/C (after/J x/C))',
                '(is/P graphbrain/Cp.s PROP EXTRA (after/J X))'),
            [{'PROP': hedge('great/C'),
              'EXTRA': hedge('today/C'),
              'X': hedge('x/C')}])
        self.assertEqual(
            match_pattern(
                '(is/P graphbrain/Cp.s great/C today/C (after/J x/C))',
                '(is/P graphbrain/Cp.s PROP EXTRA (before/J X))'),
            [])
        self.assertEqual(
            match_pattern(
                '(is/P graphbrain/Cp.s great/C today/C (after/J x/C))',
                '(is/P graphbrain/Cp.s PROP EXTRA (after/J EXTRA))'),
            [])
        self.assertEqual(
            match_pattern(
                '(is/P graphbrain/Cp.s great/C x/C (after/J x/C))',
                '(is/P graphbrain/Cp.s PROP X (after/J X))'),
            [{'PROP': hedge('great/C'),
              'X': hedge('x/C')}])
        self.assertEqual(
            match_pattern(
                '(is/P graphbrain/Cp.s great/C x/C (after/J x/C))',
                '(is/P graphbrain/Cp.s PROP X (after/J X))',
                curvars={'X': hedge('x/C')}),
            [{'PROP': hedge('great/C'),
              'X': hedge('x/C')}])
        self.assertEqual(
            match_pattern(
                '(is/P graphbrain/Cp.s great/C x/C (after/J x/C))',
                '(is/P graphbrain/Cp.s PROP X (after/J X))',
                curvars={'X': hedge('y/C')}),
            [])

    def test_match_pattern_argroles_deep(self):
        self.assertEqual(
            match_pattern(
                '(is/Pd.scx graphbrain/Cp.s great/C today/C (after/J x/C))',
                '(is/Pd.sc graphbrain/Cp.s PROP EXTRA (after/J X))'),
            [{'PROP': hedge('great/C'),
              'EXTRA': hedge('today/C'),
              'X': hedge('x/C')}])
        self.assertEqual(
            match_pattern(
                '(is/Pd.scx graphbrain/Cp.s great/C today/C (after/J x/C))',
                '(is/Pd.sc graphbrain/Cp.s PROP EXTRA (before/J X))'),
            [])
        self.assertEqual(
            match_pattern(
                '(is/Pd.scx graphbrain/Cp.s great/C today/C (after/J x/C))',
                '(is/Pd.sc graphbrain/Cp.s PROP EXTRA (after/J EXTRA))'),
            [])
        self.assertEqual(
            match_pattern(
                '(is/Pd.scx graphbrain/Cp.s great/C x/C (after/J x/C))',
                '(is/Pd.sc graphbrain/Cp.s PROP X (after/J X))'),
            [{'PROP': hedge('great/C'),
              'X': hedge('x/C')}])
        self.assertEqual(
            match_pattern(
                '(is/Pd.scx graphbrain/Cp.s great/C x/C (after/J x/C))',
                '(is/Pd.sc graphbrain/Cp.s PROP X (after/J X))',
                curvars={'X': hedge('x/C')}),
            [{'PROP': hedge('great/C'),
              'X': hedge('x/C')}])
        self.assertEqual(
            match_pattern(
                '(is/Pd.scx graphbrain/Cp.s great/C x/C (after/J x/C))',
                '(is/Pd.sc graphbrain/Cp.s PROP X (after/J X))',
                curvars={'X': hedge('y/C')}),
            [])

    def test_match_pattern_argroles_multiple_results(self):
        self.assertEqual(
            match_pattern(
                '(is/Pd.scx graphbrain/Cp.s great/C today/C (after/J x/C))',
                '(is/Pd.sc graphbrain/Cp.s PROP EXTRA ...)'),
            [{'PROP': hedge('great/C'), 'EXTRA': hedge('today/C')},
             {'PROP': hedge('great/C'), 'EXTRA': hedge('(after/J x/C)')}])
        self.assertEqual(
            match_pattern(
                '(is/Pd.sscxx i/C graphbrain/Cp.s great/C today/C x/C)',
                '(is/Pd.sc graphbrain/Cp.s PROP EXTRA ...)'),
            [{'PROP': hedge('great/C'), 'EXTRA': hedge('i/C')},
             {'PROP': hedge('great/C'), 'EXTRA': hedge('today/C')},
             {'PROP': hedge('great/C'), 'EXTRA': hedge('x/C')}])

    def test_match_pattern_argroles_exclusions(self):
        self.assertEqual(
            match_pattern('(is/Pd.sc graphbrain/Cp.s great/C)',
                          '(is/Pd.sc~x graphbrain/Cp.s X ...)'),
            [{'X': hedge('great/C')}])
        self.assertEqual(
            match_pattern('(is/Pd.scx graphbrain/Cp.s great/C today/J)',
                          '(is/Pd.sc~x graphbrain/Cp.s X ...)'),
            [])
        self.assertEqual(
            match_pattern('(is/Pd.sc graphbrain/Cp.s great/C)',
                          '(is/Pd.sc~s graphbrain/Cp.s X ...)'),
            [{'X': hedge('great/C')}])
        self.assertEqual(
            match_pattern('(is/Pd.sc i/Cp.s graphbrain/Cp.s great/C)',
                          '(is/Pd.sc~s graphbrain/Cp.s X ...)'),
            [])

    def test_match_pattern_match_connectors(self):
        self.assertEqual(
            match_pattern('(is/P graphbrain/Cp.s great/C)',
                          '(PRED/P graphbrain/Cp.s X ...)'),
            [{'PRED': hedge('is/P'), 'X': hedge('great/C')}])
        self.assertEqual(
            match_pattern('(is/P graphbrain/Cp.s great/C)',
                          '(X/P graphbrain/Cp.s X ...)'),
            [])

    def test_match_pattern_argroles_match_connectors(self):
        self.assertEqual(
            match_pattern('(is/Pd.sc graphbrain/Cp.s great/C)',
                          '(PRED/Pd.sc graphbrain/Cp.s X ...)'),
            [{'PRED': hedge('is/Pd.sc'), 'X': hedge('great/C')}])
        self.assertEqual(
            match_pattern('(is/Pd.sc graphbrain/Cp.s great/C)',
                          '(X/Pd.sc graphbrain/Cp.s X ...)'),
            [])

    def test_edge_matches_pattern_simple(self):
        self.assertTrue(edge_matches_pattern(hedge('(a b)'), '(a b)'))
        self.assertFalse(edge_matches_pattern(hedge('(a b)'), '(a a)'))

    def test_edge_matches_pattern_wildcard(self):
        self.assertTrue(edge_matches_pattern(hedge('(is/Pd graphbrain/Cp.s '
                                                   'great/C)'),
                                             '(is/Pd graphbrain/Cp.s *)'))
        self.assertFalse(edge_matches_pattern(hedge('(was/Pd graphbrain'
                                                    '/Cp.s great/C)'),
                                              '(is/Pd graphbrain/Cp.s *)'))

    def test_edge_matches_pattern_atomic_wildcard(self):
        self.assertTrue(edge_matches_pattern(hedge('(is/Pd graphbrain/Cp.s '
                                                   'great/C)'),
                                             '(is/Pd graphbrain/Cp.s @)'))
        self.assertFalse(edge_matches_pattern(hedge('(was/Pd graphbrain'
                                                    '/Cp.s great/C)'),
                                              '(is/Pd.sc graphbrain/Cp.s @)'))

        self.assertFalse(edge_matches_pattern(hedge('(is/Pd graphbrain/Cp.s'
                                                    '(fairly/M great/C))'),
                                              '(is/Pd graphbrain/Cp.s @)'))

    def test_edge_matches_pattern_edge_wildcard(self):
        self.assertTrue(edge_matches_pattern(hedge('(is/Pd graphbrain/Cp.s'
                                                   ' (fairly/M great/C))'),
                                             '(is/Pd graphbrain/Cp.s &)'))

        self.assertFalse(edge_matches_pattern(hedge('(is/Pd graphbrain/Cp.s'
                                                    ' great/C)'),
                                              '(is/Pd graphbrain/Cp.s &)'))

    def test_edge_matches_pattern_open_ended(self):
        self.assertTrue(edge_matches_pattern(hedge('(is/Pd graphbrain/Cp.s '
                                                   'great/C)'),
                                             '(is/Pd graphbrain/Cp.s '
                                             '* ...)'))

        self.assertTrue(edge_matches_pattern(hedge('(is/Pd graphbrain/Cp.s '
                                                   'great/C extra/C)'),
                                             '(is/Pd graphbrain/Cp.s '
                                             '* ...)'))

        self.assertFalse(edge_matches_pattern(hedge('(is/Pd humanity/Cp.s '
                                                    'great/C extra/C)'),
                                              '(is/Pd graphbrain/Cp.s '
                                              '* ...)'))

    def test_nest(self):
        self.assertEqual(hedge('a').nest(hedge('b')).to_str(), '(b a)')
        self.assertEqual(hedge('(a b)').nest(hedge('c')).to_str(), '(c (a b))')
        self.assertEqual(hedge('(a b)').nest(hedge('(c d)'),
                                             before=True).to_str(),
                         '(c d (a b))')
        self.assertEqual(hedge('(a b)').nest(hedge('(c d)'),
                                             before=False).to_str(),
                         '(c (a b) d)')

    def test_insert_first_argument(self):
        self.assertEqual(hedge('a').insert_first_argument(hedge('b')).to_str(),
                         '(a b)')
        result = hedge('(a b)').insert_first_argument(hedge('(c d)'))
        self.assertEqual(result.to_str(), '(a (c d) b)')

    def test_connect(self):
        self.assertEqual(hedge('(a b)').connect(hedge('(c d)')).to_str(),
                         '(a b c d)')
        self.assertEqual(hedge('(a b)').connect(hedge('()')).to_str(), '(a b)')

    def test_sequence(self):
        ab = hedge('(a b)')
        c = hedge('c')
        cd = hedge('(c d)')
        self.assertEqual(ab.sequence(c, before=True).to_str(),
                         '(c a b)')
        self.assertEqual(ab.sequence(c, before=False).to_str(),
                         '(a b c)')
        self.assertEqual(ab.sequence(cd, before=True).to_str(),
                         '(c d a b)')
        self.assertEqual(ab.sequence(cd, before=False).to_str(),
                         '(a b c d)')
        self.assertEqual(ab.sequence(cd, before=True, flat=False).to_str(),
                         '((c d) (a b))')
        self.assertEqual(ab.sequence(cd, before=False, flat=False).to_str(),
                         '((a b) (c d))')

    def test_replace_atom(self):
        x = hedge('x')
        xc = hedge('x/C')
        self.assertEqual(hedge('x').replace_atom(x, xc).to_str(), 'x/C')
        self.assertEqual(hedge('(a b x)').replace_atom(x, xc).to_str(),
                         '(a b x/C)')
        self.assertEqual(hedge('(a b c)').replace_atom(x, xc).to_str(),
                         '(a b c)')
        self.assertEqual(hedge('(a x '
                               '(b x))').replace_atom(x, xc).to_str(),
                         '(a x/C (b x/C))')

    def test_atom_role(self):
        self.assertEqual(hedge('graphbrain/Cp.s/1').role(), ['Cp', 's'])
        self.assertEqual(hedge('graphbrain').role(), ['C'])

    def test_atom_simplify_role(self):
        self.assertEqual(hedge('graphbrain/Cp.s/1').simplify_role(),
                         hedge('graphbrain/C/1'))
        self.assertEqual(hedge('graphbrain').simplify_role(),
                         hedge('graphbrain'))
        self.assertEqual(hedge('say/Pd.|f----.sr/en').simplify_role(),
                         hedge('say/P/en'))

    def test_atom_type(self):
        self.assertEqual(hedge('graphbrain/Cp.s/1').type(), 'Cp')
        self.assertEqual(hedge('graphbrain').type(), 'C')

    def test_entity_type(self):
        self.assertEqual(hedge('(is/Pd.so graphbrain/Cp.s great/C)').type(),
                         'Rd')
        self.assertEqual(hedge('(red/M shoes/Cc.p)').type(), 'Cc')
        self.assertEqual(hedge('(before/Tt noon/C)').type(), 'St')
        self.assertEqual(hedge('(very/M large/M)').type(), 'M')
        self.assertEqual(hedge('((very/M large/M) shoes/Cc.p)').type(), 'Cc')
        self.assertEqual(hedge('(will/M be/Pd.sc)').type(), 'Pd')
        self.assertEqual(hedge('((will/M be/Pd.sc) john/Cp.s rich/C)').type(),
                         'Rd')
        self.assertEqual(hedge('(play/T piano/Cc.s)').type(), 'S')
        self.assertEqual(hedge('(and/J meat/Cc.s potatoes/Cc.p)').type(), 'C')
        self.assertEqual(
            hedge('(and/J (is/Pd.so graphbrain/Cp.s great/C))').type(), 'R')

    def test_connector_type(self):
        self.assertEqual(hedge('graphbrain/Cp.s/1').connector_type(), 'Cp')
        self.assertEqual(hedge('graphbrain').connector_type(), 'C')
        self.assertEqual(hedge('(is/Pd.so graphbrain/Cp.s '
                               'great/C)').connector_type(), 'Pd')
        self.assertEqual(hedge('(red/M shoes/Cn.p)').connector_type(), 'M')
        self.assertEqual(hedge('(before/Tt noon/C)').connector_type(), 'Tt')
        self.assertEqual(hedge('(very/M large/M)').connector_type(), 'M')
        self.assertEqual(hedge('((very/M large/M) '
                               'shoes/Cn.p)').connector_type(), 'M')
        self.assertEqual(hedge('(will/M be/Pd.sc)').connector_type(), 'M')
        self.assertEqual(hedge('((will/M be/Pd.sc) john/Cp.s '
                               'rich/C)').connector_type(), 'Pd')
        self.assertEqual(hedge('(play/T piano/Cn.s)').connector_type(), 'T')

    def test_atom_with_type(self):
        self.assertEqual(hedge('(+/B a/Cn b/Cp)').atom_with_type('C'),
                         hedge('a/Cn'))
        self.assertEqual(hedge('(+/B a/C b/Cp)').atom_with_type('Cp'),
                         hedge('b/Cp'))
        self.assertEqual(hedge('(+/B a/C b/Cp)').atom_with_type('P'), None)
        self.assertEqual(hedge('a/Cn').atom_with_type('C'), hedge('a/Cn'))
        self.assertEqual(hedge('a/Cn').atom_with_type('Cn'), hedge('a/Cn'))
        self.assertEqual(hedge('a/Cn').atom_with_type('Cp'), None)
        self.assertEqual(hedge('a/Cn').atom_with_type('P'), None)

    def test_contains_atom_type(self):
        self.assertTrue(hedge('(+/B a/Cn b/Cp)').contains_atom_type('C'))
        self.assertTrue(hedge('(+/B a/C b/Cp)').contains_atom_type('Cp'))
        self.assertFalse(hedge('(+/B a/C b/Cp)').contains_atom_type('P'))
        self.assertTrue(hedge('a/Cn').contains_atom_type('C'))
        self.assertTrue(hedge('a/Cn').contains_atom_type('Cn'))
        self.assertFalse(hedge('a/Cn').contains_atom_type('Cp'))
        self.assertFalse(hedge('a/Cn').contains_atom_type('P'))

    def test_predicate(self):
        self.assertEqual(hedge('graphbrain/Cp.s/1').predicate(), None)
        self.assertEqual(hedge('graphbrain').predicate(), None)
        self.assertEqual(hedge('(is/Pd.so graphbrain/Cp.s '
                               'great/C)').predicate().to_str(), 'is/Pd.so')
        self.assertEqual(hedge('(red/M shoes/Cn.p)').predicate(), None)
        self.assertEqual(hedge('(before/Tt noon/C)').predicate(), None)
        self.assertEqual(hedge('(very/M large/M)').predicate(), None)
        self.assertEqual(hedge('((very/M large/M) '
                               'shoes/Cn.p)').predicate(), None)
        self.assertEqual(hedge('(will/M be/Pd.sc)').predicate().to_str(),
                         'be/Pd.sc')
        self.assertEqual(hedge('((will/M be/Pd.sc) john/Cp.s '
                               'rich/C)').predicate().to_str(), 'be/Pd.sc')
        self.assertEqual(hedge('(play/T piano/Cn.s)').predicate(), None)

    def test_rel_arg_role(self):
        edge = hedge('(is/Pd.so graphbrain/Cp.s great/C)')
        self.assertEqual(rel_arg_role(edge, 0), 's')
        self.assertEqual(rel_arg_role(edge, 1), 'o')
        self.assertEqual(rel_arg_role(edge, 2), None)
        edge = hedge('(is/B graphbrain/Cp.s great/C)')
        self.assertEqual(rel_arg_role(edge, 0), None)

    def test_is_pattern(self):
        edge = hedge("('s/Bp.am zimbabwe/M economy/Cn.s)")
        self.assertFalse(edge.is_pattern())
        edge = hedge("('s/Bp.am * economy/Cn.s)")
        self.assertTrue(edge.is_pattern())
        edge = hedge("('s/Bp.am * ...)")
        self.assertTrue(edge.is_pattern())
        edge = hedge('thing/C')
        self.assertFalse(edge.is_pattern())
        edge = hedge('&')
        self.assertTrue(edge.is_pattern())

    def test_is_full_pattern(self):
        edge = hedge("('s/Bp.am zimbabwe/M economy/Cn.s)")
        self.assertFalse(edge.is_full_pattern())
        edge = hedge("('s/Bp.am * economy/Cn.s)")
        self.assertFalse(edge.is_full_pattern())
        edge = hedge("('s/Bp.am * ...)")
        self.assertFalse(edge.is_full_pattern())
        edge = hedge('thing/C')
        self.assertFalse(edge.is_full_pattern())
        edge = hedge('&')
        self.assertTrue(edge.is_full_pattern())
        edge = hedge('(* * *')
        self.assertTrue(edge.is_full_pattern())
        edge = hedge('(* * * ...)')
        self.assertTrue(edge.is_full_pattern())
        edge = hedge('(@ * & ...)')
        self.assertTrue(edge.is_full_pattern())

    def test_argroles_atom(self):
        edge = hedge('s/Bp.am')
        self.assertEqual(edge.argroles(), 'am')
        edge = hedge('come/Pd.sx.-i----/en')
        self.assertEqual(edge.argroles(), 'sx')
        edge = hedge('come/Pd')
        self.assertEqual(edge.argroles(), '')
        edge = hedge('red/M')
        self.assertEqual(edge.argroles(), '')
        edge = hedge('berlin/Cp.s/de')
        self.assertEqual(edge.argroles(), '')

    def test_argroles_edge(self):
        edge = hedge('(is/Mv.|f--3s/en influenced/Pd.xpa.<pf---/en)')
        self.assertEqual(edge.argroles(), 'xpa')
        edge = hedge('(is/Mv.|f--3s/en influenced/Pd)')
        self.assertEqual(edge.argroles(), '')
        edge = hedge('(looks/Pd.sc.|f--3s she/Ci (very/M beautiful/Ca))')
        self.assertEqual(edge.argroles(), '')

    def test_replace_argroles_atom(self):
        edge = hedge('s/Bp.am')
        self.assertEqual(edge.replace_argroles('ma').to_str(), 's/Bp.ma')
        edge = hedge('come/Pd.sx.-i----/en')
        self.assertEqual(edge.replace_argroles('scx').to_str(),
                         'come/Pd.scx.-i----/en')
        edge = hedge('come/Pd/en')
        self.assertEqual(edge.replace_argroles('scx').to_str(),
                         'come/Pd.scx/en')
        edge = hedge('xxx')
        self.assertEqual(edge.replace_argroles('scx').to_str(),
                         'xxx')

    def test_insert_argrole_atom(self):
        edge = hedge('s/Bp.am')
        self.assertEqual(edge.insert_argrole('m', 0).to_str(), 's/Bp.mam')
        edge = hedge('s/Bp.am')
        self.assertEqual(edge.insert_argrole('m', 1).to_str(), 's/Bp.amm')
        edge = hedge('s/Bp.am')
        self.assertEqual(edge.insert_argrole('m', 2).to_str(), 's/Bp.amm')
        edge = hedge('s/Bp.am')
        self.assertEqual(edge.insert_argrole('m', 3).to_str(), 's/Bp.amm')
        edge = hedge('come/Pd.sx.-i----/en')
        self.assertEqual(edge.insert_argrole('x', 0).to_str(),
                         'come/Pd.xsx.-i----/en')
        edge = hedge('come/Pd.sx.-i----/en')
        self.assertEqual(edge.insert_argrole('x', 1).to_str(),
                         'come/Pd.sxx.-i----/en')
        edge = hedge('come/Pd.sx.-i----/en')
        self.assertEqual(edge.insert_argrole('x', 2).to_str(),
                         'come/Pd.sxx.-i----/en')
        edge = hedge('come/Pd.sx.-i----/en')
        self.assertEqual(edge.insert_argrole('x', 100).to_str(),
                         'come/Pd.sxx.-i----/en')
        edge = hedge('come/Pd/en')
        self.assertEqual(edge.insert_argrole('s', 0).to_str(),
                         'come/Pd.s/en')
        edge = hedge('come/Pd/en')
        self.assertEqual(edge.insert_argrole('s', 1).to_str(),
                         'come/Pd.s/en')
        edge = hedge('come/Pd/en')
        self.assertEqual(edge.insert_argrole('s', 100).to_str(),
                         'come/Pd.s/en')
        edge = hedge('xxx')
        self.assertEqual(edge.insert_argrole('s', 0).to_str(),
                         'xxx')
        edge = hedge('xxx')
        self.assertEqual(edge.insert_argrole('s', 1).to_str(),
                         'xxx')
        edge = hedge('xxx')
        self.assertEqual(edge.insert_argrole('s', 100).to_str(),
                         'xxx')

    def test_replace_argroles_edge(self):
        edge = hedge('(s/Bp.am x/C y/C)')
        self.assertEqual(edge.replace_argroles('ma').to_str(),
                         '(s/Bp.ma x/C y/C)')
        edge = hedge('(come/Pd.sx.-i----/en you/C here/C)')
        self.assertEqual(edge.replace_argroles('scx').to_str(),
                         '(come/Pd.scx.-i----/en you/C here/C)')
        edge = hedge('(come/Pd/en you/C here/C)')
        self.assertEqual(edge.replace_argroles('scx').to_str(),
                         '(come/Pd.scx/en you/C here/C)')
        edge = hedge('(come you/C here/C)')
        self.assertEqual(edge.replace_argroles('scx').to_str(),
                         '(come you/C here/C)')

    def test_insert_argrole_edge(self):
        edge = hedge('(s/Bp.am x/C y/C)')
        self.assertEqual(
            edge.insert_argrole('m', 0).to_str(), '(s/Bp.mam x/C y/C)')
        edge = hedge('(s/Bp.am x/C y/C)')
        self.assertEqual(
            edge.insert_argrole('m', 1).to_str(), '(s/Bp.amm x/C y/C)')
        edge = hedge('(s/Bp.am x/C y/C)')
        self.assertEqual(
            edge.insert_argrole('m', 2).to_str(), '(s/Bp.amm x/C y/C)')
        edge = hedge('(s/Bp.am x/C y/C)')
        self.assertEqual(
            edge.insert_argrole('m', 3).to_str(), '(s/Bp.amm x/C y/C)')
        edge = hedge('(come/Pd.sx.-i----/en you/C here/C)')
        self.assertEqual(edge.insert_argrole('x', 0).to_str(),
                         '(come/Pd.xsx.-i----/en you/C here/C)')
        edge = hedge('(come/Pd.sx.-i----/en you/C here/C)')
        self.assertEqual(edge.insert_argrole('x', 1).to_str(),
                         '(come/Pd.sxx.-i----/en you/C here/C)')
        edge = hedge('(come/Pd.sx.-i----/en you/C here/C)')
        self.assertEqual(edge.insert_argrole('x', 2).to_str(),
                         '(come/Pd.sxx.-i----/en you/C here/C)')
        edge = hedge('(come/Pd.sx.-i----/en you/C here/C)')
        self.assertEqual(edge.insert_argrole('x', 100).to_str(),
                         '(come/Pd.sxx.-i----/en you/C here/C)')
        edge = hedge('(come/Pd/en you/C here/C)')
        self.assertEqual(edge.insert_argrole('s', 0).to_str(),
                         '(come/Pd.s/en you/C here/C)')
        edge = hedge('(come/Pd/en you/C here/C)')
        self.assertEqual(edge.insert_argrole('s', 1).to_str(),
                         '(come/Pd.s/en you/C here/C)')
        edge = hedge('(come/Pd/en you/C here/C)')
        self.assertEqual(edge.insert_argrole('s', 100).to_str(),
                         '(come/Pd.s/en you/C here/C)')
        edge = hedge('(come you/C here/C)')
        self.assertEqual(edge.insert_argrole('s', 0).to_str(),
                         '(come you/C here/C)')
        edge = hedge('(come you/C here/C)')
        self.assertEqual(edge.insert_argrole('s', 1).to_str(),
                         '(come you/C here/C)')
        edge = hedge('(come you/C here/C)')
        self.assertEqual(edge.insert_argrole('s', 100).to_str(),
                         '(come you/C here/C)')

    def test_insert_edge_with_argrole(self):
        edge = hedge('(is/Pd.sc/en sky/C blue/C)')
        self.assertEqual(
            edge.insert_edge_with_argrole(hedge('today/C'), 'x', 0),
            hedge('(is/Pd.xsc/en today/C sky/C blue/C)'))
        self.assertEqual(
            edge.insert_edge_with_argrole(hedge('today/C'), 'x', 1),
            hedge('(is/Pd.sxc/en sky/C today/C blue/C)'))
        self.assertEqual(
            edge.insert_edge_with_argrole(hedge('today/C'), 'x', 2),
            hedge('(is/Pd.scx/en sky/C blue/C today/C)'))
        self.assertEqual(
            edge.insert_edge_with_argrole(hedge('today/C'), 'x', 100),
            hedge('(is/Pd.scx/en sky/C blue/C today/C)'))

    def test_edges_with_argrole(self):
        edge_str = ("((have/Mv.|f----/en (been/Mv.<pf---/en "
                    "tracking/Pd.sox.|pg---/en)) (from/Br.ma/en "
                    "satellites/Cc.p/en (and/B+/en nasa/Cp.s/en "
                    "(other/Ma/en agencies/Cc.p/en))) "
                    "(+/B.aam/. sea/Cc.s/en ice/Cc.s/en changes/Cc.p/en) "
                    "(since/Tt/en 1979/C#/en))")
        edge = hedge(edge_str)

        subj = hedge(("(from/Br.ma/en satellites/Cc.p/en "
                      "(and/B+/en nasa/Cp.s/en (other/Ma/en "
                      "agencies/Cc.p/en)))"))
        obj = hedge("(+/B.aam/. sea/Cc.s/en ice/Cc.s/en changes/Cc.p/en)")
        spec = hedge("(since/Tt/en 1979/C#/en)")

        self.assertEqual(edge.edges_with_argrole('s'), [subj])
        self.assertEqual(edge.edges_with_argrole('o'), [obj])
        self.assertEqual(edge.edges_with_argrole('x'), [spec])
        self.assertEqual(edge.edges_with_argrole('p'), [])

    def test_edges_with_argrole_no_roles(self):
        edge_str = ("((have/Mv.|f----/en (been/Mv.<pf---/en "
                    "tracking/Pd)) (from/Br.ma/en "
                    "satellites/Cc.p/en (and/B+/en nasa/Cp.s/en "
                    "(other/Ma/en agencies/Cc.p/en))) "
                    "(+/B.aam/. sea/Cc.s/en ice/Cc.s/en changes/Cc.p/en) "
                    "(since/Tt/en 1979/C#/en))")
        edge = hedge(edge_str)

        self.assertEqual(edge.edges_with_argrole('s'), [])
        self.assertEqual(edge.edges_with_argrole('o'), [])
        self.assertEqual(edge.edges_with_argrole('x'), [])
        self.assertEqual(edge.edges_with_argrole('p'), [])

    def test_edges_with_argrole_atom(self):
        edge = hedge('tracking/Pd.sox.|pg---/en')

        self.assertEqual(edge.edges_with_argrole('s'), [])
        self.assertEqual(edge.edges_with_argrole('o'), [])
        self.assertEqual(edge.edges_with_argrole('x'), [])
        self.assertEqual(edge.edges_with_argrole('p'), [])

    def test_main_concepts(self):
        concept = hedge("('s/Bp.am zimbabwe/Mp economy/Cn.s)")
        self.assertEqual(concept.main_concepts(), [hedge('economy/Cn.s')])
        concept = hedge("('s/Bp zimbabwe/Mp economy/Cn.s)")
        self.assertEqual(concept.main_concepts(), [])
        concept = hedge('(+/B.am?/. hiv/Ca kit/Cn.s (testing/M self/Cn.s))')
        self.assertEqual(concept.main_concepts(), [hedge('kit/Cn.s')])
        concept = hedge('(+/B.?a?/. hiv/Ca kit/Cn.s (testing/M self/Cn.s))')
        self.assertEqual(concept.main_concepts(), [])
        concept = hedge('(a/M thing/C)')
        self.assertEqual(concept.main_concepts(), [])
        concept = hedge('thing/C')
        self.assertEqual(concept.main_concepts(), [])

    def test_main_apply_vars(self):
        edge = hedge('(PRED zimbabwe/C PROP)')
        nedge = edge.apply_vars({'PRED': hedge('is/P'),
                                 'PROP': hedge('(sehr/M schön/C)')})
        self.assertEqual(nedge, hedge('(is/P zimbabwe/C (sehr/M schön/C))'))


if __name__ == '__main__':
    unittest.main()
