import itertools
from typing import TYPE_CHECKING

from graphbrain.hyperedge import Hyperedge
from graphbrain.patterns.utils import _defun_pattern_argroles

if TYPE_CHECKING:
    from graphbrain.patterns.matcher import Matcher


def _match_by_argroles(
        matcher: 'Matcher',
        edge: Hyperedge,
        pattern: Hyperedge,
        role_counts,
        min_vars,
        matched=(),
        curvars=None,
        tok_pos=None
):
    if curvars is None:
        curvars = {}

    if len(role_counts) == 0:
        return [curvars]

    argrole, n = role_counts[0]

    # match connector
    if argrole == 'X':
        eitems = [edge[0]]
        pitems = [pattern[0]]
    # match any argrole
    elif argrole == '*':
        eitems = [e for e in edge if e not in matched]
        pitems = pattern[-n:]
    # match specific argrole
    else:
        eitems = edge.edges_with_argrole(argrole)
        pitems = _defun_pattern_argroles(pattern).edges_with_argrole(argrole)

    if len(eitems) < n:
        if len(curvars) >= min_vars:
            return [curvars]
        else:
            return []

    result = []

    if tok_pos:
        tok_pos_items = [tok_pos[i] for i, subedge in enumerate(edge) if subedge in eitems]
        tok_pos_perms = tuple(itertools.permutations(tok_pos_items, r=n))

    for perm_n, perm in enumerate(tuple(itertools.permutations(eitems, r=n))):
        if tok_pos:
            tok_pos_perm = tok_pos_perms[perm_n]
        perm_result = [{}]
        for i, eitem in enumerate(perm):
            pitem = pitems[i]
            tok_pos_item = tok_pos_perm[i] if tok_pos else None
            item_result = []
            for variables in perm_result:
                item_result += matcher.match(
                    eitem,
                    pitem,
                    {**curvars, **variables},
                    tok_pos=tok_pos_item
                )
            perm_result = item_result
            if len(item_result) == 0:
                break

        for variables in perm_result:
            result += _match_by_argroles(
                matcher,
                edge,
                pattern,
                role_counts[1:],
                min_vars,
                matched + perm,
                {**curvars, **variables},
                tok_pos=tok_pos
            )

    return result