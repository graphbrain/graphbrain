from typing import Union, Tuple, Optional
import logging

from graphbrain import hedge
from graphbrain.hyperedge import Hyperedge, Atom
from graphbrain.hypergraph import Hypergraph
from graphbrain.patterns.atoms import _matches_atomic_pattern
from graphbrain.semsim import semsim, SemSimType
from graphbrain.utils.lemmas import lemma

logger = logging.getLogger(__name__)


def _match_semsim_fix(
        pattern_parts: Tuple[Hyperedge | Atom],
        edge: Hyperedge,
        curvars: dict,
        skip_semsim: bool,
        use_lemma: bool,
        threshold: Optional[float] = None,
        hg: Optional[Hypergraph] = None,
) -> tuple[Union[list[dict], None], Union[str, None]]:
    assert hg is not None, "Hypergraph must be provided for SemSim FIX matching."

    candidate_word: Union[str, None] = _get_candidate_word(edge, use_lemma, hg)
    if not candidate_word:
        return None, None

    # replace first edge part with wildcard pattern and match the atomic pattern symbolically
    pattern_with_wildcard_word_part = _replace_pattern_word_part(pattern_parts[0], '*')
    if not _matches_atomic_pattern(edge, pattern_with_wildcard_word_part):
        return [], None

    if not skip_semsim:
        # can be one word (e.g. "say") or a list of words (e.g. ["say, tell, speak"])
        reference_words: list[str] = _extract_pattern_words(pattern_parts[0].parts()[0])

        if not semsim(
            semsim_type=SemSimType.FIX,
            threshold=threshold,
            cand_word=candidate_word,
            ref_words=reference_words,
        ):
            return [], None

    return [curvars], candidate_word


def _extract_pattern_words(pattern_word_part: str):
    if pattern_word_part.startswith('[') and pattern_word_part.endswith(']'):
        return [w.strip() for w in pattern_word_part[1:-1].split(',')]
    return [pattern_word_part]


def _replace_pattern_word_part(pattern: Union[Hyperedge, Atom], new_word_part: str):
    return hedge('/'.join([new_word_part] + pattern.parts()[1:]))


def _get_candidate_word(edge: Union[Hyperedge, Atom], use_lemma: bool, hg: Hypergraph) -> Union[str | None]:
    if edge.not_atom:
        logger.warning(f"Edge passed to SemSim FIX matching is not an atom: {edge}")
        return None

    lemma_edge: Union[Hyperedge, None] = None
    if use_lemma:
        lemma_edge = lemma(hg, edge)
        if not lemma_edge:
            logger.debug(f"Could not extract lemma from edge passed to SemSim FIX lemma matching: {edge}")
            return None

    candidate_word: str = hg.text(edge) if not lemma_edge else hg.text(lemma_edge)
    if not candidate_word:
        logger.debug(f"Could not extract text from edge passed to SemSim FIX matching: {edge}")
        return None

    return candidate_word
