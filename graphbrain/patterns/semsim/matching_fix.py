from typing import Union, Tuple, Optional
import logging

from graphbrain import hedge
from graphbrain.hyperedge import Hyperedge, Atom
from graphbrain.hypergraph import Hypergraph
from graphbrain.patterns.atoms import _matches_atomic_pattern
from graphbrain.semsim import semsim, SemSimType

logger = logging.getLogger(__name__)


def _match_semsim_fix(
        pattern_parts: Tuple[Hyperedge | Atom],
        edge: Hyperedge,
        candidate_word: str,
        curvars: dict,
        skip_semsim: bool,
        threshold: Optional[float] = None,
        hg: Optional[Hypergraph] = None,
) -> list[dict]:
    assert hg is not None, "Hypergraph must be provided for SemSim FIX matching."

    if not skip_semsim:
        # can be one word (e.g. "say") or a list of words (e.g. ["say, tell, speak"])
        pattern_words_part: str = pattern_parts[0].parts()[0]
        reference_words: list[str] = _extract_pattern_words(pattern_words_part)

        if not semsim(
            semsim_type=SemSimType.FIX,
            threshold=threshold,
            cand_word=candidate_word,
            ref_words=reference_words,
        ):
            return []

    # replace first edge part with wildcard pattern to match atomic pattern
    pattern_with_wildcard_word_part = _replace_pattern_word_part(pattern_parts[0], '*')
    if not _matches_atomic_pattern(edge, pattern_with_wildcard_word_part):
        return []

    return [curvars]


def _extract_pattern_words(pattern_word_part: str):
    if pattern_word_part.startswith('[') and pattern_word_part.endswith(']'):
        return [w.strip() for w in pattern_word_part[1:-1].split(',')]
    return [pattern_word_part]


def _replace_pattern_word_part(pattern: Union[Hyperedge, Atom], new_word_part: str):
    return hedge('/'.join([new_word_part] + pattern.parts()[1:]))
