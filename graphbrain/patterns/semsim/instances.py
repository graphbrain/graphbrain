from dataclasses import dataclass
from typing import Optional, TYPE_CHECKING

from graphbrain.hyperedge import Hyperedge
from graphbrain.semsim import SemSimType

if TYPE_CHECKING:
    from graphbrain.patterns.matcher import Matcher


@dataclass
class SemSimInstance:
    type: SemSimType
    edge: Hyperedge
    word: Optional[str] = None
    tok_pos: Optional[Hyperedge] = None
    threshold: Optional[float] = None


def generate_semsim_instance(
    matcher: 'Matcher',
    semsim_type: SemSimType,
    edge: Hyperedge,
    word: str,
    tok_pos: Optional[Hyperedge] = None,
    threshold: Optional[float] = None,
):
    instance: SemSimInstance = SemSimInstance(
        type=semsim_type,
        edge=edge,
        word=word,
        tok_pos=tok_pos,
        threshold=threshold
    )
    if instance not in matcher.semsim_instances:
        matcher.semsim_instances.append(instance)
