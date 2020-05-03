import unittest
from graphbrain import *
from graphbrain.logic import *


class TestLogic(unittest.TestCase):
    def setUp(self):
        self.hg = hgraph('test.hg')
        self.hg.destroy()
        edge = hedge('(is/Pd.sc (the/M sun/C) red/C)')
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
        """.strip())
        result = list(match[0] for match in eval_rule(self.hg, rule))
        self.assertEqual(result, [hedge('(prop/P (the/M sun/C) red/C)')])
