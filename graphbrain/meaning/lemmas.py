import graphbrain.constants as const


def lemma(hg, atom):
    """Returns the lemma of the given atom if it exists, None otherwise."""
    if atom.is_atom():
        for lemma_edge in hg.search((const.lemma_pred,
                                     atom.simplify_role(),
                                     '*')):
            return lemma_edge[2]
    return None


def deep_lemma(hg, edge):
    """Returns the lemma of an atomic edge, or the lemma of the first atom
    found by recursively descending the hyperedge, always choosing the
    subedge immediatly after the connector.

    This is useful, for example, to find the lemma of the central verb
    in a non-atomic predicate edge. For example:

    (not/A (is/A going/P))

    could return

    go/P
    """
    if edge.is_atom():
        for lemma_edge in hg.search((const.lemma_pred,
                                     edge.simplify_role(),
                                     '*')):
            return lemma_edge[2]
        return edge
    else:
        return deep_lemma(hg, edge[1])


def lemma_degrees(hg, edge):
    """Finds all the atoms that share the same given lemma
    and computes the sum of both their degrees and deep degrees.
    These two sums are returned.

    If the parameter edge is non-atomic, this function simply returns
    the degree and deep degree of that edge.
    """
    if edge.is_atom():
        roots = {edge.root()}

        # find lemma
        for edge in hg.search((const.lemma_pred, edge.simplify_role(), '*')):
            roots.add(edge[2].root())

        # compute degrees
        d = 0
        dd = 0
        for r in roots:
            atoms = set(hg.atoms_with_root(r))
            d += sum([hg.degree(atom) for atom in atoms])
            dd += sum([hg.deep_degree(atom) for atom in atoms])

        return d, dd
    else:
        return hg.degree(edge), hg.deep_degree(edge)
