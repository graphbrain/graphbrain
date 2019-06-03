import unittest
from graphbrain import *
from graphbrain.hyperlog.program import *


class TestProgram(unittest.TestCase):
    def setUp(self):
        self.hg = hypergraph('test.hg')
        self.prog = Program(self.hg)

    def tearDown(self):
        self.hg.close()

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

    def test_apply_variables_1(self):
        expression = str2ent('(X Y Z)')
        variables = {'X': 'is/pd',
                     'Y': 'graphbrain/c',
                     'Z': 'great/c'}
        self.assertEqual(ent2str(apply_variables(expression, variables)),
                         '(is/pd graphbrain/c great/c)')

    def test_apply_variables_2(self):
        expression = str2ent('(X X X)')
        variables = {'X': 'is/pd',
                     'Y': 'graphbrain/c',
                     'Z': 'great/c'}
        self.assertEqual(ent2str(apply_variables(expression, variables)),
                         '(is/pd is/pd is/pd)')

    def test_apply_variables_3(self):
        expression = str2ent('(P Y Z)')
        variables = {'X': 'is/pd',
                     'Y': 'graphbrain/c',
                     'Z': 'great/c'}
        self.assertEqual(apply_variables(expression, variables), None)

    def test_apply_variables_4(self):
        expression = str2ent('(X Y Z)')
        variables = {}
        self.assertEqual(apply_variables(expression, variables), None)

    def test_apply_variables_5(self):
        expression = str2ent('(is/pd graphbrain/c (X Y))')
        variables = {'X': 'so/m',
                     'Y': 'great/c'}
        self.assertEqual(ent2str(apply_variables(expression, variables)),
                         '(is/pd graphbrain/c (so/m great/c))')

    def test_match_expressions_1(self):
        self.hg.destroy()

        self.hg.add(str2ent('(is/pd graphbrain/c great/c)'))

        matches = list(self.prog.match_expressions([str2ent('(is/pd Y Z)')]))
        self.assertEqual(matches, [{'Y': 'graphbrain/c', 'Z': 'great/c'}])

    def test_match_expressions_2(self):
        self.hg.destroy()

        self.hg.add(str2ent('(was/pd graphbrain/c great/c)'))

        matches = list(self.prog.match_expressions([str2ent('(is/pd Y Z)')]))
        self.assertEqual(matches, [])

    def test_match_expressions_3(self):
        self.hg.destroy()

        self.hg.add(str2ent('(is/pd graphbrain/c great/c)'))

        matches = list(self.prog.match_expressions([str2ent('(is/pd * Z)')]))
        self.assertEqual(matches, [{'Z': 'great/c'}])

    def test_match_expressions_4(self):
        self.hg.destroy()

        self.hg.add(str2ent('(is/pd graphbrain/c (so/m great/c))'))
        self.hg.add(str2ent('(is/pd graphbrain/c (not/m great/c))'))

        matches = list(
            self.prog.match_expressions([str2ent('(is/pd * (X Y))')]))
        self.assertEqual(matches, [{'X': 'not/m', 'Y': 'great/c'},
                                   {'X': 'so/m', 'Y': 'great/c'}])

        matches = list(self.prog.match_expressions(
            [str2ent('(is/pd * (so/m Y))')]))
        self.assertEqual(matches, [{'Y': 'great/c'}])

    def test_eval_conditions_1(self):
        self.hg.destroy()

        self.hg.add(str2ent('(is/pd graphbrain/c (so/m great/c))'))
        self.hg.add(str2ent('(is/pd graphbrain/c (not/m great/c))'))

        conditions = str2ent('(is/pd * (X Y))')

        matches = list(self.prog.eval_conditions(conditions))
        self.assertEqual(matches, [{'X': 'not/m', 'Y': 'great/c'},
                                   {'X': 'so/m', 'Y': 'great/c'}])

    def test_eval_conditions_2(self):
        self.hg.destroy()

        self.hg.add(str2ent('(is/pd graphbrain/c (so/m great/c))'))
        self.hg.add(str2ent('(is/pd graphbrain/c (not/m great/c))'))

        conds = str2ent('(and (is/pd * (not/m Y)) (is/pd * (X Y)))')

        matches = list(self.prog.eval_conditions(conds))
        self.assertEqual(matches, [{'X': 'not/m', 'Y': 'great/c'}])

    def test_prog_1(self):
        self.hg.destroy()

        self.hg.add(str2ent('(is/pd graphbrain/c (so/m great/c)'))
        self.hg.add(str2ent('(is/pd graphbrain/c (not/m great/c))'))

        prog_text = """
(rule
    (and (P G X)
         (is/pd * (Z Y))
         (is/pd * (not/m Y)))
    (+ (xpto X Y)))
"""

        prog = Program(self.hg, action_type=ActionType.LOG)
        prog.loads(prog_text)
        self.assertEqual((len(prog.exprs)), 1)
        prog.eval()
        self.assertEqual(prog.triggered, 1)
        self.assertEqual(prog.added, 1)
        self.assertEqual(prog.added_edges,
                         [('xpto', ('not/m', 'great/c'), 'great/c')])

    def test_prog_2(self):
        self.hg.destroy()

        self.hg.add(str2ent('(is/pd.sc (my/mp name/cn.s) telmo/cp.s)'))
        self.hg.add(str2ent('(is/pd.sc (my/mp name/cn.s) mary/cp.s)'))

        prog_text = """
(rule
    (P Y telmo/cp.s)
    (+ (has_predicate/pd telmo/cp.s P)))
    
(rule
    (is/pd.sc (* S) C)
    (+ (have/pd.sdo i S C)))
"""

        prog = Program(self.hg, action_type=ActionType.LOG)
        prog.loads(prog_text)
        self.assertEqual((len(prog.exprs)), 2)
        prog.eval()
        self.assertEqual(prog.triggered, 3)
        self.assertEqual(prog.added, 3)
        self.assertEqual(prog.added_edges,
                         [('has_predicate/pd', 'telmo/cp.s', 'is/pd.sc'),
                          ('have/pd.sdo', 'i', 'name/cn.s', 'mary/cp.s'),
                          ('have/pd.sdo', 'i', 'name/cn.s', 'telmo/cp.s')])


if __name__ == '__main__':
    unittest.main()
