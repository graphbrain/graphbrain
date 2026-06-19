import unittest

from hyperbase import edge_distance
from hyperbase.builders import hedge


class TestDistance(unittest.TestCase):
    def test_identity_atom(self):
        assert edge_distance(hedge("berlin/C"), hedge("berlin/C")) == 0.0

    def test_identity_nested(self):
        edge = hedge("(is/P.so (the/M sky/C) blue/C)")
        assert edge_distance(edge, edge) == 0.0

    def test_symmetry(self):
        a = hedge("(is/P.so berlin/C nice/C)")
        b = hedge("(loves/P.so mary/C art/C)")
        assert edge_distance(a, b) == edge_distance(b, a)

    def test_bounds(self):
        pairs = [
            ("berlin/C", "paris/C"),
            ("berlin/C", "loves/P"),
            ("(is/P.so berlin/C nice/C)", "(loves/P.so mary/C art/C)"),
            ("berlin/C", "(is/P.so berlin/C nice/C)"),
        ]
        for s1, s2 in pairs:
            d = edge_distance(hedge(s1), hedge(s2))
            assert 0.0 <= d <= 1.0

    def test_graded_atom_cost(self):
        base = hedge("(is/P.so berlin/C nice/C)")
        subtype = hedge("(is/P.so berlin/C nice/Cp)")  # same root, subtype only
        root = hedge("(is/P.so berlin/C ugly/C)")  # different root, same type
        both = hedge("(is/P.so berlin/C loves/P)")  # different root and type
        d_subtype = edge_distance(base, subtype)
        d_root = edge_distance(base, root)
        d_both = edge_distance(base, both)
        assert 0.0 < d_subtype < d_root < d_both

    def test_structural_change_costs_more(self):
        base = hedge("(is/P.so berlin/C nice/C)")
        relabel = hedge("(is/P.so berlin/C ugly/C)")  # one leaf relabel
        # extra argument (valid connector with three argroles)
        extra_arg = hedge("(is/P.soc berlin/C nice/C today/C)")
        assert edge_distance(base, relabel) < edge_distance(base, extra_arg)

    def test_canonical_invariance(self):
        # Same relation, arguments and roles permuted (s,o vs o,s).
        a = hedge("(is/P.so berlin/C nice/C)")
        b = hedge("(is/P.os nice/C berlin/C)")
        assert edge_distance(a, b, canonical=True) == 0.0
        assert edge_distance(a, b, canonical=False) > 0.0

    def test_raw_cost(self):
        # A single leaf relabel (same type, different root) -> raw cost 0.5.
        base = hedge("(is/P.so berlin/C nice/C)")
        relabel = hedge("(is/P.so berlin/C ugly/C)")
        assert edge_distance(base, relabel, normalize=False) == 0.5

    def test_weight_extremes(self):
        a = hedge("berlin/C")
        b = hedge("berlin/P")  # same root, different type
        # type_weight=0 -> purely lexical: same word -> identical
        assert edge_distance(a, b, type_weight=0.0, root_weight=1.0) == 0.0
        # root_weight=0 -> purely structural: different type -> non-zero
        assert edge_distance(a, b, root_weight=0.0, type_weight=1.0) > 0.0

    def test_method(self):
        a = hedge("(is/P.so berlin/C nice/C)")
        b = hedge("(is/P.so berlin/C ugly/C)")
        assert a.distance(b) == edge_distance(a, b)

    def test_triangle_inequality_raw(self):
        a = hedge("(is/P.so berlin/C nice/C)")
        b = hedge("(is/P.so berlin/C ugly/C)")
        c = hedge("(loves/P.so mary/C art/C)")
        d_ac = edge_distance(a, c, normalize=False)
        d_ab = edge_distance(a, b, normalize=False)
        d_bc = edge_distance(b, c, normalize=False)
        assert d_ac <= d_ab + d_bc


if __name__ == "__main__":
    unittest.main()
