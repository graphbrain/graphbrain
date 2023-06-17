from collections import Counter
from itertools import combinations, permutations, product

import graphbrain.constants as const
from graphbrain import hedge


def edge2rolemap(edge):
    argroles = edge[0].argroles()
    if argroles[0] == '{':
        argroles = argroles[1:-1]
    args = list(zip(argroles, edge[1:]))
    rolemap = {}
    for role, subedge in args:
        if role not in rolemap:
            rolemap[role] = []
        rolemap[role].append(subedge)
    return rolemap


def rolemap2edge(pred, rm):
    roles = list(rm.keys())
    argroles = ''
    subedges = [pred]
    for role in roles:
        for arg in rm[role]:
            argroles += role
            subedges.append(arg)
    edge = hedge(subedges)
    return edge.replace_argroles(argroles)


def rolemap_pairings(rm1, rm2):
    roles = list(set(rm1.keys()) & set(rm2.keys()))
    role_counts = {}
    for role in roles:
        role_counts[role] = min(len(rm1[role]), len(rm2[role]))

    pairings = []
    for role in roles:
        role_pairings = []
        n = role_counts[role]
        for args1_combs in combinations(rm1[role], n):
            for args1 in permutations(args1_combs):
                for args2 in combinations(rm2[role], n):
                    role_pairings.append((args1, args2))
        pairings.append(role_pairings)

    for pairing in product(*pairings):
        rm1_ = {}
        rm2_ = {}
        for role, role_pairing in zip(roles, pairing):
            rm1_[role] = role_pairing[0]
            rm2_[role] = role_pairing[1]
        yield rm1_, rm2_


def atom_pattern_counts(edge):
    if edge.atom:
        parts = edge.parts()
        roots = 1 if parts[0] != '*' else 0
        subtyped = 1 if len(edge.type()) > 1 else 0
        typed = 1 if len(parts) > 1 else 0
    else:
        roots = 0
        subtyped = 0
        typed = 0
        for subedge in edge:
            r, s, t = atom_pattern_counts(subedge)
            roots += r
            subtyped += s
            typed += t
    return roots, subtyped, typed


def more_general(edge1, edge2):
    r1, s1, t1 = atom_pattern_counts(edge1)
    r2, s2, t2 = atom_pattern_counts(edge2)
    if r1 == r2:
        if s1 == s2:
            return t1 < t2
        return s1 < s2
    return r1 < r2


def is_variable(edge):
    if edge.not_atom:
        return edge[0].atom and edge[0].root() == 'var'
    return False


def contains_variable(edge):
    if edge.atom:
        return False
    else:
        if is_variable(edge):
            return True
        return any(contains_variable(subedge) for subedge in edge)


def all_variables(edge, _vars=None):
    if _vars is None:
        _vars = Counter()
    if edge is None:
        return _vars
    if edge.atom:
        return _vars
    else:
        if is_variable(edge):
            _vars[edge[2]] += 1
        for subedge in edge:
            all_variables(subedge, _vars=_vars)
    return _vars


def is_valid(edge, _vars=None):
    if _vars is None:
        _vars = set()
    if edge is None:
        return False
    if edge.atom:
        return True
    if is_variable(edge):
        if edge[2].not_atom:
            return False
        # if edge[2] in _vars:
        #     return False
        _vars.add(edge[2])
        return is_valid(edge[1], _vars=_vars)
    return all(is_valid(subedge, _vars=_vars) for subedge in edge)


def extract_vars_map(edge, _vars=None):
    if _vars is None:
        _vars = {}

    if edge is None:
        return _vars
    if edge.not_atom:
        if is_variable(edge):
            new_edge = edge[1]
            var_name = str(edge[2])
            if var_name in _vars:
                cur_edge = _vars[var_name]
                if cur_edge.not_atom and str(cur_edge[0]) == const.list_or_matches_builder:
                    new_edge = cur_edge + (new_edge,)
                else:
                    new_edge = hedge((hedge(const.list_or_matches_builder), cur_edge, new_edge))
            _vars[var_name] = new_edge
        for subedge in edge:
            extract_vars_map(subedge, _vars=_vars)
    return _vars


def remove_variables(edge):
    if is_variable(edge):
        return remove_variables(edge[1])
    if edge.atom:
        return edge
    else:
        return hedge([remove_variables(subedge) for subedge in edge])


def common_pattern_argroles(edge1, edge2):
    rm1 = edge2rolemap(edge1)
    rm2 = edge2rolemap(edge2)

    _vars = all_variables(edge1) | all_variables(edge2)
    best_pattern = None
    for rm1_, rm2_ in rolemap_pairings(rm1, rm2):
        edge1_ = rolemap2edge(edge1[0], rm1_)
        edge2_ = rolemap2edge(edge2[0], rm2_)

        subedges = [_common_pattern(se1, se2) for se1, se2 in zip(edge1_, edge2_)]
        if any(subedge is None for subedge in subedges):
            continue
        argroles = edge1_[0].argroles()
        if argroles == '':
            # deal with (*/P.{} or */B.{})
            pattern = hedge('*/{}'.format(edge1_.mtype()))
        else:
            pattern = hedge(subedges)
            pattern = pattern.replace_argroles('{{{}}}'.format(edge1_[0].argroles()))

        if _vars == all_variables(pattern):
            if best_pattern is None or more_general(best_pattern, pattern):
                best_pattern = pattern

    if best_pattern is None:
        return None

    return best_pattern.normalized()


