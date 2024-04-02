from __future__ import annotations

import logging
from typing import Type

from graphbrain.hyperedge import Hyperedge
from graphbrain.hypergraph import Hypergraph
from graphbrain.semsim.matcher.matcher import SemSimConfig, SemSimMatcher, SemSimType
from graphbrain.semsim.matcher.fixed_matcher import FixedEmbeddingMatcher
from graphbrain.semsim.matcher.context_matcher import ContextEmbeddingMatcher


logger = logging.getLogger(__name__)


DEFAULT_CONFIGS: dict[SemSimType, SemSimConfig] = {
    SemSimType.FIX: SemSimConfig(
        model_name='word2vec-google-news-300',
        similarity_threshold=0.2
    ),
    SemSimType.CTX: SemSimConfig(
        model_name='intfloat/e5-large-v2',
        similarity_threshold=0.65,
        embedding_prefix="query:"
    )
}

_matcher_type_mapping: dict[SemSimType, Type[SemSimMatcher]] = {
    SemSimType.FIX: FixedEmbeddingMatcher,
    SemSimType.CTX: ContextEmbeddingMatcher
}

_matchers: dict[SemSimType, SemSimMatcher] = {}


def init_matcher(matcher_type: SemSimType, config: SemSimConfig = None):
    global _matchers

    if not config:
        config = DEFAULT_CONFIGS[matcher_type]
        logger.info(
            f"No SemSim config given, using default config"
            f"for SemSim matcher of type '{matcher_type}'"
        )

    _matchers[matcher_type] = _matcher_type_mapping[matcher_type](config=config)
    logger.info(f"Initialized SemSim matcher for type '{matcher_type}': {config=}")


def get_matcher(matcher_type: SemSimType, config: SemSimConfig = None):
    global _matchers

    if config or matcher_type not in _matchers:
        init_matcher(matcher_type=matcher_type, config=config)

    return _matchers[matcher_type]


def semsim(
        semsim_type: str,
        threshold: float = None,
        cand_word: str = None,
        ref_words: list[str] = None,
        cand_edge: Hyperedge = None,
        cand_tok_pos: Hyperedge = None,
        ref_edges: list[Hyperedge] = None,
        ref_tok_poses: list[Hyperedge] = None,
        hg: Hypergraph = None
) -> bool:
    try:
        semsim_type: SemSimType = SemSimType(semsim_type)
    except ValueError:
        logger.error(f"Invalid SemSim model type given: '{semsim_type}")
        return False

    matcher = get_matcher(matcher_type=semsim_type)

    return matcher.similar(
        threshold=threshold,
        cand_word=cand_word,
        ref_words=ref_words,
        cand_edge=cand_edge,
        cand_tok_pos=cand_tok_pos,
        ref_edges=ref_edges,
        ref_tok_poses=ref_tok_poses,
        hg=hg
    )
