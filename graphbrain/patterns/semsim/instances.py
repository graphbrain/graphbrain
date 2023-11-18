from dataclasses import dataclass
from typing import List, Dict, Optional, Union, TYPE_CHECKING

from graphbrain.hyperedge import Hyperedge
from graphbrain.patterns.variables import _generate_special_var, _get_special_var_val, _remove_special_vars
from graphbrain.semsim import SemSimType

if TYPE_CHECKING:
    from graphbrain.patterns.matcher import Matcher

_SEMSIM_INSTANCE_VAR_CODE: str = 'semsim-instances'


@dataclass
class SemSimInstance:
    type: SemSimType
    edge: Hyperedge
    word: str
    tok_pos: Optional[Hyperedge] = None
    threshold: Optional[float] = None


def _generate_semsim_instance(
        matcher: 'Matcher',
        results: List[Dict],
        semsim_type: SemSimType,
        edge: Hyperedge,
        word: Optional[str],
        tok_pos: Optional[Hyperedge],
        threshold: Optional[float],
):
    for variable_assignment in results:
        svar_name: str = _generate_special_var(_SEMSIM_INSTANCE_VAR_CODE, variable_assignment, default_var_val=[])
        semsim_instance_id: int = len(matcher.semsim_instances_mapped)
        variable_assignment[svar_name].append(semsim_instance_id)

        matcher.semsim_instances_mapped[semsim_instance_id] = SemSimInstance(
            type=semsim_type, edge=edge, word=word, tok_pos=tok_pos, threshold=threshold
        )


def process_semsim_instance_vars(matcher: 'Matcher'):
    for variable_assignment in matcher.results:
        semsim_instance_ids: Union[list[int], None] = _get_special_var_val(
            _SEMSIM_INSTANCE_VAR_CODE, variable_assignment
        )

        semsim_instances_in_variable_assignment: list[SemSimInstance] = [
            matcher.semsim_instances_mapped[semsim_instance_id] for semsim_instance_id in semsim_instance_ids
        ] if semsim_instance_ids else []

        matcher.semsim_instances_sorted.append(semsim_instances_in_variable_assignment)

    matcher.results = [
        _remove_special_vars(variable_assignment) for variable_assignment in matcher.results
    ]