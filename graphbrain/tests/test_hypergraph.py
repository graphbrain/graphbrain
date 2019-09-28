import unittest
from graphbrain import *


class TestHypergraph(unittest.TestCase):
    def setUp(self):
        self.hg = hgraph('test.hg')

    def tearDown(self):
        self.hg.close()

    def test_close(self):
        self.hg.close()

    def test_name(self):
        self.assertEqual(self.hg.name(), 'test.hg')

    def test_destroy(self):
        self.hg.destroy()
        self.hg.add(hedge('(src graphbrain/1 (size graphbrain/1 7))'))
        self.assertTrue(self.hg.exists(hedge('(src graphbrain/1 '
                                             '(size graphbrain/1 7))')))
        self.hg.destroy()
        self.assertFalse(self.hg.exists(hedge('(src graphbrain/1 '
                                              '(size graphbrain/1 7))')))

    def test_all(self):
        self.hg.destroy()
        self.hg.add('(size graphbrain/1 7)')
        self.hg.add('(is graphbrain/1 great/1)')
        self.hg.add('(src mary/1 (is graphbrain/1 great/1))')

        labels = set([edge.to_str() for edge in self.hg.all()])
        self.assertEqual(labels,
                         {'size', 'graphbrain/1', '7', 'is', 'great/1', 'src',
                          'mary/1', '(size graphbrain/1 7)',
                          '(is graphbrain/1 great/1)',
                          '(src mary/1 (is graphbrain/1 great/1))'})
        self.hg.destroy()
        labels = set(self.hg.all())
        self.assertEqual(labels, set())

    def test_atoms(self):
        self.hg.destroy()
        self.hg.add('(size graphbrain/1 7)')
        self.hg.add('(is graphbrain/1 great/1)')
        self.hg.add('(src mary/1 (is graphbrain/1 great/1))')

        labels = set([edge.to_str() for edge in self.hg.all_atoms()])
        self.assertEqual(labels,
                         {'size', 'graphbrain/1', '7', 'is', 'great/1', 'src',
                          'mary/1'})
        self.hg.destroy()
        labels = set(self.hg.all())
        self.assertEqual(labels, set())

    def test_edges(self):
        self.hg.destroy()
        self.hg.add('(size graphbrain/1 7)')
        self.hg.add('(is graphbrain/1 great/1)')
        self.hg.add('(src mary/1 (is graphbrain/1 great/1))')

        labels = set([edge.to_str() for edge in self.hg.all_non_atoms()])
        self.assertEqual(labels,
                         {'(size graphbrain/1 7)', '(is graphbrain/1 great/1)',
                          '(src mary/1 (is graphbrain/1 great/1))'})
        self.hg.destroy()
        labels = set(self.hg.all())
        self.assertEqual(labels, set())

    def test_all_attributes(self):
        self.hg.destroy()
        self.hg.add('(size graphbrain/1 7)')
        self.hg.add('(is graphbrain/1 great/1)')
        self.hg.add('(src mary/1 (is graphbrain/1 great/1))')

        labels = set(['%s %s' % (t[0].to_str(), t[1]['d'])
                      for t in self.hg.all_attributes()])
        self.assertEqual(labels, {'size 1', 'graphbrain/1 2', '7 1', 'is 1',
                                  'great/1 1', 'src 1', 'mary/1 1',
                                  '(size graphbrain/1 7) 0',
                                  '(is graphbrain/1 great/1) 1',
                                  '(src mary/1 (is graphbrain/1 great/1)) 0'})
        self.hg.destroy()
        labels = set(self.hg.all_attributes())
        self.assertEqual(labels, set())

    # atom_count(), edge_count(), primary_atom_count(), primary_edge_count()
    def test_counters1(self):
        self.hg.destroy()
        self.hg.add('(is graphbrain/1 great/1)')
        self.assertEqual(self.hg.atom_count(), 3)
        self.assertEqual(self.hg.edge_count(), 4)
        self.assertEqual(self.hg.primary_atom_count(), 0)
        self.assertEqual(self.hg.primary_edge_count(), 1)

    # atom_count(), edge_count(), primary_atom_count(), primary_edge_count()
    def test_counters2(self):
        self.hg.destroy()
        self.hg.add('(says mary/c (is graphbrain/c great/c))')
        self.assertEqual(self.hg.atom_count(), 5)
        self.assertEqual(self.hg.edge_count(), 7)
        self.assertEqual(self.hg.primary_atom_count(), 0)
        self.assertEqual(self.hg.primary_edge_count(), 1)
        self.hg.remove(hedge('(is graphbrain/c great/c)'))
        self.assertEqual(self.hg.atom_count(), 5)
        self.assertEqual(self.hg.edge_count(), 6)
        self.hg.remove(hedge('(says mary/c (is graphbrain/c great/c))'))
        self.assertEqual(self.hg.atom_count(), 5)
        self.assertEqual(self.hg.primary_atom_count(), 0)
        self.assertEqual(self.hg.edge_count(), 5)
        self.assertEqual(self.hg.primary_edge_count(), 0)

    # atom_count(), edge_count(), primary_atom_count(), primary_edge_count()
    def test_counters3_non_deep_removal(self):
        self.hg.destroy()
        self.hg.add('(says mary/c (is graphbrain/c great/c))')
        self.hg.remove(hedge('(says mary/c (is graphbrain/c great/c))'),
                       deep=False)
        self.assertEqual(self.hg.atom_count(), 5)
        self.assertEqual(self.hg.primary_atom_count(), 0)
        self.assertEqual(self.hg.edge_count(), 6)
        self.assertEqual(self.hg.primary_edge_count(), 0)

    # exists, add, remove
    def test_ops1(self):
        self.hg.destroy()
        self.hg.add('(is/pd graphbrain/cp great/c)')
        self.assertTrue(self.hg.exists(hedge('(is/pd graphbrain/cp great/c)')))
        self.hg.remove(hedge('(is/pd graphbrain/cp great/c)'))
        self.assertFalse(
            self.hg.exists(hedge('(is/pd graphbrain/cp great/c)')))

    # exists, add, remove
    def test_ops_2(self):
        self.hg.destroy()
        self.hg.add('(src graphbrain/1 (size graphbrain/1 7))')
        self.assertTrue(self.hg.exists(hedge('(src graphbrain/1 '
                                             '(size graphbrain/1 7))')))
        self.hg.remove(hedge('(src graphbrain/1 (size graphbrain/1 7))'))
        self.assertFalse(self.hg.exists(hedge('(src graphbrain/1 '
                                              '(size graphbrain/1 7))')))

    def test_search(self):
        self.hg.destroy()
        self.hg.add('(is/pd graphbrain/cp great/c)')
        self.hg.add('(says/pd mary/cp)')
        self.hg.add('(says/pd mary/cp (is/pd graphbrain/cp great/c))')
        self.hg.add('(says/pd mary/cp (is/pd graphbrain/cp great/c) extra/c)')
        self.assertEqual(list(self.hg.search('(* graphbrain/cp *)')),
                         [hedge('(is/pd graphbrain/cp great/c)')])
        self.assertEqual(list(self.hg.search('(is/pd graphbrain/cp *)')),
                         [hedge('(is/pd graphbrain/cp great/c)')])
        self.assertEqual(list(self.hg.search('(x * *)')), [])
        self.assertEqual(
            list(
                self.hg.search('(says/pd * '
                               '(is/pd graphbrain/cp great/c))')),
            [hedge('(says/pd mary/cp (is/pd graphbrain/cp great/c))')])
        self.assertEqual(
            list(
                self.hg.search('(says/pd * (is/pd * *))')),
            [hedge('(says/pd mary/cp (is/pd graphbrain/cp great/c))')])

    def test_search_open_ended(self):
        self.hg.destroy()
        self.hg.add('(is/pd graphbrain/cp great/c)')
        self.hg.add('(says/pd mary/cp)')
        self.hg.add('(says/pd mary/cp (is/pd graphbrain/cp great/c))')
        self.hg.add('(says/pd mary/cp (is/pd graphbrain/cp great/c) extra/c)')
        self.assertEqual(list(self.hg.search('(* graphbrain/cp * ...)')),
                         [hedge('(is/pd graphbrain/cp great/c)')])
        self.assertEqual(
            list(self.hg.search('(is/pd graphbrain/cp * ...)')),
            [hedge('(is/pd graphbrain/cp great/c)')])
        self.assertEqual(list(self.hg.search('(x * * ...)')), [])

        self.assertEqual(
            list(
                self.hg.search(
                    '(says/pd * (is/pd graphbrain/cp great/c) ...)')),
            [hedge('(says/pd mary/cp (is/pd graphbrain/cp great/c))'),
             hedge('(says/pd mary/cp (is/pd graphbrain/cp great/c) extra/c)')])

    def test_search_star(self):
        self.hg.destroy()
        self.hg.add('(is/pd graphbrain/cp great/c)')
        self.hg.add('(says/pd mary/cp)')
        self.hg.add('(says/pd mary/cp (is/pd graphbrain/cp great/c))')
        self.hg.add('(says/pd mary/cp (is/pd graphbrain/cp great/c) extra/c)')
        self.assertEqual(set(self.hg.search('*')),
                         {hedge('(is/pd graphbrain/cp great/c)'),
                          hedge('(says/pd mary/cp)'),
                          hedge('(says/pd mary/cp '
                                '(is/pd graphbrain/cp great/c))'),
                          hedge('(says/pd mary/cp '
                                '(is/pd graphbrain/cp great/c) extra/c)'),
                          hedge('extra/c'), hedge('graphbrain/cp'),
                          hedge('great/c'), hedge('is/pd'), hedge('mary/cp'),
                          hedge('says/pd')})

    def test_search_at(self):
        self.hg.destroy()
        self.hg.add('(is/pd graphbrain/cp great/c)')
        self.hg.add('(says/pd mary/cp)')
        self.hg.add('(says/pd mary/cp (is/pd graphbrain/cp great/c))')
        self.hg.add('(says/pd mary/cp (is/pd graphbrain/cp great/c) extra/c)')
        self.assertEqual(list(self.hg.search('@')),
                         [hedge('extra/c'), hedge('graphbrain/cp'),
                          hedge('great/c'), hedge('is/pd'), hedge('mary/cp'),
                          hedge('says/pd')])

    def test_search_amp(self):
        self.hg.destroy()
        self.hg.add('(is/pd graphbrain/cp great/c)')
        self.hg.add('(says/pd mary/cp)')
        self.hg.add('(says/pd mary/cp (is/pd graphbrain/cp great/c))')
        self.hg.add('(says/pd mary/cp (is/pd graphbrain/cp great/c) extra/c)')
        self.assertEqual(set(self.hg.search('&')),
                         {hedge('(is/pd graphbrain/cp great/c)'),
                          hedge('(says/pd mary/cp)'),
                          hedge('(says/pd mary/cp '
                                '(is/pd graphbrain/cp great/c))'),
                          hedge('(says/pd mary/cp '
                                '(is/pd graphbrain/cp great/c) extra/c)')})

    def test_search_non_atomic_pred(self):
        self.hg.destroy()
        edge = hedge('((is/a playing/pd.so ) mary/cp.s '
                     '(a/md ((very/w old/ma) violin/cn.s)))')
        self.hg.add(edge)
        self.assertEqual(list(self.hg.search('((is/a playing/pd.so) ...)')),
                         [edge])
        self.assertEqual(list(self.hg.search('((is/a playing/pd.so) * *)')),
                         [edge])
        self.assertEqual(list(self.hg.search('((is/a playing/pd.so) @ &)')),
                         [edge])
        self.assertEqual(list(self.hg.search('((is/a playing/pd.so) @ @)')),
                         [])
        self.assertEqual(list(self.hg.search('((is/a playing/pd.so) & &)')),
                         [])
        self.assertEqual(list(self.hg.search('(* mary/cp.s *)')),
                         [edge])
        self.assertEqual(list(self.hg.search('(mary/cp.s * *)')),
                         [])
        self.assertEqual(list(self.hg.search('(* * (a/md ((very/w old/ma) '
                                             'violin/cn.s)))')),
                         [edge])
        self.assertEqual(list(self.hg.search('((a/md ((very/w old/ma) '
                                             'violin/cn.s)) * *)')),
                         [])

    def test_star(self):
        self.hg.destroy()
        edge1 = hedge('(is graphbrain/1 great/1)')
        self.hg.add(edge1)
        self.assertEqual(list(self.hg.star(hedge('graphbrain/1'))),
                         [edge1])
        self.assertEqual(list(self.hg.star(hedge('graphbrain/2'))), [])
        self.hg.remove(edge1)
        edge2 = hedge('(says mary/1 (is graphbrain/1 great/1))')
        self.hg.add(edge2)
        self.assertEqual(list(self.hg.star(edge1)),
                         [edge2])

    def test_star_limit(self):
        self.hg.destroy()
        self.hg.add('(is graphbrain/1 great/1)')
        self.hg.add('(is graphbrain/1 great/2)')
        self.hg.add('(is graphbrain/1 great/3)')
        center = hedge('graphbrain/1')
        self.assertEqual(len(list(self.hg.star(center))), 3)
        self.assertEqual(len(list(self.hg.star(center, limit=1))), 1)
        self.assertEqual(len(list(self.hg.star(center, limit=2))), 2)
        self.assertEqual(len(list(self.hg.star(center, limit=10))), 3)

    def test_atoms_with_root(self):
        self.hg.destroy()
        self.hg.add('(is graphbrain/1 great/1)')
        self.assertEqual(list(self.hg.atoms_with_root('graphbrain')),
                         [hedge('graphbrain/1')])
        self.hg.add('(is graphbrain/2 great/1)')
        self.assertEqual(list(self.hg.atoms_with_root('graphbrain')),
                         [hedge('graphbrain/1'), hedge('graphbrain/2')])

    def test_edges_with_edges(self):
        self.hg.destroy()
        edge1 = hedge('(is graphbrain/1 great/1)')
        self.hg.add(edge1)
        edge2 = hedge('(is graphbrain/1 great/2)')
        self.hg.add(edge2)
        self.assertEqual(
            list(self.hg.edges_with_edges((hedge('graphbrain/1'),), 'great')),
            [edge1, edge2])
        self.assertEqual(list(self.hg.edges_with_edges((hedge('graphbrain/1'),
                                                        hedge('is')),
                                                       'great')),
                         [edge1, edge2])
        self.assertEqual(
            list(self.hg.edges_with_edges((hedge('graphbrain/1'),), 'grea')),
            [])

    def test_edges_with_edges2(self):
        self.hg.destroy()
        self.hg.add(hedge('(syns/p (of/b city/c lights/c) paris/c)'))
        self.hg.add(hedge('(syns/p (of/b city/c light/c) paris/c)'))
        self.hg.add(hedge('(going/p i/c (of/b city/c lights/c))'))

        res = self.hg.edges_with_edges([hedge('(of/b city/c lights/c)')])
        res = set([edge.to_str() for edge in res])
        self.assertEqual(res, {'(syns/p (of/b city/c lights/c) paris/c)',
                               '(going/p i/c (of/b city/c lights/c))'})

        res = self.hg.edges_with_edges([hedge('(of/b city/c lights/c)'),
                                        hedge('paris/c')])
        res = set([edge.to_str() for edge in res])
        self.assertEqual(res, {'(syns/p (of/b city/c lights/c) paris/c)'})

        res = self.hg.edges_with_edges([hedge('(of/b city/c lights/c)'),
                                        hedge('paris/c')],
                                       'syns')
        res = set([edge.to_str() for edge in res])
        self.assertEqual(res, {'(syns/p (of/b city/c lights/c) paris/c)'})

    # set_attribute, inc_attribute, dec_attribute, get_str_attribute,
    # get_int_attribute, get_float_attribute
    def test_attributes_atom(self):
        self.hg.destroy()
        self.hg.add('(is graphbrain/1 great/1)')
        atom = hedge('graphbrain/1')
        self.assertEqual(self.hg.get_int_attribute(atom, 'foo'), None)
        self.assertEqual(self.hg.get_int_attribute(atom, 'foo', 0), 0)
        self.hg.set_attribute(atom, 'foo', 66)
        self.assertEqual(self.hg.get_int_attribute(atom, 'foo'), 66)
        self.hg.inc_attribute(atom, 'foo')
        self.assertEqual(self.hg.get_int_attribute(atom, 'foo'), 67)
        self.hg.dec_attribute(atom, 'foo')
        self.assertEqual(self.hg.get_int_attribute(atom, 'foo'), 66)
        self.hg.set_attribute(atom, 'bar', -.77)
        self.assertEqual(self.hg.get_float_attribute(atom, 'bar'), -.77)
        self.hg.set_attribute(atom, 'label', '{"abc": "defg", "": 23}')
        self.assertEqual(self.hg.get_str_attribute(atom, 'label'),
                         '{"abc": "defg", "": 23}')

    # set_attribute, inc_attribute, dec_attribute, get_str_attribute,
    # get_int_attribute, get_float_attribute
    def test_attributes_edge(self):
        self.hg.destroy()
        edge = hedge('(is graphbrain/1 great/1)')
        self.hg.add(edge)
        self.assertEqual(self.hg.get_int_attribute(edge, 'foo'), None)
        self.assertEqual(self.hg.get_int_attribute(edge, 'foo', 0), 0)
        self.hg.set_attribute(edge, 'foo', 66)
        self.assertEqual(self.hg.get_int_attribute(edge, 'foo'), 66)
        self.hg.inc_attribute(edge, 'foo')
        self.assertEqual(self.hg.get_int_attribute(edge, 'foo'), 67)
        self.hg.dec_attribute(edge, 'foo')
        self.assertEqual(self.hg.get_int_attribute(edge, 'foo'), 66)
        self.hg.set_attribute(edge, 'bar', -.77)
        self.assertEqual(self.hg.get_float_attribute(edge, 'bar'), -.77)
        self.hg.set_attribute(edge, 'label', '{"abc": "defg", "": 23}')
        self.assertEqual(self.hg.get_str_attribute(edge, 'label'),
                         '{"abc": "defg", "": 23}')

    def test_degrees(self):
        self.hg.destroy()
        graphbrain = hedge('graphbrain/1')
        great = hedge('great/1')
        self.assertEqual(self.hg.degree(graphbrain), 0)
        self.hg.add('(is graphbrain/1 great/1)')
        self.assertEqual(self.hg.degree(graphbrain), 1)
        self.assertEqual(self.hg.degree(great), 1)
        self.hg.add('(size graphbrain/1 7)')
        self.assertEqual(self.hg.degree(graphbrain), 2)
        self.assertEqual(self.hg.degree(great), 1)
        self.hg.remove(hedge('(is graphbrain/1 great/1)'))
        self.assertEqual(self.hg.degree(graphbrain), 1)
        self.assertEqual(self.hg.degree(great), 0)
        self.hg.remove(hedge('(size graphbrain/1 7)'))
        self.assertEqual(self.hg.degree(graphbrain), 0)

    def test_deep_degrees(self):
        self.hg.destroy()
        edge1 = hedge('((is/a going/p) mary/c (to (the/m gym/c)))')
        self.hg.add(edge1)
        mary = hedge('mary/c')
        gym = hedge('gym/c')
        is_going = hedge('(is/a going/p)')
        self.assertEqual(self.hg.deep_degree(edge1), 0)
        self.assertEqual(self.hg.degree(mary), 1)
        self.assertEqual(self.hg.deep_degree(mary), 1)
        self.assertEqual(self.hg.degree(gym), 0)
        self.assertEqual(self.hg.deep_degree(gym), 1)
        self.assertEqual(self.hg.degree(is_going), 1)
        self.assertEqual(self.hg.deep_degree(is_going), 1)
        self.assertEqual(self.hg.deep_degree(gym), 1)
        edge2 = hedge('((is/a going/p) john/c (to (the/m gym/c)))')
        self.hg.add(edge2)
        self.assertEqual(self.hg.degree(gym), 0)
        self.assertEqual(self.hg.deep_degree(gym), 2)
        self.assertEqual(self.hg.degree(is_going), 2)
        self.assertEqual(self.hg.deep_degree(is_going), 2)

    def test_ego(self):
        self.hg.destroy()
        self.hg.add('(is graphbrain/1 great/1)')
        self.hg.add('(is graphbrain/1 great/2)')
        self.assertEqual(self.hg.ego(hedge('graphbrain/1')),
                         {hedge('graphbrain/1'), hedge('is'), hedge('great/1'),
                          hedge('great/2')})

    def test_remove_by_pattern(self):
        self.hg.destroy()
        self.hg.add('(is/pd graphbrain/cp great/c)')
        self.hg.add('(says/pd mary/cp)')
        self.hg.add('(says/pd mary/cp (is/pd graphbrain/cp great/c))')
        self.hg.add('(says/pd mary/cp (is/pd graphbrain/cp great/c) extra/c)')
        self.assertEqual(
            set(self.hg.search('(says/pd * '
                               '(is/pd graphbrain/cp great/c))')),
            {hedge('(says/pd mary/cp (is/pd graphbrain/cp great/c))')})
        self.hg.remove_by_pattern('(says/pd * *)')
        self.assertFalse(
            self.hg.exists(
                hedge('(says/pd mary/cp (is/pd graphbrain/cp great/c))')))
        self.assertTrue(self.hg.exists(hedge('(is/pd graphbrain/cp great/c)')))

    def test_root_degrees(self):
        self.hg.destroy()
        self.hg.add('(is/pd graphbrain/c great/c/1)')
        self.hg.add('(says/pd mary/cp)')
        self.hg.add('(says/pd mary/cp (is/pd graphbrain/cp great/c/2))')
        self.hg.add('(says/pd mary/cp (is/pd graphbrain/cs great/c) extra/c)')
        self.assertEqual(self.hg.root_degrees(hedge('graphbrain/cp')), (1, 3))
        self.assertEqual(self.hg.root_degrees(hedge('great/c')), (1, 3))
        self.assertEqual(self.hg.root_degrees(hedge('says/pd')), (3, 3))

    def test_sum_degree(self):
        self.hg.destroy()
        self.hg.add('(is/pd graphbrain/c great/c/1)')
        self.hg.add('(says/pd mary/cp)')
        self.hg.add('(says/pd mary/cp (is/pd graphbrain/c great/c/2))')
        self.hg.add('(says/pd mary/cp (is/pd graphbrain/c great/c) extra/c)')
        self.assertEqual(self.hg.sum_degree({hedge('graphbrain/c'),
                                             hedge('says/pd')}), 4)

    def test_sum_deep_degree(self):
        self.hg.destroy()
        self.hg.add('(is/pd graphbrain/c great/c/1)')
        self.hg.add('(says/pd mary/cp)')
        self.hg.add('(says/pd mary/cp (is/pd graphbrain/c great/c/2))')
        self.hg.add('(says/pd mary/cp (is/pd graphbrain/c great/c) extra/c)')
        self.assertEqual(self.hg.sum_deep_degree({hedge('graphbrain/c'),
                                                  hedge('says/pd')}), 6)

    def test_primary_1(self):
        self.hg.destroy()
        edge1 = hedge('((is/a going/p) mary/c (to (the/m gym/c)))')
        self.hg.add(edge1)
        self.assertTrue(self.hg.is_primary(edge1))
        self.hg.set_primary(edge1, False)
        self.assertFalse(self.hg.is_primary(edge1))
        self.assertFalse(self.hg.is_primary(hedge('(is/a going/p)')))
        self.hg.set_primary(hedge('(is/a going/p)'), True)
        self.assertTrue(self.hg.is_primary(hedge('(is/a going/p)')))
        self.assertFalse(self.hg.is_primary(hedge('mary/c')))
        self.hg.set_primary(hedge('mary/c'), True)
        self.assertTrue(self.hg.is_primary(hedge('mary/c')))

    def test_primary_2(self):
        self.hg.destroy()
        edge1 = hedge('((is/a going/p) mary/c (to (the/m gym/c)))')
        self.hg.add(edge1, primary=False)
        self.assertFalse(self.hg.is_primary(edge1))
        self.hg.set_primary(edge1, True)
        self.assertTrue(self.hg.is_primary(edge1))
        self.assertFalse(self.hg.is_primary(hedge('(is/a going/p)')))
        self.hg.set_primary(hedge('(is/a going/p)'), True)
        self.assertTrue(self.hg.is_primary(hedge('(is/a going/p)')))
        self.assertFalse(self.hg.is_primary(hedge('mary/c')))
        self.hg.set_primary(hedge('mary/c'), True)
        self.assertTrue(self.hg.is_primary(hedge('mary/c')))

    def test_add_large_edge(self):
        s1 = ("(title/p/.reddit anutensil/c/reddit.user "
              "(discovered/pd.<f----.sr/en (+/b.aam/. energy/cp.s/en "
              "company/cp.s/en staff/cp.s/en) (working/pc.------.x/en "
              "(at/t/en (+/b.am/. climate/cp.s/en ministry/cp.s/en)))) "
              "(says/pd.|f--3s.sr/en (green/ma/en "
              "(+/b.am/. party/cc.s/en mp/cp.s/en)) "
              "(have/pr.|f----.so/en (+/b.aam/. fossil/ca/en fuel/cc.s/en "
              "giants/cc.p/en) (no/md/en (there/m/en place/cc.s/en)))) "
              "((even/m/en 's/pd.|f--3s.s/en more/w/en outrageous/ma/en "
              "taxpayers/cc.p/en ((are/av.|f----/en footing/pr.|pg---.o/en) "
              "(the/md/en bill/cc.s/en))) it/ci/en) "
              "('s/pd.|f--3s.xsr/en (at/t/en (:/b/. (a/md/en time/cc.s/en) "
              "((are/av.|f----/en struggling/pr.|pg---.s/en) "
              "(+/b.aam/. british/ca/en gas/cp.s/en customers/cc.p/en)))) "
              "it/ci/en ((to/ai/en make/pc.-i----.ox/en) "
              "(£/m/en (+/b.am/. 1_4bn/c#/en profits/cc.p/en)) "
              "(this/md/en year/cc.s/en))))")
        self.hg.destroy()
        self.hg.add(s1)
        self.assertTrue(self.hg.exists(s1))

    def test_add_large_edges(self):
        s1 = ("(title/p/.reddit anutensil/c/reddit.user "
              "(discovered/pd.<f----.sr/en (+/b.aam/. energy/cp.s/en "
              "company/cp.s/en staff/cp.s/en) (working/pc.------.x/en "
              "(at/t/en (+/b.am/. climate/cp.s/en ministry/cp.s/en)))) "
              "(says/pd.|f--3s.sr/en (green/ma/en "
              "(+/b.am/. party/cc.s/en mp/cp.s/en)) "
              "(have/pr.|f----.so/en (+/b.aam/. fossil/ca/en fuel/cc.s/en "
              "giants/cc.p/en) (no/md/en (there/m/en place/cc.s/en)))) "
              "((even/m/en 's/pd.|f--3s.s/en more/w/en outrageous/ma/en "
              "taxpayers/cc.p/en ((are/av.|f----/en footing/pr.|pg---.o/en) "
              "(the/md/en bill/cc.s/en))) it/ci/en) "
              "('s/pd.|f--3s.xsr/en (at/t/en (:/b/. (a/md/en time/cc.s/en) "
              "((are/av.|f----/en struggling/pr.|pg---.s/en) "
              "(+/b.aam/. british/ca/en gas/cp.s/en customers/cc.p/en)))) "
              "it/ci/en ((to/ai/en make/pc.-i----.ox/en) "
              "(£/m/en (+/b.am/. 1_4bn/c#/en profits/cc.p/en)) "
              "(this/md/en year/cc.s/en))))")
        s2 = ("(titles/p/.reddit anutensil/c/reddit.user "
              "(discovered/pd.<f----.sr/en (+/b.aam/. energy/cp.s/en "
              "company/cp.s/en staff/cp.s/en) (working/pc.------.x/en "
              "(at/t/en (+/b.am/. climate/cp.s/en ministry/cp.s/en)))) "
              "(says/pd.|f--3s.sr/en (green/ma/en "
              "(+/b.am/. party/cc.s/en mp/cp.s/en)) "
              "(have/pr.|f----.so/en (+/b.aam/. fossil/ca/en fuel/cc.s/en "
              "giants/cc.p/en) (no/md/en (there/m/en place/cc.s/en)))) "
              "((even/m/en 's/pd.|f--3s.s/en more/w/en outrageous/ma/en "
              "taxpayers/cc.p/en ((are/av.|f----/en footing/pr.|pg---.o/en) "
              "(the/md/en bill/cc.s/en))) it/ci/en) "
              "('s/pd.|f--3s.xsr/en (at/t/en (:/b/. (a/md/en time/cc.s/en) "
              "((are/av.|f----/en struggling/pr.|pg---.s/en) "
              "(+/b.aam/. british/ca/en gas/cp.s/en customers/cc.p/en)))) "
              "it/ci/en ((to/ai/en make/pc.-i----.ox/en) "
              "(£/m/en (+/b.am/. 1_4bn/c#/en profits/cc.p/en)) "
              "(this/md/en year/cc.s/en))))")
        s3 = ("(title/p/.reddit anutensil/c/reddit.user "
              "(discovered/pd.<f----.sr/en (+/b.aam/. energy/cp.s/en "
              "company/cp.s/en staff/cp.s/en) (working/pc.------.x/en "
              "(at/t/en (+/b.am/. climate/cp.s/en ministry/cp.s/en)))) "
              "(says/pd.|f--3s.sr/en (green/ma/en "
              "(+/b.am/. party/cc.s/en mp/cp.s/en)) "
              "(have/pr.|f----.so/en (+/b.aam/. fossil/ca/en fuel/cc.s/en "
              "giants/cc.p/en) (no/md/en (there/m/en place/cc.s/en)))) "
              "((even/m/en 's/pd.|f--3s.s/en more/w/en outrageous/ma/en "
              "taxpayers/cc.p/en ((are/av.|f----/en footing/pr.|pg---.o/en) "
              "(the/md/en bill/cc.s/en))) it/ci/en) "
              "('s/pd.|f--3s.xsr/en (at/t/en (:/b/. (a/md/en time/cc.s/en) "
              "((are/av.|f----/en struggling/pr.|pg---.s/en) "
              "(+/b.aam/. british/ca/en gas/cp.s/en customers/cc.p/en)))) "
              "it/ci/en ((to/ai/en make/pc.-i----.ox/en) "
              "(£/m/en (+/b.am/. 1_4bn/c#/en profits/cc.p/en)) "
              "(these/md/en years/cc.p/en))))")
        self.hg.destroy()
        self.hg.add(s1)
        self.assertTrue(self.hg.exists(s1))
        self.assertFalse(self.hg.exists(s2))
        self.assertFalse(self.hg.exists(s3))
        self.hg.add(s2)
        self.assertTrue(self.hg.exists(s2))
        self.assertFalse(self.hg.exists(s3))
        self.hg.add(s3)
        self.assertTrue(self.hg.exists(s3))

    def test_large_edges_all(self):
        s1 = ("(title/p/.reddit anutensil/c/reddit.user "
              "(discovered/pd.<f----.sr/en (+/b.aam/. energy/cp.s/en "
              "company/cp.s/en staff/cp.s/en) (working/pc.------.x/en "
              "(at/t/en (+/b.am/. climate/cp.s/en ministry/cp.s/en)))) "
              "(says/pd.|f--3s.sr/en (green/ma/en "
              "(+/b.am/. party/cc.s/en mp/cp.s/en)) "
              "(have/pr.|f----.so/en (+/b.aam/. fossil/ca/en fuel/cc.s/en "
              "giants/cc.p/en) (no/md/en (there/m/en place/cc.s/en)))) "
              "((even/m/en 's/pd.|f--3s.s/en more/w/en outrageous/ma/en "
              "taxpayers/cc.p/en ((are/av.|f----/en footing/pr.|pg---.o/en) "
              "(the/md/en bill/cc.s/en))) it/ci/en) "
              "('s/pd.|f--3s.xsr/en (at/t/en (:/b/. (a/md/en time/cc.s/en) "
              "((are/av.|f----/en struggling/pr.|pg---.s/en) "
              "(+/b.aam/. british/ca/en gas/cp.s/en customers/cc.p/en)))) "
              "it/ci/en ((to/ai/en make/pc.-i----.ox/en) "
              "(£/m/en (+/b.am/. 1_4bn/c#/en profits/cc.p/en)) "
              "(this/md/en year/cc.s/en))))")
        s2 = ("(titles/p/.reddit anutensil/c/reddit.user "
              "(discovered/pd.<f----.sr/en (+/b.aam/. energy/cp.s/en "
              "company/cp.s/en staff/cp.s/en) (working/pc.------.x/en "
              "(at/t/en (+/b.am/. climate/cp.s/en ministry/cp.s/en)))) "
              "(says/pd.|f--3s.sr/en (green/ma/en "
              "(+/b.am/. party/cc.s/en mp/cp.s/en)) "
              "(have/pr.|f----.so/en (+/b.aam/. fossil/ca/en fuel/cc.s/en "
              "giants/cc.p/en) (no/md/en (there/m/en place/cc.s/en)))) "
              "((even/m/en 's/pd.|f--3s.s/en more/w/en outrageous/ma/en "
              "taxpayers/cc.p/en ((are/av.|f----/en footing/pr.|pg---.o/en) "
              "(the/md/en bill/cc.s/en))) it/ci/en) "
              "('s/pd.|f--3s.xsr/en (at/t/en (:/b/. (a/md/en time/cc.s/en) "
              "((are/av.|f----/en struggling/pr.|pg---.s/en) "
              "(+/b.aam/. british/ca/en gas/cp.s/en customers/cc.p/en)))) "
              "it/ci/en ((to/ai/en make/pc.-i----.ox/en) "
              "(£/m/en (+/b.am/. 1_4bn/c#/en profits/cc.p/en)) "
              "(this/md/en year/cc.s/en))))")
        s3 = ("(title/p/.reddit anutensil/c/reddit.user "
              "(discovered/pd.<f----.sr/en (+/b.aam/. energy/cp.s/en "
              "company/cp.s/en staff/cp.s/en) (working/pc.------.x/en "
              "(at/t/en (+/b.am/. climate/cp.s/en ministry/cp.s/en)))) "
              "(says/pd.|f--3s.sr/en (green/ma/en "
              "(+/b.am/. party/cc.s/en mp/cp.s/en)) "
              "(have/pr.|f----.so/en (+/b.aam/. fossil/ca/en fuel/cc.s/en "
              "giants/cc.p/en) (no/md/en (there/m/en place/cc.s/en)))) "
              "((even/m/en 's/pd.|f--3s.s/en more/w/en outrageous/ma/en "
              "taxpayers/cc.p/en ((are/av.|f----/en footing/pr.|pg---.o/en) "
              "(the/md/en bill/cc.s/en))) it/ci/en) "
              "('s/pd.|f--3s.xsr/en (at/t/en (:/b/. (a/md/en time/cc.s/en) "
              "((are/av.|f----/en struggling/pr.|pg---.s/en) "
              "(+/b.aam/. british/ca/en gas/cp.s/en customers/cc.p/en)))) "
              "it/ci/en ((to/ai/en make/pc.-i----.ox/en) "
              "(£/m/en (+/b.am/. 1_4bn/c#/en profits/cc.p/en)) "
              "(these/md/en years/cc.p/en))))")
        self.hg.destroy()
        self.hg.add(s1)
        self.hg.add(s2)
        self.hg.add(s3)
        res = set([edge.to_str() for edge in self.hg.all()])
        self.assertTrue(s1 in res)
        self.assertTrue(s2 in res)
        self.assertTrue(s3 in res)


if __name__ == '__main__':
    unittest.main()
