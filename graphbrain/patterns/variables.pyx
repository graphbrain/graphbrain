from graphbrain import hedge
from graphbrain.patterns.properties import is_pattern
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


def _assign_edge_to_var(curvars, var_name, edge):
    new_edge = edge
    if var_name in curvars:
        cur_edge = curvars[var_name]
        if cur_edge.not_atom and str(cur_edge[0]) == const.list_or_matches_builder:
            new_edge = cur_edge + (edge,)
        else:
            new_edge = hedge((hedge(const.list_or_matches_builder), cur_edge, edge))
    return {var_name: new_edge}