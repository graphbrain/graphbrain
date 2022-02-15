from graphbrain.constants import singular_plural_pred
from graphbrain.op import apply_ops
from graphbrain.op import create_op


def number(atom):
    if atom.type() not in {'Cc', 'Cp'}:
        return '?'
    role = atom.role()
    if len(role) < 2:
        return '?'
    if role[1] == 's':
        return 's'
    elif role[1] == 'p':
        return 'p'
    else:
        return '?'


def make_singular_plural_ops(hg, single, plural):
    yield create_op((singular_plural_pred, single, plural), primary=False)


def make_singular_plural(hg, edge1, edge2):
    apply_ops(hg, make_singular_plural_ops(hg, edge1, edge2))
