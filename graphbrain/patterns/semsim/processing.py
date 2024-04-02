from typing import List, Union

from graphbrain.hyperedge import Hyperedge
from graphbrain.hypergraph import Hypergraph
from graphbrain.patterns.semsim.instances import SemSimInstance
from graphbrain.patterns.semsim.references import _get_ref_tok_poses
from graphbrain.semsim import semsim


def match_semsim_instances(
        semsim_instances: List[SemSimInstance],
        pattern: Hyperedge,
        edge: Hyperedge,
        hg: Hypergraph,
        threshold: float = None,
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
        threshold: float = threshold if threshold is not None else instance.threshold

        ref_tok_poses: List[Hyperedge] | None = None
        if ref_tok_poses_per_ref_edge:
            ref_tok_poses = [ref_tok_poses[instance_idx] for ref_tok_poses in ref_tok_poses_per_ref_edge]

        if not semsim(
            semsim_type=instance.type,
            threshold=threshold,
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
