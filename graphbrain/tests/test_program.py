import unittest
from graphbrain import *
from graphbrain.logic.program import *


class TestProgram(unittest.TestCase):
    def test_is_var(self):
        self.assertTrue(is_var('X'))
        self.assertFalse(is_var('x'))
        self.assertTrue(is_var('Hello'))
        self.assertTrue(is_var('Something/pd.xxx'))
        self.assertFalse(is_var('graphbrain/cp.s/1'))
        self.assertFalse(is_var(('X', 'Y', 'Z')))

    def test_pattern_and_variables_1(self):
        pattern, variables = pattern_and_variables(str2ent('(X X X)'))
        self.assertEqual(pattern, ['*', '*', '*'])
        self.assertEqual(variables, {'X'})

    def test_pattern_and_variables_2(self):
        pattern, variables = pattern_and_variables(str2ent('(X Y Z)'))
        self.assertEqual(pattern, ['*', '*', '*'])
        self.assertEqual(variables, {'X', 'Y', 'Z'})

    def test_pattern_and_variables_3(self):
        pattern, variables = pattern_and_variables(str2ent('(is/p * G)'))
        self.assertEqual(pattern, ['is/p', '*', '*'])
        self.assertEqual(variables, {'G'})

    def test_match_variables_1(self):
        expression = str2ent('(X Y Z)')
        edge = str2ent('(is/pd graphbrain/c great/c)')
        variables = {}
        self.assertTrue(match_variables(expression, edge, variables))
        self.assertEqual(variables, {'X': 'is/pd',
                                     'Y': 'graphbrain/c',
                                     'Z': 'great/c'})

    def test_match_variables_2(self):
        expression = str2ent('(X Y Z)')
        edge = str2ent('(is/pd graphbrain/c)')
        variables = {}
        self.assertTrue(match_variables(expression, edge, variables))
        self.assertEqual(variables, {'X': 'is/pd',
                                     'Y': 'graphbrain/c'})

    def test_match_variables_3(self):
        expression = str2ent('(X X X)')
        edge = str2ent('(is/pd graphbrain/c great/c)')
        variables = {}
        self.assertFalse(match_variables(expression, edge, variables))

    def test_match_variables_4(self):
        expression = str2ent('(is/pd * (Z Y))')
        edge = str2ent('(is/pd graphbrain/c (so/m great/c))')
        variables = {}
        self.assertTrue(match_variables(expression, edge, variables))
        self.assertEqual(variables, {'Z': 'so/m',
                                     'Y': 'great/c'})

    def test_match_expression_1(self):
        hg = hypergraph('test.hg')
        hg.destroy()

        hg.add(str2ent('(is/pd graphbrain/c great/c)'))

        matches = list(match_expression(hg, str2ent('(is/pd Y Z)')))
        self.assertEqual(matches, [{'Y': 'graphbrain/c', 'Z': 'great/c'}])

        hg.close()

    def test_match_expression_2(self):
        hg = hypergraph('test.hg')
        hg.destroy()

        hg.add(str2ent('(was/pd graphbrain/c great/c)'))

        matches = list(match_expression(hg, str2ent('(is/pd Y Z)')))
        self.assertEqual(matches, [])

        hg.close()

    def test_match_expression_3(self):
        hg = hypergraph('test.hg')
        hg.destroy()

        hg.add(str2ent('(is/pd graphbrain/c great/c)'))

        matches = list(match_expression(hg, str2ent('(is/pd * Z)')))
        self.assertEqual(matches, [{'Z': 'great/c'}])

        hg.close()

    def test_match_expression_4(self):
        hg = hypergraph('test.hg')
        hg.destroy()

        hg.add(str2ent('(is/pd graphbrain/c (so/m great/c))'))
        hg.add(str2ent('(is/pd graphbrain/c (not/m great/c))'))

        matches = list(match_expression(hg, str2ent('(is/pd * (X Y))')))
        self.assertEqual(matches, [{'X': 'not/m', 'Y': 'great/c'},
                                   {'X': 'so/m', 'Y': 'great/c'}])

        matches = list(match_expression(hg, str2ent('(is/pd * (so/m Y))')))
        self.assertEqual(matches, [{'Y': 'great/c'}])

        hg.close()


if __name__ == '__main__':
    unittest.main()
