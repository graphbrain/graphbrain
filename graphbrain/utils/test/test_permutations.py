import unittest
from ..permutations import *

class TestPermutations(unittest.TestCase):

    def test_permutations(self):
    	self.assertEqual(permutations(0), 1)
    	self.assertEqual(permutations(1), 1)
    	self.assertEqual(permutations(2), 2)
    	self.assertEqual(permutations(3), 6)
    	self.assertEqual(permutations(4), 24)