from typing import Dict, List, Tuple, Optional, Union

from graphbrain.hyperedge import Hyperedge, hedge
from graphbrain.hypergraph import Hypergraph
from graphbrain.patterns.matcher import Matcher
from graphbrain.patterns.semsim.instances import SemSimInstance
from graphbrain.patterns.semsim.processing import match_semsim_instances
from graphbrain.patterns.utils import _normalize_fun_patterns, _edge_tok_pos


def match_pattern(
        edge: Union[Hyperedge, str, list, tuple],
        pattern: Union[Hyperedge, str, list, tuple],
        curvars: Optional[dict[str, Hyperedge]] = None,
        ref_edges: Optional[List[Union[Hyperedge, str, list, tuple]]] = None,
        skip_semsim: bool = False,
        hg: Optional[Hypergraph] = None
) -> Union[List[Dict], Tuple[List[Dict], List[SemSimInstance]]]:
    """Matches an edge to a pattern. This means that, if the edge fits the
    pattern, then a list of dictionaries will be returned. If the pattern
    specifies variables, then the returned dictionaries will be populated
    with the values for each pattern variable. There can be more than one
    dictionary in the list if there are multiple ways of matching the
    variables. If the pattern specifies no variables but the edge matches
    it, then a list with a single empty dictionary is returned. If the
    edge does not match the pattern, an empty list is returned.

    Patterns are themselves edges. They can match families of edges
    by employing special atoms:

    -> '\*' represents a general wildcard (matches any entity)

    -> '.' represents an atomic wildcard (matches any atom)

    -> '(\*)' represents an edge wildcard (matches any edge)

    -> '...' at the end indicates an open-ended pattern.

    The wildcards ('\*', '.' and '(\*)') can be used to specify variables,
    for example '\*x', '(CLAIM)' or '.ACTOR'. In case of a match, these
    variables are assigned the hyperedge they correspond to. For example,

    (1) the edge: (is/Pd (my/Mp name/Cn) mary/Cp)
    applied to the pattern: (is/Pd (my/Mp name/Cn) \*NAME)
    produces the result: [{'NAME', mary/Cp}]

    (2) the edge: (is/Pd (my/Mp name/Cn) mary/Cp)
    applied to the pattern: (is/Pd (my/Mp name/Cn) (NAME))
    produces the result: [{}]

    (3) the edge: (is/Pd (my/Mp name/Cn) mary/Cp)
    applied to the pattern: (is/Pd . \*NAME)
    produces the result: []
    """
    edge: Hyperedge = hedge(edge)
    pattern: Hyperedge = _normalize_fun_patterns(hedge(pattern))

    matcher: Matcher = Matcher(
        edge=edge,
        pattern=pattern,
        curvars=curvars,
        tok_pos=_edge_tok_pos(edge, hg),  # TODO: improve efficiency (do not call for every edge)
        skip_semsim=skip_semsim,
        hg=hg
    )

    if skip_semsim:
        return matcher.results, matcher.semsim_instances

    if matcher.results and match_semsim_instances(
            matcher.semsim_instances,
            pattern=pattern,
            edge=edge,
            ref_edges=ref_edges,
            hg=hg
    ):
        return matcher.results

    return []


def edge_matches_pattern(
        edge: Union[Hyperedge, str, list, tuple],
        pattern: Union[Hyperedge, str, list, tuple],
        ref_edges: Optional[List[Union[Hyperedge, str, list, tuple]]] = None,
        hg: Optional[Hypergraph] = None
):
    """Check if an edge matches a pattern.

    Patterns are themselves edges. They can match families of edges
    by employing special atoms:

    -> '\*' represents a general wildcard (matches any entity)

    -> '.' represents an atomic wildcard (matches any atom)

    -> '(\*)' represents an edge wildcard (matches any edge)

    -> '...' at the end indicates an open-ended pattern.

    The pattern can be any valid hyperedge, including the above special atoms.
    Examples: (is/Pd graphbrain/C .)
    (says/Pd * ...)
    """
    result = match_pattern(edge, pattern, ref_edges=ref_edges, hg=hg)
    return len(result) > 0
