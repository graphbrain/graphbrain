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
        self.assertTrue(hedge('graphbrain/c').is_atom())
        self.assertTrue(hedge('graphbrain/cn.p/1').is_atom())
        self.assertFalse(hedge('(is/pd.sc graphbrain/cp.s great/c)').is_atom())

    def test_atom_parts(self):
        self.assertEqual(hedge('graphbrain/c').parts(), ['graphbrain', 'c'])
        self.assertEqual(hedge('graphbrain').parts(), ['graphbrain'])
        self.assertEqual(hedge('go/p.p.so/1').parts(), ['go', 'p.p.so', '1'])

    def test_root(self):
        self.assertEqual(hedge('graphbrain/c').root(), 'graphbrain')
        self.assertEqual(hedge('go/p.p.so/1').root(), 'go')

    def test_build_atom(self):
        self.assertEqual(build_atom('graphbrain', 'c'), hedge('graphbrain/c'))
        self.assertEqual(build_atom('go', 'p.p.so', '1'), hedge('go/p.p.so/1'))

    def test_replace_atom_part(self):
        self.assertEqual(hedge('graphbrain/c').replace_atom_part(0, 'x'),
                         hedge('x/c'))
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
                       hedge('(+/b mary/c john/c)')))
        self.assertEqual(s, '(1 2) xxx (+/b mary/c john/c)')

    def test_edges2str_roots_only(self):
        s = edges2str((hedge('(1 2)'), hedge('xxx'),
                       hedge('(+/b mary/c john/c)')),
                      roots_only=True)
        self.assertEqual(s, '(1 2) xxx (+ mary john)')

    def test_to_str(self):
        self.assertEqual(
            hedge('(is graphbrain/c great/c)').to_str(),
            '(is graphbrain/c great/c)')
        self.assertEqual(
            hedge('(src graphbrain/c (is graphbrain/c great/c))').to_str(),
            '(src graphbrain/c (is graphbrain/c great/c))')

    def test_ent2str_roots_only(self):
        self.assertEqual(
            hedge('(is graphbrain/c great/c)').to_str(roots_only=True),
            '(is graphbrain great)')
        self.assertEqual(
            hedge('(src graphbrain/c '
                  '(is graphbrain/c great/c))').to_str(roots_only=True),
            '(src graphbrain (is graphbrain great))')

    def test_label(self):
        self.assertEqual(hedge('some_thing/cn.s/.').label(), 'some thing')
        self.assertEqual(hedge('(red/m shoes/c)').label(), 'red shoes')
        self.assertEqual(hedge('(of/b capital/c germany/c)').label(),
                         'capital of germany')
        self.assertEqual(hedge('(+/b/. capital/c germany/c)').label(),
                         'capital germany')
        self.assertEqual(hedge('(of/b capital/c west/c germany/c)').label(),
                         'capital of west germany')
        self.assertEqual(hedge('(of/b capital/c '
                               '(and/b belgium/c europe/c))').label(),
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
        edge = hedge('(is/pd.sc piron/c (of/b capital/c piripiri/c))')
        self.assertTrue(edge.contains(hedge('is/pd.sc')))
        self.assertTrue(edge.contains(hedge('piron/c')))
        self.assertTrue(edge.contains(hedge('(of/b capital/c piripiri/c)')))
        self.assertFalse(edge.contains(hedge('piripiri/c')))
        self.assertFalse(edge.contains(hedge('1111/c')))

    def test_contains_deep(self):
        edge = hedge('(is/pd.sc piron/c (of/b capital/c piripiri/c))')
        self.assertTrue(edge.contains(hedge('is/pd.sc'), deep=True))
        self.assertTrue(edge.contains(hedge('piron/c'), deep=True))
        self.assertTrue(edge.contains(hedge('(of/b capital/c piripiri/c)'),
                                      deep=True))
        self.assertTrue(edge.contains(hedge('piripiri/c'), deep=True))
        self.assertFalse(edge.contains(hedge('1111/c'), deep=True))

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
        self.assertEqual(match_pattern('(a b)', '(a b)'), {})
        self.assertEqual(match_pattern('(a b)', '(a a)'), None)

    def test_match_pattern_wildcard(self):
        self.assertEqual(match_pattern('(is/pd.sc graphbrain/cp.s great/c)',
                                       '(is/pd.sc graphbrain/cp.s *x)'),
                         {'x': hedge('great/c')})
        self.assertEqual(match_pattern('(is/pd.sc graphbrain/cp.s great/c)',
                                       '(is/pd.sc graphbrain/cp.s *)'),
                         {})
        self.assertEqual(match_pattern('(was/pd.sc graphbrain /cp.s great/c)',
                                       '(is/pd.sc graphbrain/cp.s *x)'),
                         None)

    def test_match_pattern_atomic_wildcard(self):
        self.assertEqual(match_pattern('(is/pd.sc graphbrain/cp.s great/c)',
                                       '(is/pd.sc graphbrain/cp.s @prop)'),
                         {'prop': hedge('great/c')})
        self.assertEqual(match_pattern('(is/pd.sc graphbrain/cp.s great/c)',
                                       '(is/pd.sc graphbrain/cp.s @)'),
                         {})
        self.assertEqual(match_pattern('(was/pd.sc graphbrain/cp.s great/c)',
                                       '(is/pd.sc graphbrain/cp.s @prop)'),
                         None)
        self.assertEqual(match_pattern('(is/pd.sc graphbrain/cp.s '
                                       '(fairly/m great/c))',
                                       '(is/pd.sc graphbrain/cp.s @prop)'),
                         None)

    def test_match_pattern_non_atomic_wildcard(self):
        self.assertEqual(match_pattern('(is/pd.sc graphbrain/cp.s '
                                       '(fairly/m great/c))',
                                       '(is/pd.sc graphbrain/cp.s &prop)'),
                         {'prop': hedge('(fairly/m great/c)')})
        self.assertEqual(match_pattern('(is/pd.sc graphbrain/cp.s '
                                       '(fairly/m great/c))',
                                       '(is/pd.sc graphbrain/cp.s &)'),
                         {})
        self.assertEqual(match_pattern('(was/pd.sc graphbrain/cp.s '
                                       '(fairly/m great/c))',
                                       '(is/pd.sc graphbrain/cp.s &prop)'),
                         None)
        self.assertEqual(match_pattern('(is/pd.sc graphbrain/cp.s great/c)',
                                       '(is/pd.sc graphbrain/cp.s &prop)'),
                         None)

    def test_match_pattern_open_ended(self):
        self.assertEqual(match_pattern('(is/pd.sc graphbrain/cp.s great/c)',
                                       '(is/pd.sc graphbrain/cp.s *x ...)'),
                         {'x': hedge('great/c')})
        self.assertEqual(match_pattern('(is/pd.sc graphbrain/cp.s great/c)',
                                       '(is/pd.sc graphbrain/cp.s * ...)'),
                         {})
        self.assertEqual(match_pattern('(was/pd.sc graphbrain /cp.s great/c)',
                                       '(is/pd.sc graphbrain/cp.s *x ...)'),
                         None)
        self.assertEqual(match_pattern('(is/pd.sc graphbrain/cp.s great/c)',
                                       '(is/pd.sc @obj ...)'),
                         {'obj': hedge('graphbrain/cp.s')})
        self.assertEqual(match_pattern('(is/pd.sc graphbrain/cp.s great/c)',
                                       '(is/pd.sc @obj)'),
                         None)

    def test_edge_matches_pattern_simple(self):
        self.assertTrue(edge_matches_pattern(hedge('(a b)'), '(a b)'))
        self.assertFalse(edge_matches_pattern(hedge('(a b)'), '(a a)'))

    def test_edge_matches_pattern_wildcard(self):
        self.assertTrue(edge_matches_pattern(hedge('(is/pd.sc graphbrain/cp.s '
                                                   'great/c)'),
                                             '(is/pd.sc graphbrain/cp.s *)'))
        self.assertFalse(edge_matches_pattern(hedge('(was/pd.sc graphbrain'
                                                    '/cp.s great/c)'),
                                              '(is/pd.sc graphbrain/cp.s *)'))

    def test_edge_matches_pattern_atomic_wildcard(self):
        self.assertTrue(edge_matches_pattern(hedge('(is/pd.sc graphbrain/cp.s '
                                                   'great/c)'),
                                             '(is/pd.sc graphbrain/cp.s @)'))
        self.assertFalse(edge_matches_pattern(hedge('(was/pd.sc graphbrain'
                                                    '/cp.s great/c)'),
                                              '(is/pd.sc graphbrain/cp.s @)'))

        self.assertFalse(edge_matches_pattern(hedge('(is/pd.sc graphbrain/cp.s'
                                                    '(fairly/m great/c))'),
                                              '(is/pd.sc graphbrain/cp.s @)'))

    def test_edge_matches_pattern_edge_wildcard(self):
        self.assertTrue(edge_matches_pattern(hedge('(is/pd.sc graphbrain/cp.s'
                                                   ' (fairly/m great/c))'),
                                             '(is/pd.sc graphbrain/cp.s &)'))

        self.assertFalse(edge_matches_pattern(hedge('(is/pd.sc graphbrain/cp.s'
                                                    ' great/c)'),
                                              '(is/pd.sc graphbrain/cp.s &)'))

    def test_edge_matches_pattern_open_ended(self):
        self.assertTrue(edge_matches_pattern(hedge('(is/pd.sc graphbrain/cp.s '
                                                   'great/c)'),
                                             '(is/pd.sc graphbrain/cp.s '
                                             '* ...)'))

        self.assertTrue(edge_matches_pattern(hedge('(is/pd.sc graphbrain/cp.s '
                                                   'great/c extra/c)'),
                                             '(is/pd.sc graphbrain/cp.s '
                                             '* ...)'))

        self.assertFalse(edge_matches_pattern(hedge('(is/pd.sc humanity/cp.s '
                                                    'great/c extra/c)'),
                                              '(is/pd.sc graphbrain/cp.s '
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
        xc = hedge('x/c')
        self.assertEqual(hedge('x').replace_atom(x, xc).to_str(), 'x/c')
        self.assertEqual(hedge('(a b x)').replace_atom(x, xc).to_str(),
                         '(a b x/c)')
        self.assertEqual(hedge('(a b c)').replace_atom(x, xc).to_str(),
                         '(a b c)')
        self.assertEqual(hedge('(a x '
                               '(b x))').replace_atom(x, xc).to_str(),
                         '(a x/c (b x/c))')

    def test_atom_role(self):
        self.assertEqual(hedge('graphbrain/cp.s/1').role(), ['cp', 's'])
        self.assertEqual(hedge('graphbrain').role(), ['c'])

    def test_atom_type(self):
        self.assertEqual(hedge('graphbrain/cp.s/1').type(), 'cp')
        self.assertEqual(hedge('graphbrain').type(), 'c')

    def test_entity_type(self):
        self.assertEqual(hedge('(is/pd.so graphbrain/cp.s great/c)').type(),
                         'rd')
        self.assertEqual(hedge('(red/m shoes/cn.p)').type(), 'cn')
        self.assertEqual(hedge('(before/tt noon/c)').type(), 'st')
        self.assertEqual(hedge('(very/w large/m)').type(), 'm')
        self.assertEqual(hedge('((very/w large/m) shoes/cn.p)').type(), 'cn')
        self.assertEqual(hedge('(will/a be/pd.sc)').type(), 'pd')
        self.assertEqual(hedge('((will/a be/pd.sc) john/cp.s rich/c)').type(),
                         'rd')
        self.assertEqual(hedge('(play/x piano/cn.s)').type(), 'd')

    def test_connector_type(self):
        self.assertEqual(hedge('graphbrain/cp.s/1').connector_type(), 'cp')
        self.assertEqual(hedge('graphbrain').connector_type(), 'c')
        self.assertEqual(hedge('(is/pd.so graphbrain/cp.s '
                               'great/c)').connector_type(), 'pd')
        self.assertEqual(hedge('(red/m shoes/cn.p)').connector_type(), 'm')
        self.assertEqual(hedge('(before/tt noon/c)').connector_type(), 'tt')
        self.assertEqual(hedge('(very/w large/m)').connector_type(), 'w')
        self.assertEqual(hedge('((very/w large/m) '
                               'shoes/cn.p)').connector_type(), 'm')
        self.assertEqual(hedge('(will/a be/pd.sc)').connector_type(), 'a')
        self.assertEqual(hedge('((will/a be/pd.sc) john/cp.s '
                               'rich/c)').connector_type(), 'pd')
        self.assertEqual(hedge('(play/x piano/cn.s)').connector_type(), 'x')

    def test_atom_with_type(self):
        self.assertEqual(hedge('(+/b a/cn b/cp)').atom_with_type('c'),
                         hedge('a/cn'))
        self.assertEqual(hedge('(+/b a/c b/cp)').atom_with_type('cp'),
                         hedge('b/cp'))
        self.assertEqual(hedge('(+/b a/c b/cp)').atom_with_type('p'), None)
        self.assertEqual(hedge('a/cn').atom_with_type('c'), hedge('a/cn'))
        self.assertEqual(hedge('a/cn').atom_with_type('cn'), hedge('a/cn'))
        self.assertEqual(hedge('a/cn').atom_with_type('cp'), None)
        self.assertEqual(hedge('a/cn').atom_with_type('p'), None)

    def test_contains_atom_type(self):
        self.assertTrue(hedge('(+/b a/cn b/cp)').contains_atom_type('c'))
        self.assertTrue(hedge('(+/b a/c b/cp)').contains_atom_type('cp'))
        self.assertFalse(hedge('(+/b a/c b/cp)').contains_atom_type('p'))
        self.assertTrue(hedge('a/cn').contains_atom_type('c'))
        self.assertTrue(hedge('a/cn').contains_atom_type('cn'))
        self.assertFalse(hedge('a/cn').contains_atom_type('cp'))
        self.assertFalse(hedge('a/cn').contains_atom_type('p'))

    def test_predicate(self):
        self.assertEqual(hedge('graphbrain/cp.s/1').predicate(), None)
        self.assertEqual(hedge('graphbrain').predicate(), None)
        self.assertEqual(hedge('(is/pd.so graphbrain/cp.s '
                               'great/c)').predicate().to_str(), 'is/pd.so')
        self.assertEqual(hedge('(red/m shoes/cn.p)').predicate(), None)
        self.assertEqual(hedge('(before/tt noon/c)').predicate(), None)
        self.assertEqual(hedge('(very/w large/m)').predicate(), None)
        self.assertEqual(hedge('((very/w large/m) '
                               'shoes/cn.p)').predicate(), None)
        self.assertEqual(hedge('(will/a be/pd.sc)').predicate().to_str(),
                         'be/pd.sc')
        self.assertEqual(hedge('((will/a be/pd.sc) john/cp.s '
                               'rich/c)').predicate().to_str(), 'be/pd.sc')
        self.assertEqual(hedge('(play/x piano/cn.s)').predicate(), None)

    def test_rel_arg_role(self):
        edge = hedge('(is/pd.so graphbrain/cp.s great/c)')
        self.assertEqual(rel_arg_role(edge, 0), 's')
        self.assertEqual(rel_arg_role(edge, 1), 'o')
        self.assertEqual(rel_arg_role(edge, 2), None)
        edge = hedge('(is/b graphbrain/cp.s great/c)')
        self.assertEqual(rel_arg_role(edge, 0), None)

    def test_is_pattern(self):
        edge = hedge("('s/bp.am zimbabwe/m economy/cn.s)")
        self.assertFalse(edge.is_pattern())
        edge = hedge("('s/bp.am * economy/cn.s)")
        self.assertTrue(edge.is_pattern())
        edge = hedge("('s/bp.am * ...)")
        self.assertTrue(edge.is_pattern())
        edge = hedge('thing/c')
        self.assertFalse(edge.is_pattern())
        edge = hedge('&')
        self.assertTrue(edge.is_pattern())

    def test_is_full_pattern(self):
        edge = hedge("('s/bp.am zimbabwe/m economy/cn.s)")
        self.assertFalse(edge.is_full_pattern())
        edge = hedge("('s/bp.am * economy/cn.s)")
        self.assertFalse(edge.is_full_pattern())
        edge = hedge("('s/bp.am * ...)")
        self.assertFalse(edge.is_full_pattern())
        edge = hedge('thing/c')
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
        edge = hedge('s/bp.am')
        self.assertEqual(edge.argroles(), 'am')
        edge = hedge('come/pd.sx.-i----/en')
        self.assertEqual(edge.argroles(), 'sx')
        edge = hedge('come/pd')
        self.assertEqual(edge.argroles(), '')
        edge = hedge('red/m')
        self.assertEqual(edge.argroles(), '')
        edge = hedge('berlin/cp.s/de')
        self.assertEqual(edge.argroles(), '')

    def test_argroles_edge(self):
        edge = hedge('(is/av.|f--3s/en influenced/pd.xpa.<pf---/en)')
        self.assertEqual(edge.argroles(), 'xpa')
        edge = hedge('(is/av.|f--3s/en influenced/pd)')
        self.assertEqual(edge.argroles(), '')
        edge = hedge('(looks/pd.sc.|f--3s she/ci (very/m beautiful/ca))')
        self.assertEqual(edge.argroles(), '')

    def test_edges_with_argrole(self):
        edge_str = ("((have/av.|f----/en (been/av.<pf---/en "
                    "tracking/pd.sox.|pg---/en)) (from/br.ma/en "
                    "satellites/cc.p/en (and/b+/en nasa/cp.s/en "
                    "(other/ma/en agencies/cc.p/en))) "
                    "(+/b.aam/. sea/cc.s/en ice/cc.s/en changes/cc.p/en) "
                    "(since/tt/en 1979/c#/en))")
        edge = hedge(edge_str)

        subj = hedge(("(from/br.ma/en satellites/cc.p/en "
                      "(and/b+/en nasa/cp.s/en (other/ma/en "
                      "agencies/cc.p/en)))"))
        obj = hedge("(+/b.aam/. sea/cc.s/en ice/cc.s/en changes/cc.p/en)")
        spec = hedge("(since/tt/en 1979/c#/en)")

        self.assertEqual(edge.edges_with_argrole('s'), [subj])
        self.assertEqual(edge.edges_with_argrole('o'), [obj])
        self.assertEqual(edge.edges_with_argrole('x'), [spec])
        self.assertEqual(edge.edges_with_argrole('p'), [])

    def test_edges_with_argrole_no_roles(self):
        edge_str = ("((have/av.|f----/en (been/av.<pf---/en "
                    "tracking/pd)) (from/br.ma/en "
                    "satellites/cc.p/en (and/b+/en nasa/cp.s/en "
                    "(other/ma/en agencies/cc.p/en))) "
                    "(+/b.aam/. sea/cc.s/en ice/cc.s/en changes/cc.p/en) "
                    "(since/tt/en 1979/c#/en))")
        edge = hedge(edge_str)

        self.assertEqual(edge.edges_with_argrole('s'), [])
        self.assertEqual(edge.edges_with_argrole('o'), [])
        self.assertEqual(edge.edges_with_argrole('x'), [])
        self.assertEqual(edge.edges_with_argrole('p'), [])

    def test_edges_with_argrole_atom(self):
        edge = hedge('tracking/pd.sox.|pg---/en')

        self.assertEqual(edge.edges_with_argrole('s'), [])
        self.assertEqual(edge.edges_with_argrole('o'), [])
        self.assertEqual(edge.edges_with_argrole('x'), [])
        self.assertEqual(edge.edges_with_argrole('p'), [])

    def test_main_concepts(self):
        concept = hedge("('s/bp.am zimbabwe/mp economy/cn.s)")
        self.assertEqual(concept.main_concepts(), [hedge('economy/cn.s')])
        concept = hedge("('s/bp zimbabwe/mp economy/cn.s)")
        self.assertEqual(concept.main_concepts(), [])
        concept = hedge('(+/b.am?/. hiv/ca kit/cn.s (testing/m self/cn.s))')
        self.assertEqual(concept.main_concepts(), [hedge('kit/cn.s')])
        concept = hedge('(+/b.?a?/. hiv/ca kit/cn.s (testing/m self/cn.s))')
        self.assertEqual(concept.main_concepts(), [])
        concept = hedge('(a/m thing/c)')
        self.assertEqual(concept.main_concepts(), [])
        concept = hedge('thing/c')
        self.assertEqual(concept.main_concepts(), [])


if __name__ == '__main__':
    unittest.main()
