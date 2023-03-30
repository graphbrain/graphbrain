import unittest

from graphbrain.hyperedge import hedge
from graphbrain.learner.classifier import Classifier


class TestClassifier(unittest.TestCase):
    def test_extract_patterns1(self):
        cls = Classifier()
        cls.cases.append((hedge('(likes/P.so mary/C chess/C)'), True))
        cls.cases.append((hedge('(likes/P.so john/C mary/C)'), True))
        cls.extract_patterns()
        patterns = set((rule.pattern.to_str(), rule.positive)
                       for rule in cls.rules)
        self.assertEqual(patterns,
                         {('(likes/P.{so} */C */C)', True)})

    def test_extract_patterns2(self):
        cls = Classifier()
        cls.cases.append((hedge('(likes/P.so mary/C chess/C)'), True))
        cls.cases.append((hedge('(likes/P.so john/C mary/C)'), True))
        cls.cases.append((hedge('(likes/P.so john/C mary/C)'), True))
        cls.extract_patterns()
        patterns = set((rule.pattern.to_str(), rule.positive)
                       for rule in cls.rules)
        self.assertEqual(patterns,
                         {('(likes/P.{so} */C */C)', True)})

    def test_extract_patterns3(self):
        cls = Classifier()
        cls.cases.append((hedge('(likes/P.sox mary/C chess/C today/C)'), True))
        cls.cases.append((hedge('(likes/P.so john/C mary/C)'), True))
        cls.cases.append((hedge('(likes/P.so john/C mary/C)'), True))
        cls.extract_patterns()
        patterns = set((rule.pattern.to_str(), rule.positive)
                       for rule in cls.rules)
        self.assertEqual(patterns,
                         {('(likes/P.{so} */C */C)', True)})


if __name__ == '__main__':
    unittest.main()
