from __future__ import annotations

import logging
from typing import Type

import graphbrain.patterns
from graphbrain import hedge
from graphbrain.hyperedge import Hyperedge, Atom
from graphbrain.hypergraph import Hypergraph
from graphbrain.semsim.matching.matcher import SemSimConfig, SemSimMatcher, SemSimType
from graphbrain.semsim.matching.fixed_matcher import FixedEmbeddingMatcher
from graphbrain.semsim.matching.context_matcher import ContextEmbeddingMatcher


logger = logging.getLogger(__name__)


DEFAULT_CONFIGS: dict[SemSimType, SemSimConfig] = {
    SemSimType.FIXED: SemSimConfig(
        model_name='word2vec-google-news-300',
        similarity_threshold=0.2
    ),
    SemSimType.CONTEXT: SemSimConfig(
        model_name='intfloat/e5-base',
        similarity_threshold=0.65
    )
}

_matcher_type_mapping: dict[SemSimType, Type[SemSimMatcher]] = {
    SemSimType.FIXED: FixedEmbeddingMatcher,
    SemSimType.CONTEXT: ContextEmbeddingMatcher
}

_matchers: dict[SemSimType, SemSimMatcher] = {}


def init_matcher(matcher_type: SemSimType, config: SemSimConfig = None):
    global _matchers

    if not config:
        config = DEFAULT_CONFIGS[matcher_type]
        logger.info(f"No SemSim config given, using default for matcher type '{matcher_type}'")

    _matchers[matcher_type] = _matcher_type_mapping[matcher_type](config=config)
    logger.info(f"Initialized SemSim matcher for type '{matcher_type}': {config=}")


def get_matcher(matcher_type: SemSimType, config: SemSimConfig = None):
    global _matchers

    if config or matcher_type not in _matchers:
        init_matcher(matcher_type=matcher_type, config=config)

    return _matchers[matcher_type]


def semsim(
        matcher_type: str,
        threshold: float = None,
        candidate: str = None,
        ref_words: list[str] = None,
        ref_edges: list[Hyperedge] = None,
        root_edge: Hyperedge = None,
        tok_pos: Hyperedge = None,
        hg: Hypergraph = None
) -> bool:
    try:
        matcher_type: SemSimType = SemSimType(matcher_type)
    except ValueError:
        logger.error(f"Invalid SemSim model type given: '{matcher_type}")
        return False

    matcher = get_matcher(matcher_type=matcher_type)

    return matcher.similar(
        threshold=threshold,
        candidate=candidate,
        ref_words=ref_words,
        ref_edges=ref_edges,
        root_edge=root_edge,
        tok_pos=tok_pos,
        hg=hg,
    )


#####################################
# --- funcs below will be moved --- #
#####################################

def match_semsim(
        pattern: Hyperedge,
        edge: Hyperedge | Atom,
        curvars: dict,
        matcher_type: str,
        ref_edges: list[Hyperedge] = None,
        root_edge: Hyperedge = None,
        tok_pos: Hyperedge = None,
        hg: Hypergraph = None
) -> list[dict]:
    edge_word_part: str = _get_edge_word_part(edge)
    if not edge_word_part:
        return []

    threshold: float | None = _extract_similarity_threshold(pattern)
    logger.debug(f"edge: {str(edge)} | word part: {edge_word_part} | "
                 f"pattern: {str(pattern)} | threshold: {threshold}")

    # can be one word (e.g. "say") or a list of words (e.g. ["say, tell, speak"])
    pattern_words_part: str = pattern[0].parts()[0]
    reference_words: list[str] = _extract_pattern_words(pattern_words_part)

    if not semsim(
            matcher_type,
            threshold=threshold,
            candidate=edge_word_part,
            ref_words=reference_words,
            ref_edges=ref_edges,
            root_edge=root_edge,
            tok_pos=tok_pos,
            hg=hg
    ):
        return []

    if graphbrain.patterns._matches_atomic_pattern(_replace_edge_word_part(edge, pattern_words_part), pattern[0]):
        return [curvars]

    return []


# replace first edge part with pattern word part
def _replace_edge_word_part(edge: Atom, pattern_word_part: str):
    return hedge('/'.join([pattern_word_part] + edge.parts()[1:]))


def _get_edge_word_part(edge: Hyperedge | Atom) -> str | None:
    if edge.not_atom:
        return None

    edge_word_part: str = edge.parts()[0]

    # special atoms ('_lemma')
    if edge_word_part.startswith('_'):
        return None

    return edge_word_part


def _extract_pattern_words(pattern_word_part: str):
    if pattern_word_part.startswith('[') and pattern_word_part.endswith(']'):
        return [w.strip() for w in pattern_word_part[1:-1].split(',')]
    return [pattern_word_part]


# extract similarity threshold if given
def _extract_similarity_threshold(pattern: Hyperedge) -> float | None:
    if not len(pattern) > 1:
        return None

    try:
        similarity_threshold = float(pattern[1][0])
    except ValueError:
        logger.error(f"Invalid value for similarity threshold: {pattern[1]}")
        return None

    return similarity_threshold
