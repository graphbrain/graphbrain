from graphbrain.hyperedge import Hyperedge, hedge
from graphbrain.patterns.matcher import Matcher
from graphbrain.patterns.semsim import _match_semsim_ctx
from graphbrain.patterns.utils import _normalize_fun_patterns, _edge_tok_pos

def match_pattern(edge, pattern, curvars=None, hg=None, ref_edges=None):
    """Matches an edge to a pattern. This means that, if the edge fits the
    pattern, then a dictionary will be returned with the values for each
    pattern variable. If the pattern specifies no variables but the edge
    matches it, then an empty dictionary is returned. If the edge does
    not match the pattern, None is returned.

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
    produces the result: {'NAME', mary/Cp}

    (2) the edge: (is/Pd (my/Mp name/Cn) mary/Cp)
    applied to the pattern: (is/Pd (my/Mp name/Cn) (NAME))
    produces the result: {}

    (3) the edge: (is/Pd (my/Mp name/Cn) mary/Cp)
    applied to the pattern: (is/Pd . \*NAME)
    produces the result: None
    """
    edge_hedged: Hyperedge = hedge(edge)
    pattern_hedged: Hyperedge = hedge(pattern)
    pattern_hedged_normalized: Hyperedge =_normalize_fun_patterns(pattern_hedged)
    matcher = Matcher(
        edge_hedged,
        pattern_hedged_normalized,
        curvars=curvars,
        tok_pos=_edge_tok_pos(edge, hg),  # TODO: improve efficiency (do not call for every edge)
        hg=hg
    )

    # check for semsim_ctx matches if necessary
    if matcher.semsim_ctx and matcher.results:
        return _match_semsim_ctx(matcher, edge_hedged, pattern_hedged_normalized, ref_edges, hg)

    return matcher.results


def edge_matches_pattern(edge, pattern, hg=None, ref_edges=None):
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
    result = match_pattern(edge, pattern, hg=hg, ref_edges=ref_edges)
    return len(result) > 0
