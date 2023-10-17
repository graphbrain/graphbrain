from graphbrain import hedge
from graphbrain.patterns import is_pattern
import graphbrain.constants as const

def apply_vars(edge, variables):
    if edge.atom:
        if is_pattern(edge):
            varname = _varname(edge)
            if len(varname) > 0 and varname in variables:
                return variables[varname]
        return edge
    else:
        return hedge([apply_vars(subedge, variables) for subedge in edge])

def _varname(atom):
    if not atom.atom:
        return ''
    label = atom.parts()[0]
    if len(label) == 0:
        return label
    elif label[0] in {'*', '.'}:
        return label[1:]
    elif label[:3] == '...':
        return label[3:]
    elif label[0].isupper():
        return label
    else:
        return ''

def _generate_special_var_name(var_code, vars_):
    prefix = f'__{var_code}'
    var_count = len([var_name for var_name in vars_ if var_name.startswith(prefix)])
    return f'__{var_code}_{var_count}'


def _regular_var_count(vars_):
    return len([var_name for var_name in vars_ if not var_name.startswith('__')])


def _remove_special_vars(vars_):
    return {key: value for key, value in vars_.items() if not key.startswith('__')}


def _assign_edge_to_var(curvars, var_name, edge):
    new_edge = edge
    if var_name in curvars:
        cur_edge = curvars[var_name]
        if cur_edge.not_atom and str(cur_edge[0]) == const.list_or_matches_builder:
            new_edge = cur_edge + (edge,)
        else:
            new_edge = hedge((hedge(const.list_or_matches_builder), cur_edge, edge))
    return {var_name: new_edge}