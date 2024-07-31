import unittest

from graphbrain import hedge
from graphbrain.utils.english import to_american, to_british, word_to_american, word_to_british


class TestEnglish(unittest.TestCase):
    def test_word_to_american_1(self):
        self.assertEqual(word_to_american("organisation"), "organization")
    
    def test_word_to_american_2(self):
        self.assertEqual(word_to_american("graphbrain"), "graphbrain")

    def test_word_to_british_1(self):
        self.assertEqual(word_to_british("organization"), "organisation")

    def test_word_to_british_2(self):
        self.assertEqual(word_to_british("graphbrain"), "graphbrain")

    def test_to_american_1(self):
        self.assertEqual(
            to_american(hedge('(is/P.sc/en graphbrain/Cp.s/en (a/Md/en (secret/Ma/en organisation/Cc.s/en)))')),
                        hedge('(is/P.sc/en graphbrain/Cp.s/en (a/Md/en (secret/Ma/en organization/Cc.s/en)))'))

    def test_to_british_1(self):
        self.assertEqual(
            to_british(hedge('(is/P.sc/en graphbrain/Cp.s/en (a/Md/en (secret/Ma/en organization/Cc.s/en)))')),
                       hedge('(is/P.sc/en graphbrain/Cp.s/en (a/Md/en (secret/Ma/en organisation/Cc.s/en)))'))
        
    def test_to_american_non_en_1(self):
        self.assertEqual(
            to_american(hedge('(is/P.sc graphbrain/Cp.s (a/Md (secret/Ma organisation/Cc.s)))')),
                        hedge('(is/P.sc graphbrain/Cp.s (a/Md (secret/Ma organisation/Cc.s)))'))

    def test_to_british_non_en_1(self):
        self.assertEqual(
            to_british(hedge('(is/P.sc graphbrain/Cp.s (a/Md (secret/Ma organization/Cc.s)))')),
                       hedge('(is/P.sc graphbrain/Cp.s (a/Md (secret/Ma organization/Cc.s)))'))

    def test_to_american_non_en_2(self):
        self.assertEqual(
            to_american(hedge('(is/P.sc/xx graphbrain/Cp.s/xx (a/Md/xx (secret/Ma/xx organisation/Cc.s/xx)))')),
                        hedge('(is/P.sc/xx graphbrain/Cp.s/xx (a/Md/xx (secret/Ma/xx organisation/Cc.s/xx)))'))

    def test_to_british_non_en_2(self):
        self.assertEqual(
            to_british(hedge('(is/P.sc/xx graphbrain/Cp.s/xx (a/Md/en (secret/Ma/xx organization/Cc.s/xx)))')),
                       hedge('(is/P.sc/xx graphbrain/Cp.s/xx (a/Md/en (secret/Ma/xx organization/Cc.s/xx)))'))

if __name__ == '__main__':
    unittest.main()
