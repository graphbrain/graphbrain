import logging

import graphbrain.patterns
from graphbrain import hedge
from graphbrain.semsim.matcher import SemSimMatcher

logger = logging.getLogger(__name__)

W2V_MODEL_NAME: str = 'word2vec-google-news-300'
SIMILARITY_THRESHOLD: float = 0.6

matcher: SemSimMatcher | None = None


def semsim(string1: str, string2: str) -> bool:
    global matcher
    if not matcher:
        matcher = SemSimMatcher(W2V_MODEL_NAME, SIMILARITY_THRESHOLD)

    return matcher.similar(string1, string2)


def match_semsim(pattern, edge, curvars=None, hg=None) -> list[dict]:
    if edge.not_atom:
        return []

    edge_word_part = edge.parts()[0]
    pattern_word_part = pattern[1].parts()[0]

    logger.debug(f"edge: {str(edge)} | word part: {edge_word_part} | pattern: {str(pattern)}")

    if not semsim(edge_word_part, pattern_word_part):
        return []

    edge_parts_modified = edge.parts()
    edge_parts_modified[0] = pattern_word_part
    edge_modified = hedge('/'.join(edge_parts_modified))

    if graphbrain.patterns._matches_atomic_pattern(edge_modified, pattern):
        return [curvars]

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

