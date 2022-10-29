import unittest

from graphbrain.hyperedge import (hedge,
                                  build_atom,
                                  str2atom,
                                  split_edge_str,
                                  edges2str)


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
        self.assertEqual(hedge('(VAR/C)').to_str(), '(VAR/C)')
        self.assertEqual(hedge('((is my) (brain/1) (super great/1))').to_str(),
                         '((is my) (brain/1) (super great/1))')

    def test_atom(self):
        self.assertTrue(hedge('a').atom)
        self.assertTrue(hedge('graphbrain/C').atom)
        self.assertTrue(hedge('graphbrain/Cn.p/1').atom)
        self.assertTrue(hedge('(X/C)').atom)
        self.assertFalse(hedge('(is/Pd.sc graphbrain/Cp.s great/C)').atom)

    def test_atom_parts(self):
        self.assertEqual(hedge('graphbrain/C').parts(), ['graphbrain', 'C'])
        self.assertEqual(hedge('graphbrain').parts(), ['graphbrain'])
        self.assertEqual(hedge('go/P.so/1').parts(), ['go', 'P.so', '1'])
        self.assertEqual(hedge('(X/P.so/1)').parts(), ['X', 'P.so', '1'])

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
        self.assertEqual(hedge('(XXX/1/yyy)').replace_atom_part(1, '77'),
                         hedge('(XXX/77/yyy)'))

    def test_str2atom(self):
        self.assertEqual(str2atom('abc'), 'abc')
        self.assertEqual(str2atom('abc%'), 'abc%25')
        self.assertEqual(str2atom('/abc'), '%2fabc')
        self.assertEqual(str2atom('a bc'), 'a%20bc')
        self.assertEqual(str2atom('ab(c'), 'ab%28c')
        self.assertEqual(str2atom('abc)'), 'abc%29')
        self.assertEqual(str2atom('.abc'), '%2eabc')
        self.assertEqual(str2atom('a*bc'), 'a%2abc')
        self.assertEqual(str2atom('ab&c'), 'ab%26c')
        self.assertEqual(str2atom('abc@'), 'abc%40')
        self.assertEqual(
            str2atom('graph brain/(1).'), 'graph%20brain%2f%281%29%2e')

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
        self.assertEqual(hedge('graph%20brain%2f%281%29%2e/Cn.s/.').label(),
                         'graph brain/(1).')
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

    def test_connector_atom(self):
        edge = hedge('(is/P.sc graphbrain/1 great/1)')
        self.assertEqual(edge.connector_atom(),
                         hedge('is/P.sc'))
        edge = hedge('((not/M is/P.sc) graphbrain/1 great/1)')
        self.assertEqual(edge.connector_atom(),
                         hedge('is/P.sc'))
        edge = hedge('((maybe/M (not/M is/P.sc)) graphbrain/1 great/1)')
        self.assertEqual(edge.connector_atom(),
                         hedge('is/P.sc'))
        edge = hedge('(((and/J not/M nope/M) is/P.sc) graphbrain/1 great/1)')
        self.assertEqual(edge.connector_atom(),
                         hedge('is/P.sc'))

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
        self.assertEqual(hedge('(is (X/C) great/1)').atoms(),
                         {hedge('is'), hedge('(X/C)'),
                          hedge('great/1')})

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
        edge = hedge('(the/Md (of/Br (X/C) (the/Md city/Cs)))')
        self.assertEqual(edge.all_atoms(),
                         [hedge('the/Md'), hedge('of/Br'), hedge('(X/C)'),
                          hedge('the/Md'), hedge('city/Cs')])

    def test_size(self):
        self.assertEqual(hedge('graphbrain/1').size(), 1)
        self.assertEqual(hedge('(X/C)').size(), 1)
        self.assertEqual(hedge('(is graphbrain/1 great/1)').size(), 3)
        self.assertEqual(hedge('(is graphbrain/1 (super great/1))').size(), 4)

    def test_depth(self):
        self.assertEqual(hedge('graphbrain/1').depth(), 0)
        self.assertEqual(hedge('(is graphbrain/1 great/1)').depth(), 1)
        self.assertEqual(hedge('(is graphbrain/1 (super great/1))').depth(), 2)
        self.assertEqual(hedge('(is graphbrain/1 (super (X/C)))').depth(), 2)

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

    def test_contains_pares_atom(self):
        edge = hedge('(is/Pd.sc (X/C) (of/B capital/C piripiri/C))')
        self.assertTrue(edge.contains(hedge('is/Pd.sc')))
        self.assertTrue(edge.contains(hedge('(X/C)')))
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

    def test_contains_deep_pares_atom(self):
        edge = hedge('(is/Pd.sc piron/C (of/B capital/C (XYZ)))')
        self.assertTrue(edge.contains(hedge('is/Pd.sc'), deep=True))
        self.assertTrue(edge.contains(hedge('piron/C'), deep=True))
        self.assertTrue(edge.contains(hedge('(of/B capital/C (XYZ))'),
                                      deep=True))
        self.assertTrue(edge.contains(hedge('(XYZ)'), deep=True))
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
        self.assertEqual(hedge('(is graphbrain/1 (X/C))').subedges(),
                         {hedge('is'), hedge('graphbrain/1'), hedge('(X/C)'),
                          hedge('(is graphbrain/1 (X/C))')})

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

    def test_replace_atom_unique(self):
        edge = hedge('(a/P x/C x/C)')
        x1 = edge[1]
        x2 = edge[2]
        y = hedge('y/C')
        self.assertEqual(
            edge.replace_atom(x1, y, unique=True).to_str(),
            '(a/P y/C x/C)')
        self.assertEqual(
            edge.replace_atom(x2, y, unique=True).to_str(),
            '(a/P x/C y/C)')
        self.assertEqual(
            edge.replace_atom(hedge('x/C'), y, unique=True).to_str(),
            '(a/P x/C x/C)')

    def test_atom_role(self):
        self.assertEqual(hedge('graphbrain/Cp.s/1').role(), ['Cp', 's'])

    def test_atom_role_implied_conjunction(self):
        self.assertEqual(hedge('and').role(), ['J'])

    def test_atom_simplify_atom(self):
        self.assertEqual(hedge('graphbrain/Cp.s/1').simplify(),
                         hedge('graphbrain/C/1'))
        self.assertEqual(hedge('graphbrain').simplify(),
                         hedge('graphbrain'))
        self.assertEqual(hedge('say/Pd.sr.|f----/en').simplify(),
                         hedge('say/P/en'))
        self.assertEqual(hedge('say/Pd.sr.|f----/en').simplify(subtypes=True),
                         hedge('say/Pd/en'))
        self.assertEqual(hedge('say/Pd.sr.|f----/en').simplify(argroles=True),
                         hedge('say/P.sr/en'))
        self.assertEqual(
            hedge('say/Pd.sr.|f----/en').simplify(namespaces=False),
            hedge('say/P'))
        self.assertEqual(
            hedge('say/Pd.sr.|f----/en').simplify(subtypes=True,
                                                  namespaces=False),
            hedge('say/Pd'))

    def test_atom_simplify_edge(self):
        edge = hedge('is/Pd.sc.|f----/en mary/Cp.s/en nice/Ca/en')
        self.assertEqual(
            edge.simplify(),
            hedge('is/P/en mary/C/en nice/C/en'))
        self.assertEqual(
            edge.simplify(subtypes=True),
            hedge('is/Pd/en mary/Cp/en nice/Ca/en'))
        self.assertEqual(
            edge.simplify(argroles=True),
            hedge('is/P.sc/en mary/C/en nice/C/en'))
        self.assertEqual(
            edge.simplify(argroles=True, namespaces=False),
            hedge('is/P.sc mary/C nice/C'))

    def test_atom_type(self):
        self.assertEqual(hedge('graphbrain/Cp.s/1').type(), 'Cp')

    def test_atom_mtype(self):
        self.assertEqual(hedge('graphbrain/Cp.s/1').mtype(), 'C')

    def test_atom_type_implied_conjunction(self):
        self.assertEqual(hedge('and').type(), 'J')

    def test_non_atom_type(self):
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

    def test_non_atom_mtype(self):
        self.assertEqual(hedge('(is/Pd.so graphbrain/Cp.s great/C)').type(),
                         'Rd')
        self.assertEqual(hedge('(red/M shoes/Cc.p)').mtype(), 'C')
        self.assertEqual(hedge('(before/Tt noon/C)').mtype(), 'S')
        self.assertEqual(hedge('(very/M large/M)').mtype(), 'M')
        self.assertEqual(hedge('((very/M large/M) shoes/Cc.p)').mtype(), 'C')
        self.assertEqual(hedge('(will/M be/Pd.sc)').mtype(), 'P')
        self.assertEqual(hedge('((will/M be/Pd.sc) john/Cp.s rich/C)').mtype(),
                         'R')
        self.assertEqual(hedge('(play/T piano/Cc.s)').mtype(), 'S')
        self.assertEqual(hedge('(and/J meat/Cc.s potatoes/Cc.p)').mtype(), 'C')
        self.assertEqual(
            hedge('(and/J (is/Pd.so graphbrain/Cp.s great/C))').mtype(), 'R')

    def test_connector_type(self):
        self.assertEqual(hedge('graphbrain/Cp.s/1').connector_type(), None)
        self.assertEqual(hedge('graphbrain').connector_type(), None)
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

    def test_connector_mtype(self):
        self.assertEqual(hedge('graphbrain/Cp.s/1').connector_mtype(), None)
        self.assertEqual(hedge('graphbrain').connector_mtype(), None)
        self.assertEqual(hedge('(is/Pd.so graphbrain/Cp.s '
                               'great/C)').connector_mtype(), 'P')
        self.assertEqual(hedge('(red/M shoes/Cn.p)').connector_mtype(), 'M')
        self.assertEqual(hedge('(before/Tt noon/C)').connector_mtype(), 'T')
        self.assertEqual(hedge('(very/M large/M)').connector_mtype(), 'M')
        self.assertEqual(hedge('((very/M large/M) '
                               'shoes/Cn.p)').connector_mtype(), 'M')
        self.assertEqual(hedge('(will/M be/Pd.sc)').connector_mtype(), 'M')
        self.assertEqual(hedge('((will/M be/Pd.sc) john/Cp.s '
                               'rich/C)').connector_mtype(), 'P')
        self.assertEqual(hedge('(play/T piano/Cn.s)').connector_mtype(), 'T')

    def test_t(self):
        self.assertEqual(hedge('graphbrain/Cp.s/1').t, 'Cp')
        self.assertEqual(hedge('(is/Pd.so graphbrain/Cp.s great/C)').t, 'Rd')
        self.assertEqual(hedge('(very/M large/M)').t, 'M')

    def test_mt(self):
        self.assertEqual(hedge('graphbrain/Cp.s/1').mt, 'C')
        self.assertEqual(hedge('(is/Pd.so graphbrain/Cp.s great/C)').mt, 'R')
        self.assertEqual(hedge('(very/M large/M)').mt, 'M')

    def test_ct(self):
        self.assertEqual(hedge('graphbrain/Cp.s/1').ct, None)
        self.assertEqual(hedge('(is/Pd.so graphbrain/Cp.s '
                               'great/C)').ct, 'Pd')
        self.assertEqual(hedge('(red/M shoes/Cn.p)').ct, 'M')

    def test_cmt(self):
        self.assertEqual(hedge('graphbrain/Cp.s/1').cmt, None)
        self.assertEqual(hedge('(is/Pd.so graphbrain/Cp.s '
                               'great/C)').cmt, 'P')
        self.assertEqual(hedge('(red/M shoes/Cn.p)').cmt, 'M')

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

    def test_argroles_connector_atom(self):
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

    def test_argroles_connector_edge(self):
        edge = hedge('(is/Mv.|f--3s/en influenced/Pd.xpa.<pf---/en)')
        self.assertEqual(edge.argroles(), 'xpa')
        edge = hedge('(is/Mv.|f--3s/en influenced/Pd)')
        self.assertEqual(edge.argroles(), '')

    def test_argroles_edge(self):
        edge = hedge('((not/M is/P.sc) bob/C sad/C)')
        self.assertEqual(edge.argroles(), 'sc')
        edge = hedge('(of/B.ma city/C berlin/C)')
        self.assertEqual(edge.argroles(), 'ma')
        edge = hedge('(of/B city/C berlin/C)')
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
        edge = hedge('((m/M s/Bp.am) x/C y/C)')
        self.assertEqual(edge.replace_argroles('ma').to_str(),
                         '((m/M s/Bp.ma) x/C y/C)')
        edge = hedge('(come/Pd.sx.-i----/en you/C here/C)')
        self.assertEqual(edge.replace_argroles('scx').to_str(),
                         '(come/Pd.scx.-i----/en you/C here/C)')
        edge = hedge('(come/Pd/en you/C here/C)')
        self.assertEqual(edge.replace_argroles('scx').to_str(),
                         '(come/Pd.scx/en you/C here/C)')
        edge = hedge('((do/M come/Pd/en) you/C here/C)')
        self.assertEqual(edge.replace_argroles('scx').to_str(),
                         '((do/M come/Pd.scx/en) you/C here/C)')
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
        edge = hedge('((m/M s/Bp.am) x/C y/C)')
        self.assertEqual(
            edge.insert_argrole('m', 0).to_str(), '((m/M s/Bp.mam) x/C y/C)')
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
        edge = hedge('((do/M come/Pd.sx.-i----/en) you/C here/C)')
        self.assertEqual(edge.insert_argrole('x', 2).to_str(),
                         '((do/M come/Pd.sxx.-i----/en) you/C here/C)')

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
        edge = hedge('((not/M is/Pd.sc/en) sky/C blue/C)')
        self.assertEqual(
            edge.insert_edge_with_argrole(hedge('today/C'), 'x', 1),
            hedge('((not/M is/Pd.sxc/en) sky/C today/C blue/C)'))
        edge = hedge('((m/M b/B.am) x/C y/C)')
        self.assertEqual(
            edge.insert_edge_with_argrole(hedge('z/C'), 'a', 2),
            hedge('((m/M b/B.ama) x/C y/C z/C)'))

    def test_replace_argroles_var(self):
        edge = hedge('((var s/Bp.am V) x/C y/C)')
        self.assertEqual(edge.replace_argroles('ma').to_str(),
                         '((var s/Bp.ma V) x/C y/C)')
        edge = hedge('((var (m/M s/Bp.am) V) x/C y/C)')
        self.assertEqual(edge.replace_argroles('ma').to_str(),
                         '((var (m/M s/Bp.ma) V) x/C y/C)')
        edge = hedge('((var come/Pd.sx.-i----/en V) you/C here/C)')
        self.assertEqual(edge.replace_argroles('scx').to_str(),
                         '((var come/Pd.scx.-i----/en V) you/C here/C)')
        edge = hedge('((var come/Pd/en V) you/C here/C)')
        self.assertEqual(edge.replace_argroles('scx').to_str(),
                         '((var come/Pd.scx/en V) you/C here/C)')
        edge = hedge('((var (do/M come/Pd/en) V) you/C here/C)')
        self.assertEqual(edge.replace_argroles('scx').to_str(),
                         '((var (do/M come/Pd.scx/en) V) you/C here/C)')
        edge = hedge('((var come V) you/C here/C)')
        self.assertEqual(edge.replace_argroles('scx').to_str(),
                         '((var come V) you/C here/C)')

    def test_insert_argrole_var(self):
        edge = hedge('((var s/Bp.am V) x/C y/C)')
        self.assertEqual(
            edge.insert_argrole('m', 0).to_str(), '((var s/Bp.mam V) x/C y/C)')
        edge = hedge('((var come/Pd.sx.-i----/en V) you/C here/C)')
        self.assertEqual(edge.insert_argrole('x', 1).to_str(),
                         '((var come/Pd.sxx.-i----/en V) you/C here/C)')
        edge = hedge('((var come/Pd/en V) you/C here/C)')
        self.assertEqual(edge.insert_argrole('s', 100).to_str(),
                         '((var come/Pd.s/en V) you/C here/C)')
        edge = hedge('((var (do/M come/Pd.sx.-i----/en) V) you/C here/C)')
        self.assertEqual(edge.insert_argrole('x', 2).to_str(),
                         '((var (do/M come/Pd.sxx.-i----/en) V) you/C here/C)')

    def test_insert_edge_with_var(self):
        edge = hedge('((var is/Pd.sc/en V) sky/C blue/C)')
        self.assertEqual(
            edge.insert_edge_with_argrole(hedge('today/C'), 'x', 0),
            hedge('((var is/Pd.xsc/en V) today/C sky/C blue/C)'))
        edge = hedge('((var (m/M b/B.am) V) x/C y/C)')
        self.assertEqual(
            edge.insert_edge_with_argrole(hedge('z/C'), 'a', 2),
            hedge('((var (m/M b/B.ama) V) x/C y/C z/C)'))

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

    def test_check_correctness_ok(self):
        edge = hedge('(red/M shoes/C)')
        output = edge.check_correctness()
        self.assertEqual(output, {})

        edge = hedge('(+/B john/C smith/C)')
        output = edge.check_correctness()
        self.assertEqual(output, {})

        edge = hedge('(in/T 1976/C)')
        output = edge.check_correctness()
        self.assertEqual(output, {})

        edge = hedge('(happened/P it/C before/C (in/T 1976/C))')
        output = edge.check_correctness()
        self.assertEqual(output, {})

        edge = hedge('(and/J red/C green/C blue/C)')
        output = edge.check_correctness()
        self.assertEqual(output, {})

    def test_check_correctness_wrong(self):
        edge = hedge('x/G')
        output = edge.check_correctness()
        self.assertTrue(edge in output)

        edge = hedge('(of/C capital/C mars/C)')
        output = edge.check_correctness()
        self.assertTrue(edge in output)

        edge = hedge('(+/B john/C smith/C iii/C)')
        output = edge.check_correctness()
        self.assertTrue(edge in output)

        edge = hedge('(of/B capital/C red/M)')
        output = edge.check_correctness()
        self.assertTrue(edge in output)

        edge = hedge('(in/T 1976/C 1977/C)')
        output = edge.check_correctness()
        self.assertTrue(edge in output)

        edge = hedge('(in/T red/M)')
        output = edge.check_correctness()
        self.assertTrue(edge in output)

        edge = hedge('(is/P red/M)')
        output = edge.check_correctness()
        self.assertTrue(edge in output)

        edge = hedge('(and/J one/C)')
        output = edge.check_correctness()
        self.assertTrue(edge in output)

    def test_check_correctness_wrong_deep(self):
        edge = hedge('(:/J x/C x/G)')
        output = edge.check_correctness()
        self.assertTrue(hedge('x/G') in output)

        edge = hedge('(:/J x/C (of/C capital/C mars/C))')
        output = edge.check_correctness()
        self.assertTrue(hedge('(of/C capital/C mars/C)') in output)

        edge = hedge('(:/J x/C (+/B john/C smith/C iii/C))')
        output = edge.check_correctness()
        self.assertTrue(hedge('(+/B john/C smith/C iii/C)') in output)

        edge = hedge('(:/J x/C (of/B capital/C red/M))')
        output = edge.check_correctness()
        self.assertTrue(hedge('(of/B capital/C red/M)') in output)

        edge = hedge('(:/J x/C (in/T 1976/C 1977/C))')
        output = edge.check_correctness()
        self.assertTrue(hedge('(in/T 1976/C 1977/C)') in output)

        edge = hedge('(:/J x/C (in/T red/M))')
        output = edge.check_correctness()
        self.assertTrue(hedge('(in/T red/M)') in output)

        edge = hedge('(:/J x/C (is/P red/M))')
        output = edge.check_correctness()
        self.assertTrue(hedge('(is/P red/M)') in output)

        edge = hedge('(:/J x/C (and/J one/C))')
        output = edge.check_correctness()
        self.assertTrue(hedge('(and/J one/C)') in output)

    def test_normalized_1(self):
        edge =  hedge('(plays/Pd.os chess/C mary/C)')
        self.assertEqual(
            edge.normalized(),
            hedge('(plays/Pd.so mary/C chess/C)'))

    def test_normalized_2(self):
        edge =  hedge('(plays/Pd chess/C mary/C)')
        self.assertEqual(
            edge.normalized(),
            hedge('(plays/Pd chess/C mary/C)'))

    def test_normalized_3(self):
        edge =  hedge('(plays/Pd.os (of/B.am chess/C games/C) mary/C)')
        self.assertEqual(
            edge.normalized(),
            hedge('(plays/Pd.so mary/C (of/B.ma games/C chess/C))'))

    def test_normalized_4(self):
        edge =  hedge('(plays/Pd.os.xxx/en chess/C mary/C)')
        self.assertEqual(
            edge.normalized(),
            hedge('(plays/Pd.so.xxx/en mary/C chess/C)'))

    def test_normalized_5(self):
        edge =  hedge('plays/Pd.os.xxx/en')
        self.assertEqual(
            edge.normalized(),
            hedge('plays/Pd.so.xxx/en'))

    def test_normalized_6(self):
        edge =  hedge('of/Br.am/en')
        self.assertEqual(
            edge.normalized(),
            hedge('of/Br.ma/en'))

    def test_normalized_7(self):
        edge =  hedge('plays/Pd.{os}.xxx/en')
        self.assertEqual(
            edge.normalized(),
            hedge('plays/Pd.{so}.xxx/en'))

if __name__ == '__main__':
    unittest.main()
