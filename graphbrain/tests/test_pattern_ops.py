import unittest

from graphbrain.hyperedge import hedge
from graphbrain.learner.pattern_ops import common_pattern, more_general, is_variable, contains_variable, merge_patterns


class TestPatternOps(unittest.TestCase):
    def test_more_general1(self):
        edge1 = hedge('*')
        edge2 = hedge('moon/C')
        self.assertTrue(more_general(edge1, edge2))

    def test_more_general2(self):
        edge1 = hedge('*')
        edge2 = hedge('((going/M is/P.sx) mary/C (to/T (the/M moon/C)))')
        self.assertTrue(more_general(edge1, edge2))

    def test_more_general3(self):
        edge1 = hedge('((going/M is/P.sx) */C (to/T (the/M moon/C)))')
        edge2 = hedge('((going/M is/P.sx) mary/C (to/T (the/M moon/C)))')
        self.assertTrue(more_general(edge1, edge2))

    def test_more_general4(self):
        edge1 = hedge('((going/M is/P.sx) */C (to/T */C))')
        edge2 = hedge('((going/M is/P.sx) */C (to/T (the/M moon/C)))')
        self.assertTrue(more_general(edge1, edge2))

    def test_is_variable1(self):
        edge = hedge('((going/M is/P.sx) */C (to/T */C))')
        self.assertFalse(is_variable(edge))

    def test_is_variable2(self):
        edge = hedge('(var ((going/M is/P.sx) */C (to/T */C)) X)')
        self.assertTrue(is_variable(edge))
    
    def test_contains_variable1(self):
        edge = hedge('((going/M is/P.sx) */C (to/T */C))')
        self.assertFalse(contains_variable(edge))

    def test_contains_variable2(self):
        edge = hedge('(var ((going/M is/P.sx) */C (to/T */C)) X)')
        self.assertTrue(contains_variable(edge))

    def test_contains_variable3(self):
        edge = hedge('((going/M is/P.sx) (var */C XYZ) (to/T */C))')
        self.assertTrue(contains_variable(edge))

    def test_contains_variable4(self):
        edge = hedge('apples/C')
        self.assertFalse(contains_variable(edge))

    def test_common_pattern1(self):
        edge1 = hedge('(likes/P.so mary/C chess/C)')
        edge2 = hedge('(likes/P.so john/C mary/C)')
        self.assertEqual(common_pattern(edge1, edge2).to_str(), '(likes/P.{so} */C */C)')

    def test_common_pattern2(self):
        edge1 = hedge('(likes/P.so mary/C chess/C)')
        edge2 = hedge('(likes/P.sox john/C mary/C x/C)')
        self.assertEqual(common_pattern(edge1, edge2).to_str(), '(likes/P.{so} */C */C)')

    def test_common_pattern3(self):
        edge1 = hedge('(likes/P mary/C chess/C)')
        edge2 = hedge('(likes/P john/C mary/C)')
        self.assertEqual(common_pattern(edge1, edge2).to_str(), '(likes/P */C */C)')

    def test_common_pattern4(self):
        edge1 = hedge('(likes/P.so mary/C chess/C)')
        edge2 = hedge('(loves/P.so john/C mary/C)')
        self.assertEqual(common_pattern(edge1, edge2).to_str(), '(*/P.{so} */C */C)')

    def test_common_pattern5(self):
        edge1 = hedge('(likes/P.so mary/C chess/C)')
        edge2 = hedge('(loves/P.so mary/C mary/C)')
        self.assertEqual(common_pattern(edge1, edge2).to_str(), '(*/P.{so} mary/C */C)')

    def test_common_pattern6(self):
        edge1 = hedge('(loves/P.so mary/C chess/C)')
        edge2 = hedge('(loves/P.so mary/C mary/C)')
        self.assertEqual(common_pattern(edge1, edge2).to_str(), '(loves/P.{so} mary/C */C)')

    def test_common_pattern7(self):
        edge1 = hedge('mary/C')
        edge2 = hedge('john/C')
        self.assertEqual(common_pattern(edge1, edge2).to_str(), '*/C')

    def test_common_pattern8(self):
        edge1 = hedge('mary/C')
        edge2 = hedge('red/M')
        self.assertEqual(common_pattern(edge1, edge2).to_str(), '*')

    def test_common_pattern9(self):
        edge1 = hedge('mary/C')
        edge2 = hedge('(loves/P.so mary/C mary/C)')
        self.assertEqual(common_pattern(edge1, edge2).to_str(), '*')

    def test_common_pattern10(self):
        edge1 = hedge('(likes/P mary/C chess/C)')
        edge2 = hedge('(likes/P john/C mary/C x/C)')
        self.assertEqual(common_pattern(edge1, edge2).to_str(), '*/R')

    def test_common_pattern11(self):
        edge1 = hedge('(likes/P.so mary/C (of/B.ma games/C chess/C))')
        edge2 = hedge('(likes/P.sox john/C (of/B.ma games/C go/C) x/C)')
        self.assertEqual(common_pattern(edge1, edge2).to_str(), '(likes/P.{so} */C (of/B.ma games/C */C))')

    def test_common_pattern12(self):
        edge1 = hedge('(likes/P.so/en mary/C/en (of/B.ma/en games/C/en chess/C/en))')
        edge2 = hedge('(likes/P.sox/en joe/C/en (of/B.ma/en games/C/en go/C/en) x/C/en)')
        self.assertEqual(common_pattern(edge1, edge2).to_str(), '(likes/P.{so} */C (of/B.ma games/C */C))')

    def test_common_pattern13(self):
        edge1 = hedge(
            """
            (said/Pd.rs.<f-----/en
                (is/P.sc.|f--3s-/en
                    (the/Md/en (only/Ma/en difference/Cc.s/en))
                    (for/Br.ma/en
                        (of/Br.ma/en (the/Md/en amount/Cc.s/en) pixels/Cc.p/en)
                        (of/Br.ma/en (a/Md/en lot/Cc.s/en) content/Cc.s/en)))
                neikirk/Cp.s/en)
            """)
        edge2 = hedge(
            """
            (said/Pd.xsorr.<f-----/en (spent/Pd.xxx.<pf----/en ((and/Mj/en
            with/T/en) (vast/Ma/en (+/B.mm/. numbers/Cc.p/en (of/Jr.ma/en
            people/Cc.p/en (and/J/en ((now/M/en working/P.x.|pg----/en)
            (from/T/en home/Cc.s/en)) (using/Pd.or.|pg----/en (and/J/en
            (+/B.am/. video/Cc.s/en chat/Cc.s/en) (digital/Ma/en
            messages/Cc.p/en)) ((to/Mi/en stay/P.x.-i-----/en) (in/T/en
            (with/Br.ma/en touch/Cc.s/en (and/J/en friends/Cc.p/en
            family/Cc.s/en)))))))))) (as/T/en (increase/Pd.so.|f-----/en
            users/Cc.p/en (their/Mp/en time/Cc.s/en))) (on/T/en (+/B.am/.
            streaming/Cc.s/en platforms/Cc.p/en))) breton/Cp.s/en
            streamers/Cc.p/en (had/P.o?.<f-----/en (a/Md/en role/Cc.s/en)
            ((to/Mi/en play/P.x.-i-----/en) (in/T/en (ensuring/P.o.|pg----/en
            (+/B.am/. telecom/Cc.s/en operators/Cc.p/en))))) ((n’t/Mn/en
            were/P.c.<f-----/en) overwhelmed/Ca/en))
            """)
        self.assertEqual(common_pattern(edge1, edge2).to_str(), '(said/Pd.{sr}.<f----- */Cp.s (*/P.{c} */C))')

    def test_common_pattern14(self):
        edge1 = hedge(
            """
            (said/Pd.rsx.<f-----/en (think/P.sr.|f-----/en i/Ci/en (that/T/en
            (is/P.sc.|f--3s-/en (the/Md/en impact/Cc.s/en) (somewhat/M/en
            marginal/Ca/en)))) he/Ci/en (noting/Pd.r.|pg----/en (that/T/en
            (makes/P.sr.|f--3s-/en (of/Br.ma/en (’s/Bp.am/en youtube/Cm/en
            lack/Cc.s/en) (+/B.am/. 4/C#/en (+/B.am/. k/Cp.s/en
            content/Cc.s/en))) (+/J.mm/. (in/Jr.ma/en (of/Jr.ma/en
            (less/P.s.-------/en it/Ci/en) (a/Md/en factor/Cc.s/en))
            (of/Br.ma/en (the/Md/en (((most/M^/en bandwidth/Ma/en) heavy/Ma/en)
            sort/Cc.s/en)) video/Cc.s/en)) (than/Jr.ma/en (paid/Mv.<pf----/en
            services/Cc.p/en) (producing/P.o.|pg----/en (their/Mp/en (own/Ma/en
            (4/M#/en (+/B.am/. k/Cc.s/en fare/Cc.s/en)))))))))))
            """)
        edge2 = hedge(
            """
            (said/Pd.sr.<f-----/en he/Ci/en ((again/M/en (would/Mm/en
            speak/P.sx.-i-----/en)) he/Ci/en (with/T/en hastings/Cp.s/en)))
            """)
        self.assertEqual(common_pattern(edge1, edge2).to_str(), '(said/Pd.{sr}.<f----- he/Ci (*/P.{s} */Ci))')

    def test_common_pattern_var1(self):
        edge1 = hedge(
            """
            (said/Pd.rsx.<f-----/en (think/P.sr.|f-----/en (var i/Ci/en SUBJ)
            (that/T/en (is/P.sc.|f--3s-/en (the/Md/en impact/Cc.s/en)
            (somewhat/M/en marginal/Ca/en)))) he/Ci/en (noting/Pd.r.|pg----/en
            (that/T/en (makes/P.sr.|f--3s-/en (of/Br.ma/en (’s/Bp.am/en
            youtube/Cm/en lack/Cc.s/en) (+/B.am/. 4/C#/en (+/B.am/. k/Cp.s/en
            content/Cc.s/en))) (+/J.mm/. (in/Jr.ma/en (of/Jr.ma/en
            (less/P.s.-------/en it/Ci/en) (a/Md/en factor/Cc.s/en))
            (of/Br.ma/en (the/Md/en (((most/M^/en bandwidth/Ma/en) heavy/Ma/en)
            sort/Cc.s/en)) video/Cc.s/en)) (than/Jr.ma/en (paid/Mv.<pf----/en
            services/Cc.p/en) (producing/P.o.|pg----/en (their/Mp/en (own/Ma/en
            (4/M#/en (+/B.am/. k/Cc.s/en fare/Cc.s/en)))))))))))
            """)
        edge2 = hedge("""(said/Pd.sr.<f-----/en he/Ci/en ((again/M/en (would/Mm/en
                      speak/P.sx.-i-----/en)) he/Ci/en (with/T/en hastings/Cp.s/en)))""")
        self.assertEqual(common_pattern(edge1, edge2).to_str(),
                         '(said/Pd.{sr}.<f----- he/Ci (*/P.{s} (var */Ci SUBJ)))')

    def test_common_pattern_var2(self):
        edge1 = hedge('(likes/P.so (var mary/C PERSON) (of/B.ma games/C (var chess/C GAME)))')
        edge2 = hedge('(likes/P.sox john/C (of/B.ma games/C go/C) x/C)')
        self.assertEqual(common_pattern(edge1, edge2).to_str(),
                         '(likes/P.{so} (var */C PERSON) (of/B.ma games/C (var */C GAME)))')

    def test_common_pattern_var2(self):
        edge1 = hedge('(likes/P.so (var mary/C PERSON) (of/B.ma games/C (var chess/C GAME)))')
        edge2 = hedge('(likes/P.sox john/C zzz/C x/C)')
        self.assertEqual(common_pattern(edge1, edge2), None)
            
    def test_common_pattern_var3(self):
        edge1 = hedge('(likes/P.{sox} (var mary/C PERSON) (of/B.ma games/C (var chess/C GAME)) (var sometimes/C WHEN))')
        edge2 = hedge('(likes/P.so john/C (of/B.ma games/C go/C))')
        self.assertEqual(common_pattern(edge1, edge2), None)

    def test_merge_edges1(self):
        edge1 = hedge('(likes/P.{sox} */C (of/B.ma games/C */C) sometimes/C)')
        edge2 = hedge('(loves/P.{sox} */C */C sometimes/C)')
        self.assertEqual(merge_patterns(edge1, edge2),
                         hedge('((any likes/P.{sox} loves/P.{sox}) */C (any (of/B.ma games/C */C) */C) sometimes/C)'))

    def test_merge_edges2(self):
        edge1 = hedge('(likes/P.{sox} */C (of/B.ma games/C */C) sometimes/C)')
        edge2 = hedge('(loves/P.{so} */C */C)')
        self.assertEqual(merge_patterns(edge1, edge2), None)

    def test_merge_edges3(self):
        edge1 = hedge('(likes/P.{so} */C (of/B.ma games/C */R))')
        edge2 = hedge('(loves/P.{so} */C (of/B.ma games/C */C))')
        self.assertEqual(merge_patterns(edge1, edge2),
                         hedge('((any likes/P.{so} loves/P.{so}) */C (of/B.ma games/C (any */R */C)))'))

    def test_merge_edges4(self):
        edge1 = hedge('(likes/P.{so} */C (of/B.ma games/C */R))')
        edge2 = hedge('(likes/P.{so} */C (of/B.ma games/C */R))')
        self.assertEqual(merge_patterns(edge1, edge2), hedge('(likes/P.{so} */C (of/B.ma games/C */R))'))

    def test_merge_edges5(self):
        edge1 = hedge('((any likes/P.{so} prefers/P.{so}) */C */C)')
        edge2 = hedge('(loves/P.{so} */C */C)')
        self.assertEqual(merge_patterns(edge1, edge2),
                         hedge('((any likes/P.{so} prefers/P.{so} loves/P.{so}) */C */C)'))


if __name__ == '__main__':
    unittest.main()
