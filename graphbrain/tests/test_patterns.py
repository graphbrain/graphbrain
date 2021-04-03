import unittest

from graphbrain import hedge
from graphbrain.patterns import PatternCounter


class Testpatterns(unittest.TestCase):
    def test_counter1(self):
        pc = PatternCounter()
        pc.count(hedge('((not/M is/P.sc) mary/C (not/M nice/C))'))

        self.assertTrue(pc.patterns[hedge('(*/P.sc */C */C)')] == 1)
        self.assertTrue(pc.patterns[hedge('((*/M */P.sc) */C */C)')] == 1)
        self.assertTrue(pc.patterns[hedge('(*/P.sc */C (*/M */C))')] == 1)
        self.assertTrue(
            pc.patterns[hedge('((*/M */P.sc) */C (*/M */C))')] == 1)
        self.assertTrue(pc.patterns[hedge('(*/M */P.sc)')] == 1)
        self.assertFalse(pc.patterns[hedge('(*/M is/P.sc)')] == 1)
        self.assertTrue(pc.patterns[hedge('(*/M */C)')] == 1)

    def test_counter2(self):
        pc = PatternCounter(count_subedges=False)
        pc.count(hedge('((not/M is/P.sc) mary/C (not/M nice/C))'))

        self.assertTrue(pc.patterns[hedge('(*/P.sc */C */C)')] == 1)
        self.assertTrue(pc.patterns[hedge('((*/M */P.sc) */C */C)')] == 1)
        self.assertTrue(pc.patterns[hedge('(*/P.sc */C (*/M */C))')] == 1)
        self.assertTrue(
            pc.patterns[hedge('((*/M */P.sc) */C (*/M */C))')] == 1)
        self.assertFalse(pc.patterns[hedge('(*/M */P.sc)')] == 1)
        self.assertFalse(pc.patterns[hedge('(*/M */C)')] == 1)

    def test_counter3(self):
        pc = PatternCounter(match_roots={'*/P'})
        pc.count(hedge('((not/M is/P.sc) mary/C (not/M nice/C))'))

        self.assertTrue(pc.patterns[hedge('(*/P.sc */C */C)')] == 0)
        self.assertTrue(pc.patterns[hedge('((*/M */P.sc) */C */C)')] == 0)
        self.assertTrue(pc.patterns[hedge('((not/M is/P.sc) */C */C)')] == 1)
        self.assertTrue(pc.patterns[hedge('(*/P.sc */C (*/M */C))')] == 0)
        self.assertTrue(
            pc.patterns[hedge('((*/M */P.sc) */C (*/M */C))')] == 0)
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
        self.assertTrue(
            pc.patterns[hedge('((*/M */P.sc) */C (*/M */C))')] == 0)
        self.assertTrue(pc.patterns[hedge('(*/M */P.sc)')] == 0)
        self.assertTrue(pc.patterns[hedge('(*/M is/P.sc)')] == 1)
        self.assertTrue(pc.patterns[hedge('(*/M */C)')] == 1)

    def test_counter5(self):
        pc = PatternCounter(count_subedges=False)
        pc.count(hedge('((not/Mn is/Pd.sc) mary/Cp.s (very/M nice/Cc.s))'))

        self.assertTrue(pc.patterns[hedge('(*/P.sc */C */C)')] == 1)
        self.assertTrue(pc.patterns[hedge('((*/M */P.sc) */C */C)')] == 1)
        self.assertTrue(pc.patterns[hedge('(*/P.sc */C (*/M */C))')] == 1)
        self.assertTrue(
            pc.patterns[hedge('((*/M */P.sc) */C (*/M */C))')] == 1)
        self.assertFalse(pc.patterns[hedge('(*/M */P.sc)')] == 1)
        self.assertFalse(pc.patterns[hedge('(*/M */C)')] == 1)

    def test_counter6(self):
        pc = PatternCounter(count_subedges=False, match_subtypes={'*/M'})
        pc.count(hedge('((not/Mn is/Pd.sc) mary/Cp.s (very/M nice/Cc.s))'))

        self.assertTrue(pc.patterns[hedge('(*/P.sc */C */C)')] == 1)
        self.assertTrue(pc.patterns[hedge('((*/Mn */P.sc) */C */C)')] == 1)
        self.assertTrue(pc.patterns[hedge('(*/P.sc */C (*/M */C))')] == 1)
        self.assertTrue(
            pc.patterns[hedge('((*/Mn */P.sc) */C (*/M */C))')] == 1)
        self.assertFalse(pc.patterns[hedge('(*/Mn */P.sc)')] == 1)
        self.assertFalse(pc.patterns[hedge('(*/M */C)')] == 1)

    def test_counter7(self):
        pc = PatternCounter(count_subedges=False,
                            match_subtypes={'C', 'M', 'P'})
        pc.count(hedge('((not/Mn is/Pd.sc) mary/Cp.s (very/M nice/Cc.s))'))

        self.assertTrue(pc.patterns[hedge('(*/Pd.sc */Cp */Cc)')] == 1)
        self.assertTrue(pc.patterns[hedge('((*/Mn */Pd.sc) */Cp */Cc)')] == 1)
        self.assertTrue(pc.patterns[hedge('(*/Pd.sc */Cp (*/M */Cc))')] == 1)
        self.assertTrue(
            pc.patterns[hedge('((*/Mn */Pd.sc) */Cp (*/M */Cc))')] == 1)
        self.assertFalse(pc.patterns[hedge('(*/Mn */P.sc)')] == 1)
        self.assertFalse(pc.patterns[hedge('(*/M */Cc)')] == 1)

    def test_counter8(self):
        pc = PatternCounter()
        pc.count(hedge('(a/M (b/M (c/M (d/M e/C))))'))

        self.assertTrue(pc.patterns[hedge('(*/M */C)')] == 4)
        self.assertTrue(pc.patterns[hedge('(*/M (*/M */C))')] == 3)
        self.assertFalse(pc.patterns[hedge('(*/M (*/M (*/M */C)))')] == 2)
        self.assertFalse(
            pc.patterns[hedge('(*/M (*/M (*/M (*/M */C)))(')] == 1)

    def test_counter9(self):
        pc = PatternCounter(depth=3)
        pc.count(hedge('(a/M (b/M (c/M (d/M e/C))))'))

        self.assertTrue(pc.patterns[hedge('(*/M */C)')] == 4)
        self.assertTrue(pc.patterns[hedge('(*/M (*/M */C))')] == 3)
        self.assertTrue(pc.patterns[hedge('(*/M (*/M (*/M */C)))')] == 2)
        self.assertFalse(
            pc.patterns[hedge('(*/M (*/M (*/M (*/M */C)))(')] == 1)

    def test_counter10(self):
        pc = PatternCounter(depth=4)
        pc.count(hedge('(a/M (b/M (c/M (d/M e/C))))'))

        self.assertTrue(pc.patterns[hedge('(*/M */C)')] == 4)
        self.assertTrue(pc.patterns[hedge('(*/M (*/M */C))')] == 3)
        self.assertTrue(pc.patterns[hedge('(*/M (*/M (*/M */C)))')] == 2)
        self.assertTrue(
            pc.patterns[hedge('(*/M (*/M (*/M (*/M */C)))(')] == 1)

    def test_counter11(self):
        pc = PatternCounter(depth=1)
        pc.count(hedge('(a/M (b/M (c/M (d/M e/C))))'))

        self.assertTrue(pc.patterns[hedge('(*/M */C)')] == 4)
        self.assertFalse(pc.patterns[hedge('(*/M (*/M */C))')] == 3)
        self.assertFalse(pc.patterns[hedge('(*/M (*/M (*/M */C)))')] == 2)
        self.assertFalse(
            pc.patterns[hedge('(*/M (*/M (*/M (*/M */C)))(')] == 1)


if __name__ == '__main__':
    unittest.main()
