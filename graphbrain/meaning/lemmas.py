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
