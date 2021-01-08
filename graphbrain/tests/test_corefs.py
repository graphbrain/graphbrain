import unittest

from graphbrain import hedge
from graphbrain import hgraph
from graphbrain.meaning.corefs import are_corefs
from graphbrain.meaning.corefs import coref_id
from graphbrain.meaning.corefs import coref_set
from graphbrain.meaning.corefs import main_coref
from graphbrain.meaning.corefs import make_corefs


class TestCorefs(unittest.TestCase):
    def setUp(self):
        self.hg = hgraph('test.hg')
        self.hg.destroy()

        concepts = []
        concepts.append(hedge('paris/C'))
        concepts.append(hedge('(of/B city/C paris/C)'))
        concepts.append(hedge('(of/B capital/C france/C)'))
        concepts.append(hedge('berlin/C'))
        concepts.append(hedge('(of/B city/C berlin/C)'))
        concepts.append(hedge('(of/B capital/C germany/C)'))
        self.concepts = concepts

        self.hg.add('(love/P i/C (of/B city/C paris/C))')
        self.hg.add('(hate/P i/C (of/B city/C paris/C))')

    def tearDown(self):
        self.hg.close()

    def test_make_2_corefs(self):
        concepts = self.concepts

        make_corefs(self.hg, concepts[0], concepts[1])

        self.assertTrue(are_corefs(self.hg, concepts[0], concepts[1]))
        self.assertFalse(are_corefs(self.hg, concepts[0], concepts[2]))
        self.assertEqual(coref_id(self.hg, concepts[0]),
                         coref_id(self.hg, concepts[1]))
        self.assertIsNotNone(coref_id(self.hg, concepts[0]))
        self.assertIsNone(coref_id(self.hg, concepts[2]))
        self.assertEqual(coref_set(self.hg, concepts[0]),
                         {concepts[0], concepts[1]})

    def test_make_3_corefs(self):
        concepts = self.concepts

        make_corefs(self.hg, concepts[0], concepts[1])
        make_corefs(self.hg, concepts[1], concepts[2])

        self.assertTrue(are_corefs(self.hg, concepts[0], concepts[1]))
        self.assertTrue(are_corefs(self.hg, concepts[0], concepts[2]))
        self.assertEqual(coref_id(self.hg, concepts[0]),
                         coref_id(self.hg, concepts[1]))
        self.assertEqual(coref_id(self.hg, concepts[0]),
                         coref_id(self.hg, concepts[2]))
        self.assertIsNotNone(coref_id(self.hg, concepts[0]))
        self.assertIsNotNone(coref_id(self.hg, concepts[1]))
        self.assertIsNotNone(coref_id(self.hg, concepts[2]))
        self.assertEqual(coref_set(self.hg, concepts[0]),
                         {concepts[0], concepts[1], concepts[2]})

    def test_connect_coref_sets(self):
        concepts = self.concepts

        # paris set
        make_corefs(self.hg, concepts[0], concepts[1])
        make_corefs(self.hg, concepts[1], concepts[2])

        # berlin set
        make_corefs(self.hg, concepts[3], concepts[4])
        make_corefs(self.hg, concepts[4], concepts[5])

        self.assertTrue(are_corefs(self.hg, concepts[0], concepts[1]))
        self.assertTrue(are_corefs(self.hg, concepts[0], concepts[2]))
        self.assertEqual(coref_id(self.hg, concepts[0]),
                         coref_id(self.hg, concepts[1]))
        self.assertEqual(coref_id(self.hg, concepts[0]),
                         coref_id(self.hg, concepts[2]))
        self.assertIsNotNone(coref_id(self.hg, concepts[0]))
        self.assertIsNotNone(coref_id(self.hg, concepts[1]))
        self.assertIsNotNone(coref_id(self.hg, concepts[2]))
        self.assertEqual(coref_set(self.hg, concepts[0]),
                         {concepts[0], concepts[1], concepts[2]})

        self.assertTrue(are_corefs(self.hg, concepts[3], concepts[4]))
        self.assertTrue(are_corefs(self.hg, concepts[3], concepts[5]))
        self.assertEqual(coref_id(self.hg, concepts[3]),
                         coref_id(self.hg, concepts[4]))
        self.assertEqual(coref_id(self.hg, concepts[3]),
                         coref_id(self.hg, concepts[5]))
        self.assertIsNotNone(coref_id(self.hg, concepts[3]))
        self.assertIsNotNone(coref_id(self.hg, concepts[4]))
        self.assertIsNotNone(coref_id(self.hg, concepts[5]))
        self.assertEqual(coref_set(self.hg, concepts[3]),
                         {concepts[3], concepts[4], concepts[5]})

        self.assertFalse(are_corefs(self.hg, concepts[0], concepts[4]))
        self.assertFalse(are_corefs(self.hg, concepts[1], concepts[5]))
        self.assertNotEqual(coref_id(self.hg, concepts[0]),
                            coref_id(self.hg, concepts[4]))
        self.assertNotEqual(coref_id(self.hg, concepts[1]),
                            coref_id(self.hg, concepts[5]))

        # connect both
        make_corefs(self.hg, concepts[0], concepts[5])

        self.assertTrue(are_corefs(self.hg, concepts[0], concepts[4]))
        self.assertTrue(are_corefs(self.hg, concepts[1], concepts[5]))
        self.assertEqual(coref_id(self.hg, concepts[0]),
                         coref_id(self.hg, concepts[4]))
        self.assertEqual(coref_id(self.hg, concepts[1]),
                         coref_id(self.hg, concepts[5]))
        for concept in concepts:
            self.assertEqual(coref_set(self.hg, concept), set(concepts))

    def test_main_coref(self):
        concepts = self.concepts

        self.assertEqual(main_coref(self.hg, concepts[0]), concepts[0])
        self.assertEqual(main_coref(self.hg, concepts[1]), concepts[1])
        self.assertEqual(main_coref(self.hg, concepts[2]), concepts[2])

        make_corefs(self.hg, concepts[0], concepts[1])
        make_corefs(self.hg, concepts[1], concepts[2])

        self.assertEqual(main_coref(self.hg, concepts[0]), concepts[1])
        self.assertEqual(main_coref(self.hg, concepts[1]), concepts[1])
        self.assertEqual(main_coref(self.hg, concepts[2]), concepts[1])


if __name__ == '__main__':
    unittest.main()
