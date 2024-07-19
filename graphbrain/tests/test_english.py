import unittest

from graphbrain.utils.english import to_american, to_british


class TestEnglish(unittest.TestCase):
    def test_to_american_1(self):
        self.assertEqual(to_american("organisation"), "organization")
    
    def test_to_american_2(self):
        self.assertEqual(to_american("graphbrain"), "graphbrain")

    def test_to_british_1(self):
        self.assertEqual(to_british("organization"), "organisation")

    def test_to_british_2(self):
        self.assertEqual(to_british("graphbrain"), "graphbrain")


if __name__ == '__main__':
    unittest.main()
