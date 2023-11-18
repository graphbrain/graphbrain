import logging
from typing import Dict, List, Tuple, Optional, Union, TYPE_CHECKING
# from urllib.parse import unquote

from graphbrain import hedge
from graphbrain.hyperedge import Hyperedge, Atom
from graphbrain.hypergraph import Hypergraph
from graphbrain.patterns.atoms import _matches_atomic_pattern
from graphbrain.patterns.semsim.types import SEMSIM_FUNS
from graphbrain.patterns.semsim.instances import _generate_semsim_instance
from graphbrain.semsim import semsim, SemSimType
from graphbrain.utils.lemmas import lemma

if TYPE_CHECKING:
    from graphbrain.patterns.matcher import Matcher

logger = logging.getLogger(__name__)


def match_semsim(
    matcher: 'Matcher',
    semsim_fun: str,
    pattern_parts: Tuple[Hyperedge | Atom],
    edge: Hyperedge,
    curvars: dict,
    tok_pos: Hyperedge,
) -> list[dict]:
    semsim_type: SemSimType = _get_semsim_type(semsim_fun)
    semsim_fix_use_lemma: bool = _extract_semsim_fix_lemma(semsim_fun)
    similarity_threshold: Union[float, None] = _extract_similarity_threshold(pattern_parts)

    candidate_word: Union[str, None] = None
    results: List[Dict] = []

    if semsim_type == SemSimType.FIX:
        # if semsim type is FIX, match semsim directly also if skipping is enabled
        # to check if edge is actually an atom and to match the atomic pattern
        candidate_word: Union[str, None] = _get_candidate_word(edge, semsim_fix_use_lemma, matcher.hg)
        if candidate_word:
            results = _match_semsim_fix(
                pattern_parts, edge, candidate_word, curvars, matcher.skip_semsim, similarity_threshold, hg=matcher.hg
            )
    if semsim_type == SemSimType.CTX:
        if not tok_pos:
            raise ValueError("tok_pos must be provided for SemSim CTX matching.")
        results = matcher.match(edge, pattern_parts[0], curvars, tok_pos)

    # if semsim matching should be skipped or semsim type is CTX,
    # add semsim instance to matcher (for later processing)
    if results and matcher.skip_semsim or semsim_type == SemSimType.CTX:
        _generate_semsim_instance(
            matcher=matcher,
            results=results,
            semsim_type=semsim_type,
            edge=edge,
            word=candidate_word,
            tok_pos=tok_pos,
            threshold=similarity_threshold,
        )

    return results


def _get_semsim_type(fun: str) -> [SemSimType]:
    semsim_type: SemSimType = SEMSIM_FUNS.get(fun)
    if not semsim_type:
        raise ValueError(f"Unknown semsim function: {fun}")
    return semsim_type


def _extract_semsim_fix_lemma(semsim_fun: str) -> bool:
    """Extracts whether SemSim FIX matching should be done on lemmas."""
    return semsim_fun.endswith('-lemma')


def _extract_similarity_threshold(pattern_parts: Tuple[Hyperedge | Atom]) -> Union[float, None]:
    """Extracts similarity threshold from pattern."""
    if len(pattern_parts) <= 1:
        return None

    try:
        similarity_threshold = float(pattern_parts[1][0])
    except ValueError:
        logger.error(f"Invalid value for similarity threshold: {pattern_parts[1]}")
        return None

    return similarity_threshold


# ----- SemSim FIX ----- #

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


# replace first pattern part with new word part
def _replace_pattern_word_part(pattern: Union[Hyperedge, Atom], new_word_part: str):
    return hedge('/'.join([new_word_part] + pattern.parts()[1:]))


# def _get_edge_word_part(edge: Union[Hyperedge, Atom]) -> Union[str | None]:
#     edge_word_part: str = edge.parts()[0]
#
#     # special atoms ('_lemma')
#     if edge_word_part.startswith('_'):
#         return None
#
#     # decode specially encoded characters
#     edge_word_part = unquote(edge_word_part)
#
#     return edge_word_part
