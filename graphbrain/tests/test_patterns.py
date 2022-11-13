import unittest

import graphbrain.constants as const
from graphbrain import hedge, hgraph
from graphbrain.patterns import (match_pattern, edge_matches_pattern, is_wildcard, is_pattern, is_full_pattern,
                                 apply_vars, PatternCounter, is_unordered_pattern)


class TestPatterns(unittest.TestCase):
    def setUp(self):
        self.hg = hgraph('test.db')

    def tearDown(self):
        self.hg.close()

    def test_close(self):
        self.hg.close()

    def test_is_wildcard1(self):
        self.assertFalse(is_wildcard(hedge('thing/C')))

    def test_is_wildcard2(self):
        self.assertTrue(is_wildcard(hedge('*/C')))

    def test_is_wildcard3(self):
        self.assertTrue(is_wildcard(hedge('./M')))

    def test_is_wildcard4(self):
        self.assertTrue(is_wildcard(hedge('...')))

    def test_is_wildcard5(self):
        self.assertTrue(is_wildcard(hedge('VARIABLE/C')))

    def test_is_wildcard6(self):
        self.assertTrue(is_wildcard(hedge('*VARIABLE/C')))

    def test_is_wildcard7(self):
        self.assertFalse(is_wildcard(hedge('go/Pd.so')))

    def test_is_wildcard8(self):
        self.assertFalse(is_wildcard(hedge('go/Pd.{so}')))

    def test_is_wildcard9(self):
        self.assertFalse(is_wildcard(hedge('(is/P.sc */M */Cn.s)')))

    def test_is_pattern1(self):
        self.assertFalse(is_pattern(hedge("('s/Bp.am zimbabwe/M economy/Cn.s)")))

    def test_is_pattern2(self):
        self.assertTrue(is_pattern(hedge("('s/Bp.am * economy/Cn.s)")))

    def test_is_pattern3(self):
        self.assertTrue(is_pattern(hedge("('s/Bp.am * ...)")))

    def test_is_pattern4(self):
        self.assertFalse(is_pattern(hedge('thing/C')))

    def test_is_pattern5(self):
        self.assertTrue(is_pattern(hedge('(*)')))

    def test_is_pattern6(self):
        self.assertFalse(is_pattern(hedge('go/Pd.so')))

    def test_is_pattern7(self):
        self.assertTrue(is_pattern(hedge('go/Pd.{so}')))

    def test_is_pattern8(self):
        self.assertFalse(is_pattern(hedge('(is/P.sc x/C y/Cn.s)')))

    def test_is_pattern9(self):
        self.assertTrue(is_pattern(hedge('(is/P.{sc} x/C y/Cn.s)')))

    def test_is_full_pattern1(self):
        self.assertFalse(is_full_pattern(hedge("('s/Bp.am zimbabwe/M economy/Cn.s)")))

    def test_is_full_pattern2(self):
        self.assertFalse(is_full_pattern(hedge("('s/Bp.am * economy/Cn.s)")))

    def test_is_full_pattern3(self):
        self.assertFalse(is_full_pattern(hedge("('s/Bp.am * ...)")))

    def test_is_full_pattern4(self):
        self.assertFalse(is_full_pattern(hedge('thing/C')))

    def test_is_full_pattern5(self):
        self.assertTrue(is_full_pattern(hedge('(*)')))

    def test_is_full_pattern6(self):
        self.assertTrue(is_full_pattern(hedge('(* * *')))

    def test_is_full_pattern7(self):
        self.assertTrue(is_full_pattern(hedge('(* * * ...)')))

    def test_is_full_pattern8(self):
        self.assertTrue(is_full_pattern(hedge('(. * (*) ...)')))

    def test_is_unordered_pattern1(self):
        self.assertFalse(is_unordered_pattern(hedge("('s/Bp.am * ...)")))

    def test_is_unordered_pattern2(self):
        self.assertFalse(is_unordered_pattern(hedge('thing/C')))

    def test_is_unordered_pattern3(self):
        self.assertFalse(is_unordered_pattern(hedge('*')))

    def test_is_unordered_pattern4(self):
        self.assertFalse(is_unordered_pattern(hedge('go/Pd.so')))

    def test_is_unordered_pattern5(self):
        self.assertTrue(is_unordered_pattern(hedge('go/Pd.{so}')))

    def test_is_unordered_pattern6(self):
        self.assertFalse(is_unordered_pattern(hedge('(is/P.sc x/C y/Cn.s)')))

    def test_is_unordered_pattern7(self):
        self.assertTrue(is_unordered_pattern(hedge('(is/P.{sc} x/C y/Cn.s)')))

    def test_is_unordered_pattern8(self):
        self.assertTrue(is_unordered_pattern(hedge('((not/M is/P.{sc}) x/C y/Cn.s)')))

    def test_main_apply_vars(self):
        edge = hedge('(PRED zimbabwe/C PROP)')
        nedge = apply_vars(edge, {'PRED': hedge('is/P'), 'PROP': hedge('(sehr/M schön/C)')})
        self.assertEqual(nedge, hedge('(is/P zimbabwe/C (sehr/M schön/C))'))

    def test_match_pattern_simple1(self):
        self.assertEqual(match_pattern('(a b)', '(a b)'), [{}])

    def test_match_pattern_simple2(self):
        self.assertEqual(match_pattern('(a b)', '(a a)'), [])

    def test_match_pattern_wildcard1(self):
        self.assertEqual(match_pattern('(is/Pd graphbrain/Cp.s great/C)', '(is/Pd graphbrain/Cp.s *X)'),
                         [{'X': hedge('great/C')}])

    def test_match_pattern_wildcard2(self):
        self.assertEqual(match_pattern('(is/Pd graphbrain/Cp.s great/C)', '(is/Pd graphbrain/Cp.s *)'), [{}])

    def test_match_pattern_wildcard3(self):
        self.assertEqual(match_pattern('(was/Pd graphbrain /Cp.s great/C)', '(is/Pd graphbrain/Cp.s *X)'), [])

    def test_match_pattern_atomic_wildcard1(self):
        self.assertEqual(match_pattern('(is/Pd graphbrain/Cp.s great/C)', '(is/Pd graphbrain/Cp.s .PROP)'),
                         [{'PROP': hedge('great/C')}])

    def test_match_pattern_atomic_wildcard2(self):
        self.assertEqual(match_pattern('(is/Pd graphbrain/Cp.s great/C)', '(is/Pd graphbrain/Cp.s .)'), [{}])

    def test_match_pattern_atomic_wildcard3(self):
        self.assertEqual(match_pattern('(was/Pd graphbrain/Cp.s great/C)', '(is/Pd graphbrain/Cp.s .PROP)'), [])

    def test_match_pattern_atomic_wildcard4(self):
        self.assertEqual(match_pattern('(is/Pd graphbrain/Cp.s (fairly/M great/C))', '(is/Pd graphbrain/Cp.s .PROP)'),
                         [])

    def test_match_pattern_non_atomic_wildcard1(self):
        self.assertEqual(match_pattern('(is/Pd graphbrain/Cp.s (fairly/M great/C))', '(is/Pd graphbrain/Cp.s (PROP))'),
                         [{'PROP': hedge('(fairly/M great/C)')}])

    def test_match_pattern_non_atomic_wildcard2(self):
        self.assertEqual(match_pattern('(is/Pd graphbrain/Cp.s (fairly/M great/C))', '(is/Pd graphbrain/Cp.s (*))'),
                         [{}])

    def test_match_pattern_non_atomic_wildcard3(self):
        self.assertEqual(match_pattern('(was/Pd graphbrain/Cp.s (fairly/M great/C))', '(is/Pd graphbrain/Cp.s (PROP))'),
                         [])

    def test_match_pattern_non_atomic_wildcard4(self):
        self.assertEqual(match_pattern('(is/Pd graphbrain/Cp.s great/C)', '(is/Pd graphbrain/Cp.s (PROP))'), [])

    def test_match_pattern_open_ended1(self):
        self.assertEqual(match_pattern('(is/Pd graphbrain/Cp.s great/C)', '(is/Pd graphbrain/Cp.s *X ...)'),
                         [{'X': hedge('great/C')}])

    def test_match_pattern_open_ended2(self):
        self.assertEqual(match_pattern('(is/Pd graphbrain/Cp.s great/C)', '(is/Pd graphbrain/Cp.s * ...)'), [{}])

    def test_match_pattern_open_ended3(self):
        self.assertEqual(match_pattern('(was/Pd graphbrain /Cp.s great/C)', '(is/Pd graphbrain/Cp.s *X ...)'), [])

    def test_match_pattern_open_ended4(self):
        self.assertEqual(match_pattern('(is/Pd graphbrain/Cp.s great/C)', '(is/Pd .OBJ ...)'),
                         [{'OBJ': hedge('graphbrain/Cp.s')}])

    def test_match_pattern_open_ended5(self):
        self.assertEqual(match_pattern('(is/Pd graphbrain/Cp.s great/C)', '(is/Pd .OBJ)'), [])

    def test_match_pattern_argroles1(self):
        self.assertEqual(match_pattern('(is/Pd.sc graphbrain/Cp.s great/C)', '(is/Pd.sc graphbrain/Cp.s *X)'),
                         [{'X': hedge('great/C')}])

    def test_match_pattern_argroles2(self):
        self.assertEqual(match_pattern('(is/Pd.cs great/C graphbrain/Cp.s)', '(is/Pd.{sc} graphbrain/Cp.s *X)'),
                         [{'X': hedge('great/C')}])

    def test_match_pattern_argroles3(self):
        self.assertEqual(match_pattern('(is/Pd.sc graphbrain/Cp.s great/C)', '(is/Pd.{scx} graphbrain/Cp.s *X *Y)'), [])

    def test_match_pattern_argroles4(self):
        self.assertEqual(
            match_pattern('(is/Pd.xcs today/C great/C graphbrain/Cp.s)', '(is/Pd.{sc} graphbrain/Cp.s *X ...)'),
            [{'X': hedge('great/C')}])

    def test_match_pattern_argroles5(self):
        self.assertEqual(
            match_pattern('(is/Pd.xcs today/C great/C graphbrain/Cp.s)', '(is/Pd.{sc} graphbrain/Cp.s *X)'),
            [{'X': hedge('great/C')}])

    def test_match_pattern_argroles6(self):
        self.assertEqual(match_pattern('(is/Pd.sc graphbrain/Cp.s great/C)', '(is/Pd.{sc} graphbrain/Cp.s *)'), [{}])

    def test_match_pattern_argroles7(self):
        self.assertEqual(match_pattern('(was/Pd.sc graphbrain /Cp.s great/C)', '(is/Pd.{sc} graphbrain/Cp.s *X)'), [])

    def test_match_pattern_argroles8(self):
        self.assertEqual(match_pattern('(is/Pd.sc graphbrain/Cp.s great/C)', '(+/Pd.{sc} graphbrain/Cp.s *X)'), [])

    def test_match_pattern_argroles_ordered1(self):
        self.assertEqual(match_pattern('(is/Pd.sc graphbrain/Cp.s great/C)', '(is/Pd.sc graphbrain/Cp.s *X)'),
                         [{'X': hedge('great/C')}])

    def test_match_pattern_argroles_ordered2(self):
        self.assertEqual(match_pattern('(is/Pd.cs great/C graphbrain/Cp.s)', '(is/Pd.sc graphbrain/Cp.s *X)'), [])

    def test_match_pattern_argroles_ordered3(self):
        self.assertEqual(match_pattern('(is/Pd.sc graphbrain/Cp.s great/C)', '(is/Pd.scx graphbrain/Cp.s *X *Y)'), [])

    def test_match_pattern_argroles_ordered4(self):
        self.assertEqual(
            match_pattern('(is/Pd.xcs today/C great/C graphbrain/Cp.s)', '(is/Pd.sc graphbrain/Cp.s *X ...)'), [])

    def test_match_pattern_argroles_ordered5(self):
        self.assertEqual(match_pattern('(is/Pd.xcs today/C great/C graphbrain/Cp.s)', '(is/Pd.sc graphbrain/Cp.s *X)'),
                         [])

    def test_match_pattern_argroles_ordered6(self):
        self.assertEqual(match_pattern('(is/Pd.sc graphbrain/Cp.s great/C)', '(is/Pd.sc graphbrain/Cp.s *)'), [{}])

    def test_match_pattern_argroles_ordered7(self):
        self.assertEqual(match_pattern('(was/Pd.sc graphbrain /Cp.s great/C)', '(is/Pd.sc graphbrain/Cp.s *X)'), [])

    def test_match_pattern_argroles_ordered8(self):
        self.assertEqual(match_pattern('(is/Pd.sc graphbrain/Cp.s great/C)', '(+/Pd.sc graphbrain/Cp.s *X)'), [])

    def test_match_pattern_argroles_vars(self):
        self.assertEqual(match_pattern('(is/Pd.sc graphbrain/Cp.s great/C)', '(is/Pd.{sc} graphbrain/Cp.s PROP)'),
                         [{'PROP': hedge('great/C')}])

    def test_match_pattern_argroles_vars1(self):
        self.assertEqual(match_pattern('(is/Pd.cs great/C graphbrain/Cp.s)', '(is/Pd.{sc} graphbrain/Cp.s X)'),
                         [{'X': hedge('great/C')}])

    def test_match_pattern_argroles_vars2(self):
        self.assertEqual(match_pattern('(is/Pd.sc graphbrain/Cp.s great/C)', '(is/Pd.{scx} graphbrain/Cp.s X Y)'), [])

    def test_match_pattern_argroles_vars3(self):
        self.assertEqual(
            match_pattern('(is/Pd.xcs today/C great/C graphbrain/Cp.s)', '(is/Pd.{sc} graphbrain/Cp.s X ...)'),
            [{'X': hedge('great/C')}])

    def test_match_pattern_argroles_vars4(self):
        self.assertEqual(match_pattern('(is/Pd.xcs today/C great/C graphbrain/Cp.s)', '(is/Pd.{sc} graphbrain/Cp.s X)'),
            [{'X': hedge('great/C')}])

    def test_match_pattern_argroles_vars5(self):
        self.assertEqual(match_pattern('(is/Pd.sc graphbrain/Cp.s great/C)', '(is/Pd.{sc} graphbrain/Cp.s XYZ)'),
                         [{'XYZ': hedge('great/C')}])

    def test_match_pattern_argroles_vars6(self):
        self.assertEqual(match_pattern('(was/Pd.sc graphbrain /Cp.s great/C)', '(is/Pd.{sc} graphbrain/Cp.s X)'), [])

    def test_match_pattern_argroles_unknown(self):
        self.assertEqual(
            match_pattern('(is/Pd.scx graphbrain/Cp.s great/C today/C)', '(is/Pd.{sc} graphbrain/Cp.s PROP ...)'),
            [{'PROP': hedge('great/C')}])

    def test_match_pattern_argroles_unknown1(self):
        self.assertEqual(
            match_pattern('(is/Pd.scx graphbrain/Cp.s great/C today/C)', '(is/Pd.{sc} graphbrain/Cp.s PROP EXTRA)'),
            [{'PROP': hedge('great/C'), 'EXTRA': hedge('today/C')}])

    def test_match_pattern_argroles_unknown2(self):
        self.assertEqual(match_pattern('(is/Pd.scx graphbrain/Cp.s great/C today/C (after/J x/C))',
                                       '(is/Pd.{sc} graphbrain/Cp.s PROP EXTRA)'),
                         [{'PROP': hedge('great/C'), 'EXTRA': hedge('today/C')},
                          {'PROP': hedge('great/C'), 'EXTRA': hedge('(after/J x/C)')}])

    def test_match_pattern_argroles_unknown3(self):
        self.assertEqual(match_pattern('(is/Pd.scx graphbrain/Cp.s great/C today/C (after/J x/C))',
                                       '(is/Pd.{sc} graphbrain/Cp.s PROP EXTRA ...)'),
                         [{'PROP': hedge('great/C'), 'EXTRA': hedge('today/C')},
                          {'PROP': hedge('great/C'), 'EXTRA': hedge('(after/J x/C)')}])

    def test_match_pattern_argroles_unknown4(self):
        self.assertEqual(match_pattern('(is/Pd.scx graphbrain/Cp.s great/C today/C (after/J x/C))',
                                       '(is/Pd.{sc} graphbrain/Cp.s PROP (EXTRA1) EXTRA2)'),
                         [{'PROP': hedge('great/C'), 'EXTRA1': hedge('(after/J x/C)'), 'EXTRA2': hedge('today/C')}])

    def test_match_pattern_argroles_unknown5(self):
        self.assertEqual(match_pattern('(is/Pd.scx graphbrain/Cp.s great/C (after/J x/C) today/C)',
                                       '(is/Pd.{sc} graphbrain/Cp.s PROP (EXTRA1) EXTRA2)'),
                         [{'PROP': hedge('great/C'), 'EXTRA1': hedge('(after/J x/C)'), 'EXTRA2': hedge('today/C')}])

    def test_match_pattern_repeated_vars1(self):
        self.assertEqual(match_pattern('(is/P graphbrain/Cp.s great/C today/C (after/J x/C))',
                                       '(is/P graphbrain/Cp.s PROP EXTRA EXTRA)'),
                         [])

    def test_match_pattern_repeated_vars2(self):
        self.assertEqual(match_pattern('(is/P graphbrain/Cp.s great/C today/C today/C)',
                                       '(is/P graphbrain/Cp.s PROP EXTRA EXTRA)'),
                         [{'PROP': hedge('great/C'), 'EXTRA': hedge('today/C')}])

    def test_match_pattern_argroles_repeated_vars1(self):
        self.assertEqual(match_pattern('(is/Pd.scx graphbrain/Cp.s great/C today/C (after/J x/C))',
                                       '(is/Pd.{sc} graphbrain/Cp.s PROP EXTRA EXTRA)'),
                         [])

    def test_match_pattern_argroles_repeated_vars2(self):
        self.assertEqual(match_pattern('(is/Pd.scx graphbrain/Cp.s great/C today/C today/C)',
                                       '(is/Pd.{sc} graphbrain/Cp.s PROP EXTRA EXTRA)'),
                         [{'PROP': hedge('great/C'), 'EXTRA': hedge('today/C')}])

    def test_match_pattern_repeated_vars_external1(self):
        self.assertEqual(match_pattern('(is/P graphbrain/Cp.s great/C today/C)', '(is/P graphbrain/Cp.s PROP EXTRA)',
                                       curvars={'PROP': hedge('great/C')}),
                         [{'PROP': hedge('great/C'), 'EXTRA': hedge('today/C')}])

    def test_match_pattern_repeated_vars_external2(self):
        self.assertEqual(match_pattern('(is/P graphbrain/Cp.s great/C today/C)', '(is/P graphbrain/Cp.s PROP EXTRA)',
                                       curvars={'PROP': hedge('error/C')}),
                         [])

    def test_match_pattern_argroles_repeated_vars_external1(self):
        self.assertEqual(match_pattern('(is/Pd.scx graphbrain/Cp.s great/C today/C)',
                                       '(is/Pd.{sc} graphbrain/Cp.s PROP EXTRA)',
                                       curvars={'PROP': hedge('great/C')}),
                         [{'PROP': hedge('great/C'), 'EXTRA': hedge('today/C')}])

    def test_match_pattern_argroles_repeated_vars_external2(self):
        self.assertEqual(match_pattern('(is/Pd.scx graphbrain/Cp.s great/C today/C)',
                                       '(is/P.{sc} graphbrain/Cp.s PROP EXTRA)',
                                       curvars={'PROP': hedge('error/C')}),
                         [])

    def test_match_pattern_deep1(self):
        self.assertEqual(match_pattern('(is/P graphbrain/Cp.s great/C today/C (after/J x/C))',
                                       '(is/P graphbrain/Cp.s PROP EXTRA (after/J X))'),
            [{'PROP': hedge('great/C'), 'EXTRA': hedge('today/C'), 'X': hedge('x/C')}])

    def test_match_pattern_deep2(self):
        self.assertEqual(match_pattern('(is/P graphbrain/Cp.s great/C today/C (after/J x/C))',
                                       '(is/P graphbrain/Cp.s PROP EXTRA (before/J X))'),
                         [])

    def test_match_pattern_deep3(self):
        self.assertEqual(match_pattern('(is/P graphbrain/Cp.s great/C today/C (after/J x/C))',
                                       '(is/P graphbrain/Cp.s PROP EXTRA (after/J EXTRA))'),
                         [])

    def test_match_pattern_deep4(self):
        self.assertEqual(match_pattern('(is/P graphbrain/Cp.s great/C x/C (after/J x/C))',
                                       '(is/P graphbrain/Cp.s PROP X (after/J X))'),
                         [{'PROP': hedge('great/C'), 'X': hedge('x/C')}])

    def test_match_pattern_deep5(self):
        self.assertEqual(match_pattern('(is/P graphbrain/Cp.s great/C x/C (after/J x/C))',
                                       '(is/P graphbrain/Cp.s PROP X (after/J X))',
                                       curvars={'X': hedge('x/C')}),
                         [{'PROP': hedge('great/C'), 'X': hedge('x/C')}])

    def test_match_pattern_deep6(self):
        self.assertEqual(match_pattern('(is/P graphbrain/Cp.s great/C x/C (after/J x/C))',
                                       '(is/P graphbrain/Cp.s PROP X (after/J X))',
                                       curvars={'X': hedge('y/C')}),
                         [])

    def test_match_pattern_argroles_deep1(self):
        self.assertEqual(match_pattern('(is/Pd.scx graphbrain/Cp.s great/C today/C (after/J x/C))',
                                       '(is/Pd.{sc} graphbrain/Cp.s PROP EXTRA (after/J X))'),
                         [{'PROP': hedge('great/C'), 'EXTRA': hedge('today/C'), 'X': hedge('x/C')}])

    def test_match_pattern_argroles_deep2(self):
        self.assertEqual(match_pattern('(is/Pd.scx graphbrain/Cp.s great/C today/C (after/J x/C))',
                                       '(is/Pd.{sc} graphbrain/Cp.s PROP EXTRA (before/J X))'),
                         [])

    def test_match_pattern_argroles_deep3(self):
        self.assertEqual(match_pattern('(is/Pd.scx graphbrain/Cp.s great/C today/C (after/J x/C))',
                                       '(is/Pd.{sc} graphbrain/Cp.s PROP EXTRA (after/J EXTRA))'),
                         [])

    def test_match_pattern_argroles_deep4(self):
        self.assertEqual(match_pattern('(is/Pd.scx graphbrain/Cp.s great/C x/C (after/J x/C))',
                                       '(is/Pd.{sc} graphbrain/Cp.s PROP X (after/J X))'),
                         [{'PROP': hedge('great/C'), 'X': hedge('x/C')}])

    def test_match_pattern_argroles_deep5(self):
        self.assertEqual(match_pattern('(is/Pd.scx graphbrain/Cp.s great/C x/C (after/J x/C))',
                                       '(is/Pd.{sc} graphbrain/Cp.s PROP X (after/J X))',
                                       curvars={'X': hedge('x/C')}),
                         [{'PROP': hedge('great/C'), 'X': hedge('x/C')}])

    def test_match_pattern_argroles_deep6(self):
        self.assertEqual(match_pattern('(is/Pd.scx graphbrain/Cp.s great/C x/C (after/J x/C))',
                                       '(is/Pd.{sc} graphbrain/Cp.s PROP X (after/J X))',
                                       curvars={'X': hedge('y/C')}),
                         [])

    def test_match_pattern_argroles_multiple_results1(self):
        self.assertEqual(match_pattern('(is/Pd.scx graphbrain/Cp.s great/C today/C (after/J x/C))',
                                       '(is/Pd.{sc} graphbrain/Cp.s PROP EXTRA ...)'),
                         [{'PROP': hedge('great/C'), 'EXTRA': hedge('today/C')},
                          {'PROP': hedge('great/C'), 'EXTRA': hedge('(after/J x/C)')}])

    def test_match_pattern_argroles_multiple_results2(self):
        self.assertEqual(match_pattern('(is/Pd.sscxx i/C graphbrain/Cp.s great/C today/C x/C)',
                                       '(is/Pd.{sc} graphbrain/Cp.s PROP EXTRA ...)'),
                         [{'PROP': hedge('great/C'), 'EXTRA': hedge('i/C')},
                          {'PROP': hedge('great/C'), 'EXTRA': hedge('today/C')},
                          {'PROP': hedge('great/C'), 'EXTRA': hedge('x/C')}])

    def test_match_pattern_argroles_exclusions1(self):
        self.assertEqual(match_pattern('(is/Pd.sc graphbrain/Cp.s great/C)', '(is/Pd.sc-x graphbrain/Cp.s X ...)'),
                         [{'X': hedge('great/C')}])

    def test_match_pattern_argroles_exclusions2(self):
        self.assertEqual(match_pattern('(is/Pd.scx graphbrain/Cp.s great/C today/J)',
                                       '(is/Pd.sc-x graphbrain/Cp.s X ...)'),
                         [])

    def test_match_pattern_argroles_exclusions3(self):
        self.assertEqual(match_pattern('(is/Pd.sc graphbrain/Cp.s great/C)', '(is/Pd.sc-s graphbrain/Cp.s X ...)'),
                         [{'X': hedge('great/C')}])

    def test_match_pattern_argroles_exclusions4(self):
        self.assertEqual(match_pattern('(is/Pd.sc i/Cp.s graphbrain/Cp.s great/C)',
                                       '(is/Pd.sc-s graphbrain/Cp.s X ...)'),
                         [])

    def test_match_pattern_argroles_optionals1(self):
        self.assertEqual(match_pattern('(is/Pd.sc graphbrain/Cp.s great/C)', '(is/Pd.{sc,x} X Y Z)'),
                         [{'X': hedge('graphbrain/Cp.s'), 'Y': hedge('great/C')}])

    def test_match_pattern_argroles_optionals2(self):
        self.assertEqual(match_pattern('(is/Pd.scx graphbrain/Cp.s great/C today/J)', '(is/Pd.{sc,x} X Y Z)'),
                         [{'X': hedge('graphbrain/Cp.s'), 'Y': hedge('great/C'), 'Z': hedge('today/J')}])

    def test_match_pattern_match_connectors1(self):
        self.assertEqual(match_pattern('(is/P graphbrain/Cp.s great/C)', '(PRED/P graphbrain/Cp.s X ...)'),
                         [{'PRED': hedge('is/P'), 'X': hedge('great/C')}])

    def test_match_pattern_match_connectors2(self):
        self.assertEqual(match_pattern('(is/P graphbrain/Cp.s great/C)', '(X/P graphbrain/Cp.s X ...)'), [])

    def test_match_pattern_argroles_match_connectors1(self):
        self.assertEqual(match_pattern('(is/Pd.sc graphbrain/Cp.s great/C)', '(PRED/Pd.sc graphbrain/Cp.s X ...)'),
                         [{'PRED': hedge('is/Pd.sc'), 'X': hedge('great/C')}])

    def test_match_pattern_argroles_match_connectors2(self):
        self.assertEqual(match_pattern('(is/Pd.sc graphbrain/Cp.s great/C)', '(X/Pd.sc graphbrain/Cp.s X ...)'), [])

    def test_match_pattern_predicate_singleton(self):
        self.assertEqual(match_pattern('keep/Pd..-i-----/en', 'keep/Pd..-i-----'), [{}])

    def test_match_pattern_debug_case_1(self):
        self.assertEqual(
            match_pattern('(said/Pd.sr.<f-----/en entner/Cp.s/en (did/P.so.<f-----/en (of/Br.ma/en all/Cd/en ('
                          'the/Md/en people/Cc.p/en)) (the/Md/en (right/Ma/en thing/Cc.s/en))))',
                          '(said/Pd.{sr}.<f----- */C *)'),
            [{}])

    def test_match_pattern_debug_case_2(self):
        self.assertEqual(
            match_pattern('(said/Pd.sxr.<f-----/en (+/B.ma/. providers/Cc.p/en service/Cc.s/en) (on/Tt/en (+/B.ma/. ('
                          'with/Br.ma/en (a/Md/en call/Cc.s/en) (+/B.ma/. (+/B.ma/. pai/Cp.s/en ajit/Cp.s/en) '
                          'chairman/Cp.s/en)) (last/Ma/en week/Cc.s/en))) (by/T/en ((up/M/en was/P.s.<f-----/en) ('
                          'cellular/Ma/en usage/Cc.s/en))))',
                          '(said/Pd.{sr}.<f----- */C *)'),
            [{}])

    def test_edge_matches_pattern_simple1(self):
        self.assertTrue(edge_matches_pattern(hedge('(a b)'), '(a b)'))

    def test_edge_matches_pattern_simple2(self):
        self.assertFalse(edge_matches_pattern(hedge('(a b)'), '(a a)'))

    def test_edge_matches_pattern_wildcard1(self):
        self.assertTrue(edge_matches_pattern(hedge('(is/Pd graphbrain/Cp.s great/C)'), '(is/Pd graphbrain/Cp.s *)'))

    def test_edge_matches_pattern_wildcard2(self):
        self.assertFalse(edge_matches_pattern(hedge('(was/Pd graphbrain/Cp.s great/C)'), '(is/Pd graphbrain/Cp.s *)'))

    def test_edge_matches_pattern_atomic_wildcard1(self):
        self.assertTrue(edge_matches_pattern(hedge('(is/Pd graphbrain/Cp.s great/C)'), '(is/Pd graphbrain/Cp.s .)'))

    def test_edge_matches_pattern_atomic_wildcard2(self):
        self.assertFalse(
            edge_matches_pattern(hedge('(was/Pd graphbrain /Cp.s great/C)'), '(is/Pd.sc graphbrain/Cp.s .)'))

    def test_edge_matches_pattern_atomic_wildcard3(self):
        self.assertFalse(
            edge_matches_pattern(hedge('(is/Pd graphbrain/Cp.s (fairly/M great/C))'), '(is/Pd graphbrain/Cp.s .)'))

    def test_edge_matches_pattern_edge_wildcard1(self):
        self.assertTrue(
            edge_matches_pattern(hedge('(is/Pd graphbrain/Cp.s (fairly/M great/C))'), '(is/Pd graphbrain/Cp.s (*))'))

    def test_edge_matches_pattern_edge_wildcard2(self):
        self.assertFalse(edge_matches_pattern(hedge('(is/Pd graphbrain/Cp.s great/C)'), '(is/Pd graphbrain/Cp.s (*))'))

    def test_edge_matches_pattern_open_ended1(self):
        self.assertTrue(edge_matches_pattern(hedge('(is/Pd graphbrain/Cp.s great/C)'), '(is/Pd graphbrain/Cp.s * ...)'))

    def test_edge_matches_pattern_open_ended2(self):
        self.assertTrue(
            edge_matches_pattern(hedge('(is/Pd graphbrain/Cp.s great/C extra/C)'), '(is/Pd graphbrain/Cp.s * ...)'))

    def test_edge_matches_pattern_open_ended3(self):
        self.assertFalse(
            edge_matches_pattern(hedge('(is/Pd humanity/Cp.s great/C extra/C)'), '(is/Pd graphbrain/Cp.s * ...)'))

    def test_match_pattern_complex(self):
        s = ('(says/Pd.rr.|f--3s-/en (calls/Pr.so.|f--3s-/en */C (*/M (draconian/Ma/en (+/B.am/. coronavirus/Cc.s/en '
             'restrictions/Cc.p/en)))) */R)')
        pattern = hedge(s)
        s = ("(says/Pd.rr.|f--3s-/en ((+/B.aaa/. ali/Cp.s/en wentworth/Cp.s/en"
             " tests/Cp.p/en) (+/B.aaa/. ali/Cp.s/en wentworth/Cp.s/en "
             "tests/Cp.p/en) (for/T/en coronavirus/Cp.s/en)) "
             "('s/Pr.s.|f--3s-/en (+/B.aaa/. ali/Cp.s/en wentworth/Cp.s/en "
             "tests/Cp.p/en)))")
        edge = hedge(s)
        self.assertEqual(match_pattern(edge, pattern), [])

    def test_match_pattern_fun_var1(self):
        s = "((var */P PRED) */C */C)"
        pattern = hedge(s)
        s = "(says/Pd x/C y/C)"
        edge = hedge(s)
        self.assertEqual(match_pattern(edge, pattern), [{'PRED': hedge('says/Pd')}])

    def test_match_pattern_fun_var2(self):
        s = "((var (*/M VERB/P) PRED) */C */C)"
        pattern = hedge(s)
        s = "((will/M say/Pd) x/C y/C)"
        edge = hedge(s)
        self.assertEqual(match_pattern(edge, pattern), [{'PRED': hedge('(will/M say/Pd)'), 'VERB': hedge('say/Pd')}])

    def test_match_pattern_fun_var3(self):
        s = "((var (*/M VERB/P) PRED) */C */C)"
        pattern = hedge(s)
        s = "((var (will/M say/Pd) PRED) x/C y/C)"
        edge = hedge(s)
        self.assertEqual(
            match_pattern(edge, pattern), [{'PRED': hedge('(will/M say/Pd)'), 'VERB': hedge('say/Pd')}])

    def test_match_pattern_fun_atoms1(self):
        s = "(atoms */P)"
        pattern = hedge(s)
        s = "says/Pd.rr.|f--3s-/en"
        edge = hedge(s)
        self.assertEqual(match_pattern(edge, pattern), [{}])

    def test_match_pattern_fun_atoms2(self):
        s = "(atoms says/P)"
        pattern = hedge(s)
        s = "says/Pd.rr.|f--3s-/en"
        edge = hedge(s)
        self.assertEqual(match_pattern(edge, pattern), [{}])

    def test_match_pattern_fun_atoms3(self):
        s = "(atoms say/P)"
        pattern = hedge(s)
        s = "says/Pd.rr.|f--3s-/en"
        edge = hedge(s)
        self.assertEqual(match_pattern(edge, pattern), [])

    def test_match_pattern_fun_atoms4(self):
        s = "(atoms VERB/P)"
        pattern = hedge(s)
        s = "says/Pd.rr.|f--3s-/en"
        edge = hedge(s)
        self.assertEqual(match_pattern(edge, pattern), [{'VERB': hedge('says/Pd.rr.|f--3s-/en')}])

    def test_match_pattern_fun_atoms5(self):
        s = "(atoms VERB/P)"
        pattern = hedge(s)
        s = "(will/M say/Pd)"
        edge = hedge(s)
        self.assertEqual(match_pattern(edge, pattern), [{'VERB': hedge('say/Pd')}])

    def test_match_pattern_fun_atoms6(self):
        s = "(atoms */M VERB/P)"
        pattern = hedge(s)
        s = "(will/M say/Pd)"
        edge = hedge(s)
        self.assertEqual(match_pattern(edge, pattern), [{'VERB': hedge('say/Pd')}])

    def test_match_pattern_fun_atoms7(self):
        s = "(atoms not/M VERB/P)"
        pattern = hedge(s)
        s = "(will/M say/Pd)"
        edge = hedge(s)
        self.assertEqual(match_pattern(edge, pattern), [])

    def test_match_pattern_fun_atoms8(self):
        s = "(atoms will/M VERB/P)"
        pattern = hedge(s)
        s = "(will/M (not/M say/Pd))"
        edge = hedge(s)
        self.assertEqual(match_pattern(edge, pattern), [{'VERB': hedge('say/Pd')}])

    def test_match_pattern_fun_atoms9(self):
        s = "(atoms MOD/M VERB/P)"
        pattern = hedge(s)
        s = "(will/M (not/M say/Pd))"
        edge = hedge(s)
        result = match_pattern(edge, pattern)
        self.assertIn({'MOD': hedge('not/M'), 'VERB': hedge('say/Pd')}, result)
        self.assertIn({'MOD': hedge('will/M'), 'VERB': hedge('say/Pd')}, result)
        self.assertEqual(len(result), 2)

    def test_match_pattern_fun_atoms10(self):
        s = "((atoms MOD/M VERB/P.so) * *)"
        pattern = hedge(s)
        s = "((will/M (not/M say/Pd.so)) x/C y/C)"
        edge = hedge(s)
        result = match_pattern(edge, pattern)
        self.assertIn({'MOD': hedge('not/M'), 'VERB': hedge('say/Pd.so')}, result)
        self.assertIn({'MOD': hedge('will/M'), 'VERB': hedge('say/Pd.so')}, result)
        self.assertEqual(len(result), 2)

    def test_match_pattern_fun_atoms11(self):
        s = "((atoms MOD/M VERB/P.so) X Y)"
        pattern = hedge(s)
        s = "((will/M (not/M say/Pd.so)) x/C y/C)"
        edge = hedge(s)
        result = match_pattern(edge, pattern)
        self.assertIn(
            {'MOD': hedge('not/M'), 'VERB': hedge('say/Pd.so'), 'X': hedge('x/C'), 'Y': hedge('y/C')}, result)
        self.assertIn(
            {'MOD': hedge('will/M'), 'VERB': hedge('say/Pd.so'), 'X': hedge('x/C'), 'Y': hedge('y/C')}, result)
        self.assertEqual(len(result), 2)

    def test_match_pattern_fun_lemma1(self):
        s = "(lemma say/P)"
        pattern = hedge(s)
        s = "say/Pd"
        edge = hedge(s)
        self.assertEqual(match_pattern(edge, pattern, hg=self.hg), [{}])
    
    def test_match_pattern_fun_lemma2(self):
        s = "(var (lemma say/P) VERB)"
        pattern = hedge(s)
        s = "say/Pd"
        edge = hedge(s)
        self.assertEqual(match_pattern(edge, pattern, hg=self.hg), [{'VERB': hedge('say/Pd')}])

    def test_match_pattern_fun_lemma3(self):
        s = "(lemma say/P)"
        pattern = hedge(s)
        s = "talk/Pd"
        edge = hedge(s)
        self.assertEqual(match_pattern(edge, pattern, hg=self.hg), [])

    def test_match_pattern_fun_lemma4(self):
        self.hg.add((const.lemma_pred, 'said/Pd', 'say/P'))
        s = "(lemma say/P)"
        pattern = hedge(s)
        s = "said/Pd"
        edge = hedge(s)
        self.assertEqual(match_pattern(edge, pattern, hg=self.hg), [{}])

    def test_match_pattern_fun_argroles1(self):
        self.hg.add((const.lemma_pred, 'said/Pd.so', 'say/P'))
        s = "((lemma say/P.{so}) */C */C)"
        pattern = hedge(s)
        s = "(said/Pd.so x/C y/C)"
        edge = hedge(s)
        self.assertEqual(match_pattern(edge, pattern, hg=self.hg), [{}])

    def test_match_pattern_fun_argroles2(self):
        s = "((atoms has/M said/P.{so}) */C */C)"
        pattern = hedge(s)
        s = "((has/M said/Pd.so) x/C y/C)"
        edge = hedge(s)
        self.assertEqual(match_pattern(edge, pattern, hg=self.hg), [{}])

    def test_match_pattern_fun_argroles3(self):
        self.hg.add((const.lemma_pred, 'said/Pd.so', 'say/P'))
        s = "((atoms (lemma say/P.{so})) */C */C)"
        pattern = hedge(s)
        s = "((has/M said/Pd.so) x/C y/C)"
        edge = hedge(s)
        self.assertEqual(match_pattern(edge, pattern, hg=self.hg), [{}])

    def test_match_pattern_fun_argroles4(self):
        self.hg.add((const.lemma_pred, 'said/Pd.so', 'say/P'))
        s = "((atoms has/M (lemma say/P.{so})) */C */C)"
        pattern = hedge(s)
        s = "((has/M said/Pd.so) x/C y/C)"
        edge = hedge(s)
        self.assertEqual(match_pattern(edge, pattern, hg=self.hg), [{}])

    def test_match_pattern_fun_argroles5(self):
        self.hg.add((const.lemma_pred, 'said/Pd.so', 'say/P'))
        s = "((atoms has/M not/M (lemma say/P.{so})) */C */C)"
        pattern = hedge(s)
        s = "((has/M said/Pd.so) x/C y/C)"
        edge = hedge(s)
        self.assertEqual(match_pattern(edge, pattern, hg=self.hg), [])

    def test_match_pattern_fun_argroles6(self):
        self.hg.add((const.lemma_pred, 'said/Pd.so', 'say/P'))
        s = "((var (atoms has/M (lemma say/P.{so})) X) */C */C)"
        pattern = hedge(s)
        s = "((has/M said/Pd.so) x/C y/C)"
        edge = hedge(s)
        self.assertEqual(match_pattern(edge, pattern, hg=self.hg), [{'X': hedge('(has/M said/Pd.so)')}])

    def test_match_pattern_fun_argroles8(self):
        s = ("""
        (says/Pd.{r}.|f--3s-/en (var (executes/P.{o}.|f--3s-/en (+/B.{mm}/. (15/M#/en people/Cc.p/en)
        (for/Br.{ma}/en 10/C#/en (+/B.{ma}/. convictions/Cc.p/en terrorism/Cc.s/en)))) CLAIM))
        """)
        pattern = hedge(s)
        s = ("""
        (says/Pd.r.|f--3s-/en (var (executes/P.o.|f--3s-/en (+/B.mm/. (15/M#/en people/Cc.p/en) (for/Br.ma/en 10/C#/en
        (+/B.am/. terrorism/Cc.s/en convictions/Cc.p/en)))) CLAIM))
        """)
        edge = hedge(s)
        matches = match_pattern(edge, pattern, hg=self.hg)
        self.assertFalse(matches == [])
        # self.assertEqual(matches[0]['ACTOR'], hedge('jordan/Cp.s/en'))

    def test_match_pattern_fun_argroles9(self):
        s = ("""
        (says/Pd.{sr}.|f--3s-/en (var jordan/Cp.s/en ACTOR) (var (executes/P.{o}.|f--3s-/en (+/B.{mm}/.
        (15/M#/en people/Cc.p/en) (for/Br.{ma}/en 10/C#/en (+/B.{ma}/. convictions/Cc.p/en terrorism/Cc.s/en)))) CLAIM))
        """)
        pattern = hedge(s)
        s = ("""
        (says/Pd.sr.|f--3s-/en (var jordan/Cp.s/en ACTOR) (var (executes/P.o.|f--3s-/en (+/B.mm/.
        (15/M#/en people/Cc.p/en) (for/Br.ma/en 10/C#/en (+/B.am/. terrorism/Cc.s/en convictions/Cc.p/en)))) CLAIM))
        """)
        edge = hedge(s)
        matches = match_pattern(edge, pattern, hg=self.hg)
        self.assertFalse(matches == [])
        self.assertEqual(matches[0]['ACTOR'], hedge('jordan/Cp.s/en'))

    def test_counter1(self):
        pc = PatternCounter()
        pc.count(hedge('((not/M is/P.sc) mary/C (not/M nice/C))'))

        self.assertTrue(pc.patterns[hedge('(*/P.sc */C */C)')] == 1)
        self.assertTrue(pc.patterns[hedge('((*/M */P.sc) */C */C)')] == 1)
        self.assertTrue(pc.patterns[hedge('(*/P.sc */C (*/M */C))')] == 1)
        self.assertTrue(pc.patterns[hedge('((*/M */P.sc) */C (*/M */C))')] == 1)
        self.assertTrue(pc.patterns[hedge('(*/M */P.sc)')] == 1)
        self.assertFalse(pc.patterns[hedge('(*/M is/P.sc)')] == 1)
        self.assertTrue(pc.patterns[hedge('(*/M */C)')] == 1)

    def test_counter2(self):
        pc = PatternCounter(count_subedges=False)
        pc.count(hedge('((not/M is/P.sc) mary/C (not/M nice/C))'))

        self.assertTrue(pc.patterns[hedge('(*/P.sc */C */C)')] == 1)
        self.assertTrue(pc.patterns[hedge('((*/M */P.sc) */C */C)')] == 1)
        self.assertTrue(pc.patterns[hedge('(*/P.sc */C (*/M */C))')] == 1)
        self.assertTrue(pc.patterns[hedge('((*/M */P.sc) */C (*/M */C))')] == 1)
        self.assertFalse(pc.patterns[hedge('(*/M */P.sc)')] == 1)
        self.assertFalse(pc.patterns[hedge('(*/M */C)')] == 1)

    def test_counter3(self):
        pc = PatternCounter(match_roots={'*/P'})
        pc.count(hedge('((not/M is/P.sc) mary/C (not/M nice/C))'))

        self.assertTrue(pc.patterns[hedge('(*/P.sc */C */C)')] == 0)
        self.assertTrue(pc.patterns[hedge('((*/M */P.sc) */C */C)')] == 0)
        self.assertTrue(pc.patterns[hedge('((not/M is/P.sc) */C */C)')] == 1)
        self.assertTrue(pc.patterns[hedge('(*/P.sc */C (*/M */C))')] == 0)
        self.assertTrue(pc.patterns[hedge('((*/M */P.sc) */C (*/M */C))')] == 0)
        self.assertTrue(pc.patterns[hedge('(*/M */P.sc)')] == 0)
        self.assertTrue(pc.patterns[hedge('(not/M is/P.sc)')] == 1)
        self.assertTrue(pc.patterns[hedge('(*/M */C)')] == 1)

    def test_counter4(self):
        pc = PatternCounter(match_roots={'./P'})
        pc.count(hedge('((not/M is/P.sc) mary/C (not/M nice/C))'))

        self.assertTrue(pc.patterns[hedge('(*/P.sc */C */C)')] == 0)
        self.assertTrue(pc.patterns[hedge('((*/M */P.sc) */C */C)')] == 0)
        self.assertTrue(pc.patterns[hedge('((*/M is/P.sc) */C */C)')] == 1)
        self.assertTrue(pc.patterns[hedge('(*/P.sc */C (*/M */C))')] == 0)
        self.assertTrue(pc.patterns[hedge('((*/M */P.sc) */C (*/M */C))')] == 0)
        self.assertTrue(pc.patterns[hedge('(*/M */P.sc)')] == 0)
        self.assertTrue(pc.patterns[hedge('(*/M is/P.sc)')] == 1)
        self.assertTrue(pc.patterns[hedge('(*/M */C)')] == 1)

    def test_counter5(self):
        pc = PatternCounter(count_subedges=False)
        pc.count(hedge('((not/Mn is/Pd.sc) mary/Cp.s (very/M nice/Cc.s))'))

        self.assertTrue(pc.patterns[hedge('(*/P.sc */C */C)')] == 1)
        self.assertTrue(pc.patterns[hedge('((*/M */P.sc) */C */C)')] == 1)
        self.assertTrue(pc.patterns[hedge('(*/P.sc */C (*/M */C))')] == 1)
        self.assertTrue(pc.patterns[hedge('((*/M */P.sc) */C (*/M */C))')] == 1)
        self.assertFalse(pc.patterns[hedge('(*/M */P.sc)')] == 1)
        self.assertFalse(pc.patterns[hedge('(*/M */C)')] == 1)

    def test_counter6(self):
        pc = PatternCounter(count_subedges=False, match_subtypes={'*/M'})
        pc.count(hedge('((not/Mn is/Pd.sc) mary/Cp.s (very/M nice/Cc.s))'))

        self.assertTrue(pc.patterns[hedge('(*/P.sc */C */C)')] == 1)
        self.assertTrue(pc.patterns[hedge('((*/Mn */P.sc) */C */C)')] == 1)
        self.assertTrue(pc.patterns[hedge('(*/P.sc */C (*/M */C))')] == 1)
        self.assertTrue(pc.patterns[hedge('((*/Mn */P.sc) */C (*/M */C))')] == 1)
        self.assertFalse(pc.patterns[hedge('(*/Mn */P.sc)')] == 1)
        self.assertFalse(pc.patterns[hedge('(*/M */C)')] == 1)

    def test_counter7(self):
        pc = PatternCounter(count_subedges=False, match_subtypes={'C', 'M', 'P'})
        pc.count(hedge('((not/Mn is/Pd.sc) mary/Cp.s (very/M nice/Cc.s))'))

        self.assertTrue(pc.patterns[hedge('(*/Pd.sc */Cp */Cc)')] == 1)
        self.assertTrue(pc.patterns[hedge('((*/Mn */Pd.sc) */Cp */Cc)')] == 1)
        self.assertTrue(pc.patterns[hedge('(*/Pd.sc */Cp (*/M */Cc))')] == 1)
        self.assertTrue(pc.patterns[hedge('((*/Mn */Pd.sc) */Cp (*/M */Cc))')] == 1)
        self.assertFalse(pc.patterns[hedge('(*/Mn */P.sc)')] == 1)
        self.assertFalse(pc.patterns[hedge('(*/M */Cc)')] == 1)

    def test_counter8(self):
        pc = PatternCounter()
        pc.count(hedge('(a/M (b/M (c/M (d/M e/C))))'))

        self.assertTrue(pc.patterns[hedge('(*/M */C)')] == 4)
        self.assertTrue(pc.patterns[hedge('(*/M (*/M */C))')] == 3)
        self.assertFalse(pc.patterns[hedge('(*/M (*/M (*/M */C)))')] == 2)
        self.assertFalse(pc.patterns[hedge('(*/M (*/M (*/M (*/M */C)))(')] == 1)

    def test_counter9(self):
        pc = PatternCounter(depth=3)
        pc.count(hedge('(a/M (b/M (c/M (d/M e/C))))'))

        self.assertTrue(pc.patterns[hedge('(*/M */C)')] == 4)
        self.assertTrue(pc.patterns[hedge('(*/M (*/M */C))')] == 3)
        self.assertTrue(pc.patterns[hedge('(*/M (*/M (*/M */C)))')] == 2)
        self.assertFalse(pc.patterns[hedge('(*/M (*/M (*/M (*/M */C)))(')] == 1)

    def test_counter10(self):
        pc = PatternCounter(depth=4)
        pc.count(hedge('(a/M (b/M (c/M (d/M e/C))))'))

        self.assertTrue(pc.patterns[hedge('(*/M */C)')] == 4)
        self.assertTrue(pc.patterns[hedge('(*/M (*/M */C))')] == 3)
        self.assertTrue(pc.patterns[hedge('(*/M (*/M (*/M */C)))')] == 2)
        self.assertTrue(pc.patterns[hedge('(*/M (*/M (*/M (*/M */C)))(')] == 1)

    def test_counter11(self):
        pc = PatternCounter(depth=1)
        pc.count(hedge('(a/M (b/M (c/M (d/M e/C))))'))

        self.assertTrue(pc.patterns[hedge('(*/M */C)')] == 4)
        self.assertFalse(pc.patterns[hedge('(*/M (*/M */C))')] == 3)
        self.assertFalse(pc.patterns[hedge('(*/M (*/M (*/M */C)))')] == 2)
        self.assertFalse(pc.patterns[hedge('(*/M (*/M (*/M (*/M */C)))(')] == 1)

    def test_match_pattern_real_case1(self):
        s = ("((var ((atoms (lemma be/M)) *) PRED) (var * ARG1) (var * ARG2))")
        pattern = hedge(s)
        s = ("""((var picks/Pd.sox.|f--3s-/en PRED) (var trump/Cc.s/en ARG1) (var (+/B.am/. lawyer/Cc.s/en (+/B.am/. 
        ty/Cp.s/en cobb/Cp.s/en)) ARG2) ((to/Mi/en handle/Pd.o.-i-----/en) (+/B.am/. russia/Cp.s/en probe/Cc.s/en)))
        """)
        edge = hedge(s)
        matches = match_pattern(edge, pattern, hg=self.hg)
        self.assertEqual(matches, [])


if __name__ == '__main__':
    unittest.main()
