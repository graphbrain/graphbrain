import unittest
from graphbrain import *
from graphbrain.meaning.synonyms import *


class TestSynonyms(unittest.TestCase):
    def setUp(self):
        self.hg = hypergraph('test.hg')
        self.hg.destroy()

        concepts = []
        concepts.append(str2ent('paris/c'))
        concepts.append(str2ent('(of/b city/c paris/c)'))
        concepts.append(str2ent('(of/b capital/c france/c)'))
        concepts.append(str2ent('berlin/c'))
        concepts.append(str2ent('(of/b city/c berlin/c'))
        concepts.append(str2ent('(of/b capital/c germany/c'))
        self.concepts = concepts

        self.hg.add('(love/p i/c (of/b city/c paris/c))')
        self.hg.add('(hate/p i/c (of/b city/c paris/c))')

    def tearDown(self):
        self.hg.close()

    def test_close(self):
        self.hg.close()

    def test_make_2_synonyms(self):
        concepts = self.concepts

        make_synonyms(self.hg, concepts[0], concepts[1])

        self.assertTrue(are_synonyms(self.hg, concepts[0], concepts[1]))
        self.assertFalse(are_synonyms(self.hg, concepts[0], concepts[2]))
        self.assertEqual(syn_id(self.hg, concepts[0]),
                         syn_id(self.hg, concepts[1]))
        self.assertIsNotNone(syn_id(self.hg, concepts[0]))
        self.assertIsNone(syn_id(self.hg, concepts[2]))
        self.assertEqual(synonym_set(self.hg, concepts[0]),
                         {concepts[0], concepts[1]})

    def test_make_3_synonyms(self):
        concepts = self.concepts

        make_synonyms(self.hg, concepts[0], concepts[1])
        make_synonyms(self.hg, concepts[1], concepts[2])

        self.assertTrue(are_synonyms(self.hg, concepts[0], concepts[1]))
        self.assertTrue(are_synonyms(self.hg, concepts[0], concepts[2]))
        self.assertEqual(syn_id(self.hg, concepts[0]),
                         syn_id(self.hg, concepts[1]))
        self.assertEqual(syn_id(self.hg, concepts[0]),
                         syn_id(self.hg, concepts[2]))
        self.assertIsNotNone(syn_id(self.hg, concepts[0]))
        self.assertIsNotNone(syn_id(self.hg, concepts[1]))
        self.assertIsNotNone(syn_id(self.hg, concepts[2]))
        self.assertEqual(synonym_set(self.hg, concepts[0]),
                         {concepts[0], concepts[1], concepts[2]})

    def test_connect_synonym_sets(self):
        concepts = self.concepts

        # paris set
        make_synonyms(self.hg, concepts[0], concepts[1])
        make_synonyms(self.hg, concepts[1], concepts[2])

        # berlin set
        make_synonyms(self.hg, concepts[3], concepts[4])
        make_synonyms(self.hg, concepts[4], concepts[5])

        self.assertTrue(are_synonyms(self.hg, concepts[0], concepts[1]))
        self.assertTrue(are_synonyms(self.hg, concepts[0], concepts[2]))
        self.assertEqual(syn_id(self.hg, concepts[0]),
                         syn_id(self.hg, concepts[1]))
        self.assertEqual(syn_id(self.hg, concepts[0]),
                         syn_id(self.hg, concepts[2]))
        self.assertIsNotNone(syn_id(self.hg, concepts[0]))
        self.assertIsNotNone(syn_id(self.hg, concepts[1]))
        self.assertIsNotNone(syn_id(self.hg, concepts[2]))
        self.assertEqual(synonym_set(self.hg, concepts[0]),
                         {concepts[0], concepts[1], concepts[2]})

        self.assertTrue(are_synonyms(self.hg, concepts[3], concepts[4]))
        self.assertTrue(are_synonyms(self.hg, concepts[3], concepts[5]))
        self.assertEqual(syn_id(self.hg, concepts[3]),
                         syn_id(self.hg, concepts[4]))
        self.assertEqual(syn_id(self.hg, concepts[3]),
                         syn_id(self.hg, concepts[5]))
        self.assertIsNotNone(syn_id(self.hg, concepts[3]))
        self.assertIsNotNone(syn_id(self.hg, concepts[4]))
        self.assertIsNotNone(syn_id(self.hg, concepts[5]))
        self.assertEqual(synonym_set(self.hg, concepts[3]),
                         {concepts[3], concepts[4], concepts[5]})

        self.assertFalse(are_synonyms(self.hg, concepts[0], concepts[4]))
        self.assertFalse(are_synonyms(self.hg, concepts[1], concepts[5]))
        self.assertNotEqual(syn_id(self.hg, concepts[0]),
                            syn_id(self.hg, concepts[4]))
        self.assertNotEqual(syn_id(self.hg, concepts[1]),
                            syn_id(self.hg, concepts[5]))

        # connect both
        make_synonyms(self.hg, concepts[0], concepts[5])

        self.assertTrue(are_synonyms(self.hg, concepts[0], concepts[4]))
        self.assertTrue(are_synonyms(self.hg, concepts[1], concepts[5]))
        self.assertEqual(syn_id(self.hg, concepts[0]),
                         syn_id(self.hg, concepts[4]))
        self.assertEqual(syn_id(self.hg, concepts[1]),
                         syn_id(self.hg, concepts[5]))
        for concept in concepts:
            self.assertEqual(synonym_set(self.hg, concept), set(concepts))

    def test_main_synonym(self):
        concepts = self.concepts

        self.assertEqual(main_syn(self.hg, concepts[0]), concepts[0])
        self.assertEqual(main_syn(self.hg, concepts[1]), concepts[1])
        self.assertEqual(main_syn(self.hg, concepts[2]), concepts[2])

        make_synonyms(self.hg, concepts[0], concepts[1])
        make_synonyms(self.hg, concepts[1], concepts[2])

        self.assertEqual(main_syn(self.hg, concepts[0]), concepts[1])
        self.assertEqual(main_syn(self.hg, concepts[1]), concepts[1])
        self.assertEqual(main_syn(self.hg, concepts[2]), concepts[1])


if __name__ == '__main__':
    unittest.main()
