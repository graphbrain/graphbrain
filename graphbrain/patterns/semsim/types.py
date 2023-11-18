from typing import Dict
from graphbrain.semsim import SemSimType

SEMSIM_FUNS: Dict[str, SemSimType] = {
    'semsim': SemSimType.FIX,
    'semsim-fix': SemSimType.FIX,
    'semsim-fix-lemma': SemSimType.FIX,
    'semsim-ctx': SemSimType.CTX
}
