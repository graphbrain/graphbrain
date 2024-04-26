import logging
from typing import Dict, List, Tuple, Union, TYPE_CHECKING

from graphbrain.hyperedge import Hyperedge, Atom
from graphbrain.patterns.semsim.types import SEMSIM_FUNS
from graphbrain.patterns.semsim.instances import generate_semsim_instance
from graphbrain.patterns.semsim.matching_fix import _match_semsim_fix
from graphbrain.semsim import SemSimType

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

    results: List[Dict] = []
    candidate_word: Union[str, None] = None

    # if semsim type is FIX, match semsim directly also if skipping is enabled
    # to check if edge is actually an atom and to match the atomic pattern
    if semsim_type == SemSimType.FIX:
        semsim_fix_result, candidate_word = _match_semsim_fix(
            pattern_parts,
            edge,
            curvars,
            matcher.skip_semsim,
            semsim_fix_use_lemma,
            similarity_threshold,
            hg=matcher.hg
        )
        if semsim_fix_result is not None:
            results = semsim_fix_result

    # if semsim type is CTX, perform normal symbolic matching
    if semsim_type == SemSimType.CTX and tok_pos:
        results = matcher.match(edge, pattern_parts[0], curvars, tok_pos)

    # if semsim matching should be skipped or semsim type is CTX,
    # add semsim instance to matcher (for later processing)
    if results and (matcher.skip_semsim or semsim_type == SemSimType.CTX):
        generate_semsim_instance(matcher, semsim_type, edge, candidate_word, tok_pos, similarity_threshold)

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
