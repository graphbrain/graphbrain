from graphbrain.hypergraph import Hypergraph
import graphbrain.globals as g


def match_semsim(fun_pattern, edge, curvars: dict | None, hg: Hypergraph):
    g.semsim_call_count += 1


    return []



# def _match_lemma(lemma_pattern, edge, curvars, hg):
    # if hg is None:
    #     raise RuntimeError('Lemma pattern function requires hypergraph.')
    #
    # if edge.not_atom:
    #     return []
    #
    # _lemma = lemma(hg, edge, same_if_none=True)
    #
    # # add argroles to _lemma if needed
    # ar = edge.argroles()
    # if ar != '':
    #     parts = _lemma.parts()
    #     parts[1] = '{}.{}'.format(parts[1], ar)
    #     _lemma = hedge('/'.join(parts))
    #
    # if _matches_atomic_pattern(_lemma, lemma_pattern):
    #     return [curvars]
    #
    # return []



