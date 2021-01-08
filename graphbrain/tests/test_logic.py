import unittest

from graphbrain import *
from graphbrain.logic import *


class TestLogic(unittest.TestCase):
    def setUp(self):
        self.hg = hgraph('test.hg')
        self.hg.destroy()
        edge = hedge('(is/Pd.sc (the/M sun/C) red/C)')
        self.hg.add(edge)
        edge = hedge('(is/Pd.sc red/C color/C)')
        self.hg.add(edge)

    def tearDown(self):
        self.hg.close()

    def test_is_rule(self):
        rule = hedge("""
        (:- (is/P SUBJ OBJ) (PRED/P.so SUBJ OBJ))
        """.strip())
        self.assertTrue(is_rule(rule))
        rule = hedge("""
        (is (is/P SUBJ OBJ) (PRED/P.so SUBJ OBJ))
        """.strip())
        self.assertFalse(is_rule(rule))
        rule = hedge("""
        (:- (PRED/P.so SUBJ OBJ))
        """.strip())
        self.assertFalse(is_rule(rule))

    def test_eval_rule(self):
        rule = hedge("""
        (:- (prop/P ENTITY PROP) (is/Pd.sc ENTITY PROP))
        """)
        result = list(inference.edge for inference in eval_rule(self.hg, rule))
        self.assertEqual(
            result, [hedge('(prop/P (the/M sun/C) red/C)'),
                     hedge('(prop/P red/C color/C)')])

    def test_eval_rule_and(self):
        rule = hedge("""
        (:- (color/P ENTITY COLOR)
            (and (is/Pd.sc (the/M ENTITY) COLOR)
                 (is/Pd.sc COLOR color/C)))
        """)
        result = list(inference.edge for inference in eval_rule(self.hg, rule))
        self.assertEqual(
            result, [hedge('(color/P sun/C red/C)')])
