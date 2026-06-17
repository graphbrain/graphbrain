from functools import lru_cache

from hyperbase.hyperedge import Hyperedge


@lru_cache(maxsize=2048)
def clean_alphanumeric(s: str) -> str:
    """Lowercase ``s`` and strip every non-alphanumeric character.

    Memoized: this is a pure ``str -> str`` map called many times on the same
    small set of token / atom-label strings (e.g. the correctness check runs it
    per token per candidate during the parser's correctness search). The cache
    is bounded so long-running workers / REPLs don't grow it without limit.
    """
    return "".join(c.lower() for c in s if c.isalnum())


def filter_alphanumeric_strings(strings: list[str]) -> list[str]:
    """
    Filter a list of strings to include only those containing alphanumeric characters,
    and remove all non-alphanumeric characters from each string.

    Args:
        strings: List of strings to filter

    Returns:
        Filtered list containing only lowercased alphanumeric characters
    """
    return [cleaned for s in strings if (cleaned := clean_alphanumeric(s))]


def edge_depth_exceeds(edge: Hyperedge, limit: int) -> bool:
    """Iteratively check whether an edge's nesting depth exceeds *limit*.

    Walks the edge with an explicit stack so it never triggers Python's
    recursion limit, even on pathologically deep edges that would crash a
    recursive ``Hyperedge.depth()`` call. Used by parsers to reject parses
    that are too deep to be safely transformed or serialised.
    """
    if edge.atom:
        return limit < 0
    stack: list[tuple[Hyperedge, int]] = [(edge, 1)]
    while stack:
        e, d = stack.pop()
        if d > limit:
            return True
        if e.atom:
            continue
        for sub in e:
            stack.append((sub, d + 1))
    return False
