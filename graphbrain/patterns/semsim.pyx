import logging
from functools import lru_cache
from typing import Union

from graphbrain.hyperedge import Hyperedge
from graphbrain.hypergraph import Hypergraph
from graphbrain.patterns.matcher import Matcher
from graphbrain.patterns.utils import _edge_tok_pos
from graphbrain.semsim import semsim
from graphbrain.utils.semsim import get_semsim_ctx_tok_poses, get_semsim_ctx_thresholds

logger = logging.getLogger(__name__)

def _match_semsim_ctx(
        matcher: "Matcher",
        edge: Hyperedge,
        pattern: Hyperedge,
        ref_edges: list[Hyperedge],
        hg: Hypergraph

):
    if not _edge_tok_pos(edge, hg):
        logger.error(f"Candidate edge has no 'tok_pos' attribute: {edge}")
        return []

    cand_edge_tok_poses: dict[int, Hyperedge] = get_semsim_ctx_tok_poses(matcher.results_with_special_vars)
    thresholds: dict[int, Union[float, None]] = get_semsim_ctx_thresholds(matcher.results_with_special_vars)

    ref_edges_tok_poses: list[dict[int, Hyperedge]] = _get_ref_edges_tok_poses(
        pattern, ref_edges, [_edge_tok_pos(ref_edge, hg) for ref_edge in ref_edges], hg
    )
    try:
        assert (
            cand_edge_tok_poses.keys() == thresholds.keys() and
            all(cand_edge_tok_poses.keys() == ref_edge_tok_poses.keys() for ref_edge_tok_poses in ref_edges_tok_poses)
        )
    except AssertionError:
        raise ValueError(
            "Number of semsim-ctx for candidate edge and reference edges are not equal."
            "Do the references edges match the pattern?"
        )

    for semsim_ctx_idx in cand_edge_tok_poses.keys():
        if not semsim(
            semsim_type="CTX",
            threshold=thresholds[semsim_ctx_idx],
            cand_edge=edge,
            ref_edges=ref_edges,
            cand_tok_pos=cand_edge_tok_poses[semsim_ctx_idx],
            ref_tok_poses=[ref_edge_tok_poses[semsim_ctx_idx] for ref_edge_tok_poses in ref_edges_tok_poses],
            hg=hg
        ):
            return []

    return matcher.results

# these methods need to be in this module to avoid circular imports
# store hypergraphs to avoid passing them as arguments and enable caching
# TODO: better caching, this is insensitive to changes in the hypergraph
_HG_STORE: dict[int, Hypergraph] = {}
#
def _get_ref_edges_tok_poses(
    pattern: Hyperedge,
    ref_edges: list[Hyperedge],
    root_tok_poses: list[Hyperedge],
    hg: Hypergraph,
) -> list[dict[int, Hyperedge]]:
    hg_id: int = id(hg)
    _HG_STORE[hg_id] = hg

    return _get_ref_edges_tok_poses_cached(pattern, tuple(ref_edges), tuple(root_tok_poses), hg_id)


@lru_cache(maxsize=None)
def _get_ref_edges_tok_poses_cached(
        pattern: Hyperedge,
        ref_edges: tuple[Hyperedge],
        root_tok_poses: tuple[Hyperedge],
        hg_id: int,
) -> list[dict[int, Hyperedge]]:
    try:
        hg: Hypergraph = _HG_STORE[hg_id]
    except KeyError:
        raise ValueError(f"Hypergraph with id '{hg_id}' not found")

    ref_matchers: list[Matcher] = [
        Matcher(ref_edge, pattern, tok_pos=tok_pos, hg=hg)
        for ref_edge, tok_pos in zip(ref_edges, root_tok_poses)
    ]

    non_matching_ref_edges: list[Hyperedge] = [
        ref_edge for ref_edge, ref_matcher in zip(ref_edges, ref_matchers) if not ref_matcher.results
    ]
    try:
        assert not non_matching_ref_edges
    except:
        raise ValueError(f"Reference edge(s) do not match pattern: {non_matching_ref_edges}")

    return [get_semsim_ctx_tok_poses(ref_matcher.results_with_special_vars) for ref_matcher in ref_matchers]