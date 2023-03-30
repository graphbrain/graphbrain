from __future__ import annotations

import logging

import graphbrain.patterns
from graphbrain import hedge
from graphbrain.hyperedge import Hyperedge, Atom
from graphbrain.hypergraph import Hypergraph
from graphbrain.semsim.matchers.matcher import SemSimConfig, SemSimMatcher, SemSimModelType
from graphbrain.semsim.matchers.fixed_matcher import FixedEmbeddingMatcher
from graphbrain.semsim.matchers.context_matcher import ContextEmbeddingMatcher


logger = logging.getLogger(__name__)


DEFAULT_CONFIGS: dict[SemSimModelType, SemSimConfig] = {
    SemSimModelType.FIXED_EMBEDDING: SemSimConfig(
        model_type=SemSimModelType.FIXED_EMBEDDING,
        model_name='word2vec-google-news-300',
        similarity_threshold=0.8
    ),
    SemSimModelType.CONTEXT_EMBEDDING: SemSimConfig(
        model_type=SemSimModelType.CONTEXT_EMBEDDING,
        model_name='infloat/e5-base',
        similarity_threshold=0.65
    )
}


matcher: SemSimMatcher | None = None


def init_matcher(config: SemSimConfig = None, model_type: SemSimModelType = None):
    global matcher

    if not config:
        config = DEFAULT_CONFIGS[model_type]
        logger.info(f"No SemSim config given, using default for model_type '{model_type}': {config}")

    if not model_type:
        model_type = config.model_type

    if model_type == SemSimModelType.FIXED_EMBEDDING:
        matcher = FixedEmbeddingMatcher(config)
    if model_type == SemSimModelType.CONTEXT_EMBEDDING:
        matcher = ContextEmbeddingMatcher(config)

    return matcher


def semsim(
        # *args,
        # **kwargs
        candidate: str,
        references: list[str],
        model_type: SemSimModelType,
        threshold: float = None,
        candidate_edge: Hyperedge = None,
        tok_pos: Hyperedge = None,
        root_edge: Hyperedge = None,
        hg: Hypergraph = None
) -> bool:
    global matcher
    if not matcher:
        init_matcher(model_type=model_type)
    # return matcher.similar(*args, **kwargs)
    return matcher.similar(
        candidate,
        references,
        threshold=threshold,
        candidate_edge=candidate_edge,
        root_edge=root_edge,
        tok_pos=tok_pos,
        hg=hg,
    )


# --- funcs below will be moved --- #
def match_semsim(
        pattern: Hyperedge,
        edge: Hyperedge | Atom,
        curvars: dict,
        model_type: SemSimModelType = None,
        ref_sentences: list[str] = None,
        root_edge: Hyperedge = None,
        tok_pos: Hyperedge = None,
        hg: Hypergraph = None
) -> list[dict]:
    try:
        model_type = SemSimModelType(model_type)
    except ValueError:
        logger.error(f"Invalid SemSim model type given: '{model_type}")

    edge_word_part: str = _get_edge_word_part(edge)
    if not edge_word_part:
        return []

    threshold: float | None = _extract_similarity_threshold(pattern)
    logger.debug(f"edge: {str(edge)} | word part: {edge_word_part} | "
                 f"pattern: {str(pattern)} | threshold: {threshold}")

    pattern_word_part: str = pattern[0].parts()[0]
    references: list[str] | None = None

    if model_type == SemSimModelType.FIXED_EMBEDDING:
        references = _extract_pattern_words(pattern_word_part)
    if model_type == SemSimModelType.CONTEXT_EMBEDDING:
        references = ref_sentences

    if not semsim(
            candidate=edge_word_part,
            references=references,
            model_type=model_type,
            threshold=threshold,
            candidate_edge=edge,
            root_edge=root_edge,
            tok_pos=tok_pos,
            hg=hg
    ):
        return []

    if graphbrain.patterns._matches_atomic_pattern(_modify_edge(edge, pattern_word_part), pattern[0]):
        return [curvars]

    return []


# replace first edge part with pattern word part
def _modify_edge(edge: Atom, pattern_word_part: str):
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
