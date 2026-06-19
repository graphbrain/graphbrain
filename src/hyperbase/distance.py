"""Distance measure between two hyperedges.

The distance is an ordered *tree edit distance* (Zhang-Shasha): the minimum
cost of a sequence of node relabel / insert / delete operations that turns one
edge-tree into the other. It accounts for both the nesting structure and the
atom content of the edges, and is polynomial in the size of the edges, which
makes it tractable for the (small) edges produced by sentence parsing.

The raw cost is a true metric. By default it is normalised to ``[0, 1]`` by
dividing by the combined node count of both edges, so ``0`` means identical and
larger values mean more different.
"""

from __future__ import annotations

from typing import cast

from hyperbase.hyperedge import Atom, Hyperedge


def _annotate(root: Hyperedge) -> tuple[list[Hyperedge], list[int], list[int]]:
    """Post-order annotation of a tree for the Zhang-Shasha algorithm.

    Returns ``(nodes, lmld, keyroots)`` where ``nodes`` lists every node of the
    tree in post-order, ``lmld[i]`` is the post-order index of the leftmost-leaf
    descendant of node ``i``, and ``keyroots`` lists the post-order indices of
    the key roots (the root plus every node that has a left sibling).
    """
    nodes: list[Hyperedge] = []
    lmld: list[int] = []

    def visit(edge: Hyperedge) -> int:
        if edge.atom or len(edge) == 0:
            idx = len(nodes)
            nodes.append(edge)
            lmld.append(idx)
            return idx
        first_leaf = -1
        for pos, child in enumerate(edge):
            child_idx = visit(child)
            if pos == 0:
                first_leaf = lmld[child_idx]
        idx = len(nodes)
        nodes.append(edge)
        lmld.append(first_leaf)
        return idx

    visit(root)

    # The key root for a given leftmost leaf is the highest-indexed node that
    # shares it; iterating in order and overwriting yields exactly that.
    keyroot_for: dict[int, int] = {}
    for i, leaf in enumerate(lmld):
        keyroot_for[leaf] = i
    keyroots = sorted(keyroot_for.values())
    return nodes, lmld, keyroots


def _safe_normalise(edge: Hyperedge) -> Hyperedge:
    """Normalise argument roles, falling back to the edge on malformed input."""
    from hyperbase.transforms import normalise

    try:
        return normalise(edge)
    except (KeyError, RuntimeError):
        return edge


def _internal_label(edge: Hyperedge) -> str:
    """Label used to compare two non-atom nodes (their inferred main type)."""
    try:
        return edge.mtype()
    except RuntimeError:
        return "?"


def edge_distance(
    edge1: Hyperedge,
    edge2: Hyperedge,
    *,
    normalize: bool = True,
    canonical: bool = True,
    root_weight: float = 0.5,
    type_weight: float = 0.5,
) -> float:
    """Tree edit distance between two hyperedges.

    Keyword arguments:
    normalize -- divide the raw edit cost by the combined node count, yielding a
        value in ``[0, 1]`` (default: True). When False, the raw cost is
        returned.
    canonical -- normalise the argument-role ordering of both edges first, so
        that edges differing only by a benign argument reordering are treated as
        equal (default: True).
    root_weight -- relative weight of the atom root (the word) when relabelling
        two atoms (default: 0.5).
    type_weight -- relative weight of the atom type/role when relabelling two
        atoms (default: 0.5).

    The atom weights are normalised so that any non-negative pair keeps a single
    atom relabel cost within ``[0, 1]``; ``type_weight=0`` gives a purely
    lexical comparison and ``root_weight=0`` a purely structural one.
    """
    if canonical:
        edge1 = _safe_normalise(edge1)
        edge2 = _safe_normalise(edge2)

    total_weight = root_weight + type_weight
    if total_weight <= 0:
        w_root = w_type = 0.5
    else:
        w_root = root_weight / total_weight
        w_type = type_weight / total_weight

    def atom_cost(a: Atom, b: Atom) -> float:
        root_part = 0.0 if a.root() == b.root() else 1.0
        if a.type() == b.type():
            type_part = 0.0
        elif a.mtype() == b.mtype():
            type_part = 0.5
        else:
            type_part = 1.0
        return w_root * root_part + w_type * type_part

    def relabel_cost(a: Hyperedge, b: Hyperedge) -> float:
        if a.atom and b.atom:
            return atom_cost(cast(Atom, a), cast(Atom, b))
        if a.not_atom and b.not_atom:
            return 0.0 if _internal_label(a) == _internal_label(b) else 0.5
        # one atom and one non-atom: maximal substitution cost
        return 1.0

    nodes1, lmld1, keyroots1 = _annotate(edge1)
    nodes2, lmld2, keyroots2 = _annotate(edge2)
    n1 = len(nodes1)
    n2 = len(nodes2)

    treedist = [[0.0] * n2 for _ in range(n1)]

    for i in keyroots1:
        li = lmld1[i]
        for j in keyroots2:
            lj = lmld2[j]
            rows = i - li + 2
            cols = j - lj + 2
            fd = [[0.0] * cols for _ in range(rows)]
            for di in range(1, rows):
                fd[di][0] = fd[di - 1][0] + 1.0
            for dj in range(1, cols):
                fd[0][dj] = fd[0][dj - 1] + 1.0
            for di in range(1, rows):
                ni = li + di - 1
                for dj in range(1, cols):
                    nj = lj + dj - 1
                    delete = fd[di - 1][dj] + 1.0
                    insert = fd[di][dj - 1] + 1.0
                    if lmld1[ni] == li and lmld2[nj] == lj:
                        relabel = fd[di - 1][dj - 1] + relabel_cost(
                            nodes1[ni], nodes2[nj]
                        )
                        best = min(delete, insert, relabel)
                        fd[di][dj] = best
                        treedist[ni][nj] = best
                    else:
                        relabel = fd[lmld1[ni] - li][lmld2[nj] - lj] + treedist[ni][nj]
                        fd[di][dj] = min(delete, insert, relabel)

    ted = treedist[n1 - 1][n2 - 1]
    if not normalize:
        return ted
    total = n1 + n2
    if total == 0:
        return 0.0
    return ted / total
