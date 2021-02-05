from graphbrain.hyperedge import hedge, match_pattern


def apply_vars(edge, vars):
    if edge.is_atom():
        if edge.root() in vars:
            return vars[edge.root()]
        else:
            return edge
    return hedge([apply_vars(subedge, vars) for subedge in edge])


def eval(edge, vars):
    if edge[0].to_str() == 'inner-atom':
        return apply_vars(edge[1], vars).predicate_atom()
    else:
        return edge


def match(edge, pattern, curvars={}):
    pattern = hedge(pattern)
    if pattern[0].to_str() == 'and':
        matches = [curvars]
        for subpattern in pattern[1:]:
            new_matches = []
            for vars in matches:
                new_matches += match(edge, subpattern, vars)
            matches = new_matches
        return matches
    elif pattern[0].to_str() == '=':
        newvar = {pattern[1].to_str(): eval(pattern[2], curvars)}
        return [{**curvars, **newvar}]
    else:
        return match_pattern(edge, pattern, curvars)
