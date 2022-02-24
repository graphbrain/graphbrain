from graphbrain.constants import singular_plural_pred


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


def make_singular_plural(hg, single, plural):
    hg.add((singular_plural_pred, single, plural), primary=False)
