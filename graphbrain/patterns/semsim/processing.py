from typing import Dict, List, Union, TYPE_CHECKING

from graphbrain.hyperedge import Hyperedge
from graphbrain.hypergraph import Hypergraph
from graphbrain.patterns.semsim.references import _get_ref_tok_poses
from graphbrain.semsim import semsim

if TYPE_CHECKING:
    from graphbrain.patterns.matcher import Matcher
    from graphbrain.patterns.semsim.instances import SemSimInstance


def filter_results_by_semsim_instances(
        matcher: 'Matcher',
        pattern: Hyperedge,
        edge: Hyperedge,
        ref_edges: List[Hyperedge] = None,
):
    # results_post_semsim: List[Dict] = []
    # for result, semsim_instances in zip(matcher.results, matcher.semsim_instances_sorted):
    #     if process_semsim_instances(semsim_instances, pattern, edge, matcher.hg, ref_edges=ref_edges):
    #         results_post_semsim.append(result)
    # return results_post_semsim

    return [
        result for result, semsim_instances in zip(matcher.results, matcher.semsim_instances_sorted)
        if process_semsim_instances(semsim_instances, pattern, edge, matcher.hg, ref_edges=ref_edges)
    ]


def process_semsim_instances(
        semsim_instances: List['SemSimInstance'],
        pattern: Hyperedge,
        edge: Hyperedge,
        hg: Hypergraph,
        ref_words: List[str] = None,
        ref_edges: List[Hyperedge] = None,
) -> bool:
    ref_tok_poses_per_ref_edge: Union[List[List[Hyperedge]], None] = None
    if ref_edges:
        # this gives a list of lists of tok_poses
        # one list for each ref edge and one tok_pos for each semsim instance
        # if the pattern contains n semsim instances, the lists will have n elements
        ref_tok_poses_per_ref_edge: List[List[Hyperedge]] = _get_ref_tok_poses(pattern, ref_edges, hg)

    for instance_idx, instance in enumerate(semsim_instances):
        ref_tok_poses: List[Hyperedge] = [
            ref_tok_poses[instance_idx] for ref_tok_poses in ref_tok_poses_per_ref_edge
        ] if ref_tok_poses_per_ref_edge else None
        if not semsim(
            semsim_type=instance.type,
            threshold=instance.threshold,
            cand_word=instance.word,
            ref_words=ref_words,
            cand_edge=edge,
            cand_tok_pos=instance.tok_pos,
            ref_edges=ref_edges,
            ref_tok_poses=ref_tok_poses,
            hg=hg
        ):
            return False

    return True
