from graphbrain.semsim import SemSimType

SEMSIM_FUNS: dict[str, SemSimType] = {
    'semsim': SemSimType.FIX,
    'semsim-fix': SemSimType.FIX,
    'semsim-ctx': SemSimType.CTX
}