import unittest
from graphbrain import *


class TestHypergraph(unittest.TestCase):
    def setUp(self):
        self.hg = hypergraph('test.hg')

    def tearDown(self):
        self.hg.close()

    def test_close(self):
        self.hg.close()

    def test_name(self):
        self.assertEqual(self.hg.name(), 'test.hg')

    def test_destroy(self):
        self.hg.destroy()
        self.hg.add(('src', 'graphbrain/1', ('size', 'graphbrain/1', '7')))
        self.assertTrue(self.hg.exists(('src', 'graphbrain/1',
                                        ('size', 'graphbrain/1', '7'))))
        self.hg.destroy()
        self.assertFalse(self.hg.exists(('src', 'graphbrain/1',
                                         ('size', 'graphbrain/1', '7'))))

    def test_all(self):
        self.hg.destroy()
        self.hg.add(('size', 'graphbrain/1', '7'))
        self.hg.add(('is', 'graphbrain/1', 'great/1'))
        self.hg.add(('src', 'mary/1', ('is', 'graphbrain/1', 'great/1')))

        labels = set([ent2str(v) for v in self.hg.all()])
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
        self.hg.add(('size', 'graphbrain/1', '7'))
        self.hg.add(('is', 'graphbrain/1', 'great/1'))
        self.hg.add(('src', 'mary/1', ('is', 'graphbrain/1', 'great/1')))

        labels = set([ent2str(v) for v in self.hg.all_atoms()])
        self.assertEqual(labels,
                         {'size', 'graphbrain/1', '7', 'is', 'great/1', 'src',
                          'mary/1'})
        self.hg.destroy()
        labels = set(self.hg.all())
        self.assertEqual(labels, set())

    def test_edges(self):
        self.hg.destroy()
        self.hg.add(('size', 'graphbrain/1', '7'))
        self.hg.add(('is', 'graphbrain/1', 'great/1'))
        self.hg.add(('src', 'mary/1', ('is', 'graphbrain/1', 'great/1')))

        labels = set([ent2str(v) for v in self.hg.all_edges()])
        self.assertEqual(labels,
                         {'(size graphbrain/1 7)', '(is graphbrain/1 great/1)',
                          '(src mary/1 (is graphbrain/1 great/1))'})
        self.hg.destroy()
        labels = set(self.hg.all())
        self.assertEqual(labels, set())

    def test_all_attributes(self):
        self.hg.destroy()
        self.hg.add(('size', 'graphbrain/1', '7'))
        self.hg.add(('is', 'graphbrain/1', 'great/1'))
        self.hg.add(('src', 'mary/1', ('is', 'graphbrain/1', 'great/1')))

        labels = set(['%s %s' % (ent2str(t[0]), t[1]['d'])
                      for t in self.hg.all_attributes()])
        self.assertEqual(labels, {'size 1', 'graphbrain/1 2', '7 1', 'is 1',
                                  'great/1 1', 'src 1', 'mary/1 1',
                                  '(size graphbrain/1 7) 0',
                                  '(is graphbrain/1 great/1) 1',
                                  '(src mary/1 (is graphbrain/1 great/1)) 0'})
        self.hg.destroy()
        labels = set(self.hg.all_attributes())
        self.assertEqual(labels, set())

    # atom_count(), edge_count(), total_degrees()
    def test_counters(self):
        self.hg.destroy()
        self.hg.add(('is', 'graphbrain/1', 'great/1'))
        self.assertEqual(self.hg.atom_count(), 3)
        self.assertEqual(self.hg.edge_count(), 1)
        self.assertEqual(self.hg.total_degree(), 3)
        self.hg.add(('says', 'mary/1', ('is', 'graphbrain/1', 'great/1')))
        self.assertEqual(self.hg.atom_count(), 5)
        self.assertEqual(self.hg.edge_count(), 2)
        self.assertEqual(self.hg.total_degree(), 6)
        self.hg.remove(('is', 'graphbrain/1', 'great/1'))
        self.assertEqual(self.hg.edge_count(), 1)
        self.assertEqual(self.hg.total_degree(), 3)
        self.hg.remove(('says', 'mary/1', ('is', 'graphbrain/1', 'great/1')))
        self.assertEqual(self.hg.edge_count(), 0)
        self.assertEqual(self.hg.total_degree(), 0)

    # exists, add, remove
    def test_ops1(self):
        self.hg.destroy()
        self.hg.add(('is/pd', 'graphbrain/cp', 'great/c'))
        self.assertTrue(self.hg.exists(('is/pd', 'graphbrain/cp', 'great/c')))
        self.hg.remove(('is/pd', 'graphbrain/cp', 'great/c'))
        self.assertFalse(self.hg.exists(('is/pd', 'graphbrain/cp', 'great/c')))

    # exists, add, remove
    def test_ops_2(self):
        self.hg.destroy()
        self.hg.add(('src', 'graphbrain/1', ('size', 'graphbrain/1', '7')))
        self.assertTrue(self.hg.exists(('src', 'graphbrain/1',
                                        ('size', 'graphbrain/1', '7'))))
        self.hg.remove(('src', 'graphbrain/1', ('size', 'graphbrain/1', '7')))
        self.assertFalse(self.hg.exists(('src', 'graphbrain/1',
                                         ('size', 'graphbrain/1', '7'))))

    def test_add_deep(self):
        self.hg.destroy()
        self.hg.add(('says', 'mary', ('is', 'graphbrain/1', 'great/1')),
                    deep=True)
        self.assertTrue(self.hg.exists(('is', 'graphbrain/1', 'great/1')))
        self.assertTrue(
            self.hg.exists(('says', 'mary',
                            ('is', 'graphbrain/1', 'great/1'))))

    def test_pat2ents(self):
        self.hg.destroy()
        self.hg.add(('is/pd', 'graphbrain/cp', 'great/c'))
        self.hg.add(('says/pd', 'mary/cp'))
        self.hg.add(('says/pd', 'mary/cp',
                     ('is/pd', 'graphbrain/cp', 'great/c')))
        self.hg.add(('says/pd', 'mary/cp',
                     ('is/pd', 'graphbrain/cp', 'great/c'), 'extra/c'))
        self.assertEqual(list(self.hg.pat2ents(('*', 'graphbrain/cp', '*'))),
                         [('is/pd', 'graphbrain/cp', 'great/c')])
        self.assertEqual(list(self.hg.pat2ents(('is/pd', 'graphbrain/cp',
                                                '*'))),
                         [('is/pd', 'graphbrain/cp', 'great/c')])
        self.assertEqual(list(self.hg.pat2ents(('x', '*', '*'))), [])
        self.assertEqual(
            list(self.hg.pat2ents(('says/pd', '*',
                                   ('is/pd', 'graphbrain/cp', 'great/c')))),
            [('says/pd', 'mary/cp', ('is/pd', 'graphbrain/cp', 'great/c'))])
        self.assertEqual(
            list(self.hg.pat2ents(('says/pd', '*',
                                   ('is/pd', '*', '*')))),
            [('says/pd', 'mary/cp', ('is/pd', 'graphbrain/cp', 'great/c'))])

    def test_pat2ents_open_ended(self):
        self.hg.destroy()
        self.hg.add(('is/pd', 'graphbrain/cp', 'great/c'))
        self.hg.add(('says/pd', 'mary/cp'))
        self.hg.add(('says/pd', 'mary/cp',
                     ('is/pd', 'graphbrain/cp', 'great/c')))
        self.hg.add(('says/pd', 'mary/cp',
                     ('is/pd', 'graphbrain/cp', 'great/c'), 'extra/c'))
        self.assertEqual(list(self.hg.pat2ents(('*', 'graphbrain/cp',
                                                     '*', '...'))),
                         [('is/pd', 'graphbrain/cp', 'great/c')])
        self.assertEqual(list(self.hg.pat2ents(('is/pd', 'graphbrain/cp',
                                                '*', '...'))),
                         [('is/pd', 'graphbrain/cp', 'great/c')])
        self.assertEqual(list(self.hg.pat2ents(('x', '*', '*', '...'))), [])
        self.assertEqual(list(self.hg.pat2ents(('says/pd', '*',
                                                ('is/pd', 'graphbrain/cp',
                                                 'great/c'), '...'))),
                         [('says/pd', 'mary/cp',
                           ('is/pd', 'graphbrain/cp', 'great/c')),
                          ('says/pd', 'mary/cp',
                           ('is/pd', 'graphbrain/cp', 'great/c'),
                           'extra/c')])

    def test_pat2ents_star(self):
        self.hg.destroy()
        self.hg.add(('is/pd', 'graphbrain/cp', 'great/c'))
        self.hg.add(('says/pd', 'mary/cp'))
        self.hg.add(('says/pd', 'mary/cp',
                     ('is/pd', 'graphbrain/cp', 'great/c')))
        self.hg.add(('says/pd', 'mary/cp',
                     ('is/pd', 'graphbrain/cp', 'great/c'), 'extra/c'))
        self.assertEqual(list(self.hg.pat2ents('*')),
                         [('is/pd', 'graphbrain/cp', 'great/c'),
                          ('says/pd', 'mary/cp',
                           ('is/pd', 'graphbrain/cp', 'great/c'), 'extra/c'),
                          ('says/pd', 'mary/cp',
                           ('is/pd', 'graphbrain/cp', 'great/c')),
                          ('says/pd', 'mary/cp'),
                          'extra/c', 'graphbrain/cp', 'great/c', 'is/pd',
                          'mary/cp', 'says/pd'])

    def test_pat2ents_at(self):
        self.hg.destroy()
        self.hg.add(('is/pd', 'graphbrain/cp', 'great/c'))
        self.hg.add(('says/pd', 'mary/cp'))
        self.hg.add(('says/pd', 'mary/cp',
                     ('is/pd', 'graphbrain/cp', 'great/c')))
        self.hg.add(('says/pd', 'mary/cp',
                     ('is/pd', 'graphbrain/cp', 'great/c'), 'extra/c'))
        self.assertEqual(list(self.hg.pat2ents('@')),
                         ['extra/c', 'graphbrain/cp', 'great/c', 'is/pd',
                          'mary/cp', 'says/pd'])

    def test_pat2ents_amp(self):
        self.hg.destroy()
        self.hg.add(('is/pd', 'graphbrain/cp', 'great/c'))
        self.hg.add(('says/pd', 'mary/cp'))
        self.hg.add(('says/pd', 'mary/cp',
                     ('is/pd', 'graphbrain/cp', 'great/c')))
        self.hg.add(('says/pd', 'mary/cp',
                     ('is/pd', 'graphbrain/cp', 'great/c'), 'extra/c'))
        self.assertEqual(list(self.hg.pat2ents('&')),
                         [('is/pd', 'graphbrain/cp', 'great/c'),
                          ('says/pd', 'mary/cp',
                           ('is/pd', 'graphbrain/cp', 'great/c'), 'extra/c'),
                          ('says/pd', 'mary/cp',
                           ('is/pd', 'graphbrain/cp', 'great/c')),
                          ('says/pd', 'mary/cp')])

    def test_pat2ents_non_atomic_pred(self):
        self.hg.destroy()
        edge = str2ent('((is/a playing/pd.so ) mary/cp.s '
                       '(a/md ((very/w old/ma) violin/cn.s)))')
        self.hg.add(edge, deep=True)
        self.assertEqual(list(self.hg.pat2ents('((is/a playing/pd.so) ...)')),
                         [(('is/a', 'playing/pd.so'), 'mary/cp.s',
                           ('a/md', (('very/w', 'old/ma'), 'violin/cn.s')))])
        self.assertEqual(list(self.hg.pat2ents('((is/a playing/pd.so) * *)')),
                         [(('is/a', 'playing/pd.so'), 'mary/cp.s',
                           ('a/md', (('very/w', 'old/ma'), 'violin/cn.s')))])
        self.assertEqual(list(self.hg.pat2ents('((is/a playing/pd.so) @ &)')),
                         [(('is/a', 'playing/pd.so'), 'mary/cp.s',
                           ('a/md', (('very/w', 'old/ma'), 'violin/cn.s')))])
        self.assertEqual(list(self.hg.pat2ents('((is/a playing/pd.so) @ @)')),
                         [])
        self.assertEqual(list(self.hg.pat2ents('((is/a playing/pd.so) & &)')),
                         [])
        self.assertEqual(list(self.hg.pat2ents('(* mary/cp.s *)')),
                         [(('is/a', 'playing/pd.so'), 'mary/cp.s',
                           ('a/md', (('very/w', 'old/ma'), 'violin/cn.s')))])
        self.assertEqual(list(self.hg.pat2ents('(mary/cp.s * *)')),
                         [])
        self.assertEqual(list(self.hg.pat2ents('(* * (a/md ((very/w old/ma) '
                                               'violin/cn.s)))')),
                         [(('is/a', 'playing/pd.so'), 'mary/cp.s',
                           ('a/md', (('very/w', 'old/ma'), 'violin/cn.s')))])
        self.assertEqual(list(self.hg.pat2ents('((a/md ((very/w old/ma) '
                                               'violin/cn.s)) * *)')),
                         [])

    def test_star(self):
        self.hg.destroy()
        self.hg.add(('is', 'graphbrain/1', 'great/1'))
        self.assertEqual(list(self.hg.star('graphbrain/1')),
                         [('is', 'graphbrain/1', 'great/1')])
        self.assertEqual(list(self.hg.star('graphbrain/2')), [])
        self.hg.remove(('is', 'graphbrain/1', 'great/1'))
        self.hg.add(('says', 'mary/1', ('is', 'graphbrain/1', 'great/1')))
        self.assertEqual(list(self.hg.star(('is', 'graphbrain/1', 'great/1'))),
                         [('says', 'mary/1',
                           ('is', 'graphbrain/1', 'great/1'))])

    def test_star_limit(self):
        self.hg.destroy()
        self.hg.add(('is', 'graphbrain/1', 'great/1'))
        self.hg.add(('is', 'graphbrain/1', 'great/2'))
        self.hg.add(('is', 'graphbrain/1', 'great/3'))
        self.assertEqual(len(list(self.hg.star('graphbrain/1'))), 3)
        self.assertEqual(len(list(self.hg.star('graphbrain/1', limit=1))), 1)
        self.assertEqual(len(list(self.hg.star('graphbrain/1', limit=2))), 2)
        self.assertEqual(len(list(self.hg.star('graphbrain/1', limit=10))), 3)

    def test_atoms_with_root(self):
        self.hg.destroy()
        self.hg.add(('is', 'graphbrain/1', 'great/1'))
        self.assertEqual(list(self.hg.atoms_with_root('graphbrain')),
                         ['graphbrain/1'])
        self.hg.add(('is', 'graphbrain/2', 'great/1'))
        self.assertEqual(list(self.hg.atoms_with_root('graphbrain')),
                         ['graphbrain/1', 'graphbrain/2'])

    def test_edges_with_atoms(self):
        self.hg.destroy()
        self.hg.add(('is', 'graphbrain/1', 'great/1'))
        self.hg.add(('is', 'graphbrain/1', 'great/2'))
        self.assertEqual(list(self.hg.edges_with_atoms(('graphbrain/1',),
                                                       'great')),
                         [('is', 'graphbrain/1', 'great/1'),
                          ('is', 'graphbrain/1', 'great/2')])
        self.assertEqual(list(self.hg.edges_with_atoms(('graphbrain/1', 'is'),
                                                       'great')),
                         [('is', 'graphbrain/1', 'great/1'),
                          ('is', 'graphbrain/1', 'great/2')])
        self.assertEqual(list(self.hg.edges_with_atoms(('graphbrain/1',),
                                                       'grea')),
                         [])

    # set_attribute, inc_attribute, dec_attribute, get_str_attribute,
    # get_int_attribute, get_float_attribute
    def test_attributes_atom(self):
        self.hg.destroy()
        self.hg.add(('is', 'graphbrain/1', 'great/1'))
        self.assertEqual(self.hg.get_int_attribute('graphbrain/1', 'foo'),
                         None)
        self.assertEqual(self.hg.get_int_attribute('graphbrain/1', 'foo', 0),
                         0)
        self.hg.set_attribute('graphbrain/1', 'foo', 66)
        self.assertEqual(self.hg.get_int_attribute('graphbrain/1', 'foo'), 66)
        self.hg.inc_attribute('graphbrain/1', 'foo')
        self.assertEqual(self.hg.get_int_attribute('graphbrain/1', 'foo'), 67)
        self.hg.dec_attribute('graphbrain/1', 'foo')
        self.assertEqual(self.hg.get_int_attribute('graphbrain/1', 'foo'), 66)
        self.hg.set_attribute('graphbrain/1', 'bar', -.77)
        self.assertEqual(self.hg.get_float_attribute('graphbrain/1', 'bar'),
                         -.77)
        self.hg.set_attribute('graphbrain/1', 'label', 'x0 x0 | test \\ test')
        self.assertEqual(self.hg.get_str_attribute('graphbrain/1', 'label'),
                         'x0 x0   test   test')

    # set_attribute, inc_attribute, dec_attribute, get_str_attribute,
    # get_int_attribute, get_float_attribute
    def test_attributes_edge(self):
        self.hg.destroy()
        self.hg.add(('is', 'graphbrain/1', 'great/1'))
        self.assertEqual(self.hg.get_int_attribute(('is',
                                                    'graphbrain/1', 'great/1'),
                                                   'foo'),
                         None)
        self.assertEqual(self.hg.get_int_attribute(('is',
                                                    'graphbrain/1', 'great/1'),
                                                   'foo', 0),
                         0)
        self.hg.set_attribute(('is', 'graphbrain/1', 'great/1'), 'foo', 66)
        self.assertEqual(self.hg.get_int_attribute(('is',
                                                    'graphbrain/1', 'great/1'),
                                                   'foo'),
                         66)
        self.hg.inc_attribute(('is', 'graphbrain/1', 'great/1'), 'foo')
        self.assertEqual(self.hg.get_int_attribute(('is',
                                                    'graphbrain/1', 'great/1'),
                                                   'foo'),
                         67)
        self.hg.dec_attribute(('is', 'graphbrain/1', 'great/1'), 'foo')
        self.assertEqual(self.hg.get_int_attribute(('is',
                                                    'graphbrain/1', 'great/1'),
                                                   'foo'),
                         66)
        self.hg.set_attribute(('is', 'graphbrain/1', 'great/1'), 'bar', -.77)
        self.assertEqual(self.hg.get_float_attribute(('is', 'graphbrain/1',
                                                      'great/1'),
                                                     'bar'),
                         -.77)
        self.hg.set_attribute(('is', 'graphbrain/1', 'great/1'), 'label',
                              'x0 x0 | test \\ test')
        self.assertEqual(self.hg.get_str_attribute(('is',
                                                    'graphbrain/1', 'great/1'),
                                                   'label'),
                         'x0 x0   test   test')

    def test_degree(self):
        self.hg.destroy()
        self.assertEqual(self.hg.degree('graphbrain/1'), 0)
        self.hg.add(('is', 'graphbrain/1', 'great/1'))
        self.assertEqual(self.hg.degree('graphbrain/1'), 1)
        self.assertEqual(self.hg.degree('great/1'), 1)
        self.hg.add(('size', 'graphbrain/1', '7'))
        self.assertEqual(self.hg.degree('graphbrain/1'), 2)
        self.assertEqual(self.hg.degree('great/1'), 1)
        self.hg.remove(('is', 'graphbrain/1', 'great/1'))
        self.assertEqual(self.hg.degree('graphbrain/1'), 1)
        self.assertEqual(self.hg.degree('great/1'), 0)
        self.hg.remove(('size', 'graphbrain/1', '7'))
        self.assertEqual(self.hg.degree('graphbrain/1'), 0)

    def test_ego(self):
        self.hg.destroy()
        self.hg.add(('is', 'graphbrain/1', 'great/1'))
        self.hg.add(('is', 'graphbrain/1', 'great/2'))
        self.assertEqual(self.hg.ego('graphbrain/1'),
                         {'graphbrain/1', 'is', 'great/1', 'great/2'})

    def test_remove_by_pattern(self):
        self.hg.destroy()
        self.hg.add(('is/pd', 'graphbrain/cp', 'great/c'))
        self.hg.add(('says/pd', 'mary/cp'))
        self.hg.add(('says/pd', 'mary/cp',
                     ('is/pd', 'graphbrain/cp', 'great/c')))
        self.hg.add(('says/pd', 'mary/cp',
                     ('is/pd', 'graphbrain/cp', 'great/c'), 'extra/c'))
        self.assertEqual(
            set(self.hg.pat2ents(('says/pd', '*',
                                  ('is/pd', 'graphbrain/cp', 'great/c')))),
            {('says/pd', 'mary/cp', ('is/pd', 'graphbrain/cp', 'great/c'))})
        self.hg.remove_by_pattern(('says/pd', '*', '*'))
        self.assertFalse(self.hg.exists(('says/pd', 'mary/cp',
                                         ('is/pd',
                                          'graphbrain/cp', 'great/c'))))
        self.assertTrue(self.hg.exists(('is/pd', 'graphbrain/cp', 'great/c')))


if __name__ == '__main__':
    unittest.main()
