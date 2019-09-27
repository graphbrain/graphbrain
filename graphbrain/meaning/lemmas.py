import graphbrain.constants as const


def lemma(hg, edge):
    if edge.is_atom():
        for lemma_edge in hg.search((const.lemma_pred, edge, '*')):
            return lemma_edge[2]
    return None


def deep_lemma(hg, edge):
    if edge.is_atom():
        for lemma_edge in hg.search((const.lemma_pred, edge, '*')):
            return lemma_edge[2]
        return edge
    else:
        return deep_lemma(hg, edge[1])


def lemma_degrees(hg, edge):
    """Finds all the atoms that share the same given lemma
    and computes the sum of both their degrees and deep degrees.
    These two sums are returned.

    If the parameter edge is non-atomic, this function simply returns
    the degree and deep degree of that edge."""
    if edge.is_atom():
        roots = {edge.root()}

        # find lemma
        for edge in hg.search((const.lemma_pred, edge, '*')):
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