def common_type(edges):
    types = [edge.type() for edge in edges]
    if len(set(types)) == 1:
        return types[0]
    types = [edge.mtype() for edge in edges]
    if len(set(types)) == 1:
        return types[0]
    return None


def common_pattern_atoms(atoms):
    roots = [atom.root() for atom in atoms]
            
    if len(set(roots)) != 1 or '*' in roots:
        root = '*'
    else:
        root = roots[0]

    if any(len(str(atom).split('/')) == 1 for atom in atoms):
        atype = None
    else:
        atype = common_type(atoms)

    roles1 = []
    roles2 = []
    for atom in atoms:
        role = atom.role()
        role1 = role[1] if len(role) > 1 else None
        role2 = role[2] if len(role) > 2 else None
        roles1.append(role1)
        roles2.append(role2)
    
    role1 = None
    role2 = None
    if len(set(roles1)) == 1 and not roles1[0] is None:
        role1 = roles1[0]
        if len(set(roles2)) == 1 and not roles2[0] is None:
            role2 = roles2[0]
    
    if atype is None:
        atom_str = root
    else:
        role_parts = [atype]
        if role1 is not None:
            role_parts.append(role1)
            if role2 is not None:
                role_parts.append(role2)
        role_str = '.'.join(role_parts)
        atom_str = '{}/{}'.format(root, role_str)
    
    return hedge(atom_str)


def contains_argroles(edge):
    return edge.not_atom and edge[0].argroles() != ''


def _common_pattern(edge1, edge2):
    nedge1 = edge1
    nedge2 = edge2

    # variables
    if is_variable(nedge1):
        var1 = nedge1[2]
    else:
        var1 = None
    if is_variable(nedge2):
        var2 = nedge2[2]
        if var1 is None:
            return None
    else:
        var2 = None
        if var1:
            return None
    if var1 or var2:
        # different variables on same position?
        if var1 and var2 and var1 != var2:
            return None
        var = None
        if var1:
            vedge1 = nedge1[1]
            var = var1
        else:
            vedge1 = nedge1
        if var2:
            vedge2 = nedge2[1]
            var = var2
        else:
            vedge2 = nedge2
        vedge = _common_pattern(vedge1, vedge2)
        if vedge is None or contains_variable(vedge):
            return None
        else:
            return hedge(('var', vedge, var))

    # both are atoms
    if nedge1.atom and nedge2.atom:
        return common_pattern_atoms((nedge1, nedge2))
    # at least one non-atom
    else:
        if contains_argroles(nedge1) and contains_argroles(nedge2):
            if nedge1.mt == nedge2.mt:
                common_pattern = common_pattern_argroles(nedge1, nedge2)
                if common_pattern:
                    return common_pattern

        # do not combine edges with argroles and edges without them
        perform_ordered_match = not (contains_argroles(nedge1) or contains_argroles(nedge2))
        # same length
        if perform_ordered_match and nedge1.not_atom and nedge2.not_atom and len(nedge1) == len(nedge2):
            subedges = [_common_pattern(subedge1, subedge2) for subedge1, subedge2 in zip(nedge1, nedge2)]
            if any(subedge is None for subedge in subedges):
                return None
            return hedge(subedges)
        # not same length
        else:
            if contains_variable(nedge1) or contains_variable(nedge2):
                return None
            etype = common_type((nedge1, nedge2))
            if etype:
                return hedge('*/{}'.format(etype))
            else:
                return hedge('*')


def common_pattern(edge1, edge2):
    edge = _common_pattern(edge1, edge2)
    if is_valid(edge):
        return edge
    else:
        return None


def _extract_any_edges(edge):
    if edge.not_atom and str(edge[0]) == 'any':
        return list(edge[1:])
    else:
        return [edge]


def _merge_patterns(edge1, edge2):
    # edges with different sizes cannot be merged
    if len(edge1) != len(edge2):
        return None

    # atoms are not to be merged
    if edge1.atom or edge2.atom:
        return None

    # edges with no subedge in common are not to be merged
    if all(subedge1 != subedge2 for subedge1, subedge2 in zip(edge1, edge2)):
        return None

    merged_edge = []
    for subedge1, subedge2 in zip(edge1, edge2):
        if subedge1 == subedge2:
            merged_edge.append(subedge1)
        else:
            submerged = merge_patterns(subedge1, subedge2)
            if submerged:
                merged_edge.append(submerged)
            else:
                alternatives = _extract_any_edges(subedge1) + _extract_any_edges(subedge2)
                # heuristic: more complex edges first, likely to be more specific
                alternatives = sorted(alternatives, key=lambda x: x.size(), reverse=True)
                merged_edge.append(['any'] + alternatives)

    return hedge(merged_edge)


def merge_patterns(edge1, edge2):
    edge = _merge_patterns(edge1, edge2)
    if is_valid(edge):
        return edge
    else:
        return None
