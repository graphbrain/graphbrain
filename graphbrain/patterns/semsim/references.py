import logging
from functools import lru_cache
from typing import List, Tuple

from graphbrain.hyperedge import Hyperedge
from graphbrain.hypergraph import Hypergraph
from graphbrain.patterns.matcher import Matcher
from graphbrain.patterns.utils import _edge_tok_pos

logger: logging.Logger = logging.getLogger(__name__)

# caching is insensitive to changes in the hypergraph
_HG_STORE: dict[int, Hypergraph] = {}


def _get_ref_tok_poses(
    pattern: Hyperedge,
    ref_edges: List[Hyperedge],
    hg: Hypergraph,
) -> List[List[Hyperedge]]:
    hg_id: int = id(hg)
    _HG_STORE[hg_id] = hg

    ref_tok_poses: List[List[Hyperedge]] = _get_ref_edges_tok_poses_cached(pattern, tuple(ref_edges), hg_id)
    logger.debug(f"Ref. tok poses - cache info: {_get_ref_edges_tok_poses_cached.cache_info()}")
    return ref_tok_poses


@lru_cache(maxsize=None)
def _get_ref_edges_tok_poses_cached(
        pattern: Hyperedge,
        ref_edges: Tuple[Hyperedge],
        hg_id: int,
) -> List[List[Hyperedge]]:
    try:
        hg: Hypergraph = _HG_STORE[hg_id]
    except KeyError:
        raise ValueError(f"Hypergraph with id '{hg_id}' not found")

    root_tok_poses: List[Hyperedge] = [_edge_tok_pos(ref_edge, hg) for ref_edge in ref_edges]
    assert all(tok_pos is not None for tok_pos in root_tok_poses), (
        f"Could not get root tok pos for all ref edges: {ref_edges}"
    )

    ref_matchers: List[Matcher] = [
        Matcher(ref_edge, pattern, curvars={}, tok_pos=tok_pos, skip_semsim=True, hg=hg)
        for ref_edge, tok_pos in zip(ref_edges, root_tok_poses)
    ]

    return _get_semsim_tok_poses(ref_matchers, ref_edges)


def _get_semsim_tok_poses(ref_matchers: List[Matcher], ref_edges: Tuple[Hyperedge]) -> List[List[Hyperedge]]:
    ref_tok_poses: list[list[Hyperedge]] = []
    for matcher, ref_edge in zip(ref_matchers, ref_edges):
        assert matcher.results, f"Reference edge does not match pattern: {ref_edge}"
        assert matcher.semsim_instances, f"No semsim instances found for reference edge: {ref_edge}"
        ref_tok_poses.append([instance.tok_pos for instance in matcher.semsim_instances])
    return ref_tok_poses
