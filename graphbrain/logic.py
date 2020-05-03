def is_rule(edge):
    if edge.is_atom():
        return False
    if len(edge) != 3:
        return False
    if edge[0].to_str() != ':-':
        return False
    if edge[1].is_atom() or edge[2].is_atom():
        return False
    return True


def eval_rule(hg, rule):
    if not is_rule(rule):
        raise RuntimeError('Not a valid rule: {}'.format(rule.to_str()))

    matches = hg.match(rule[2])
    for edge, results in matches:
        for result in results:
            yield rule[1].apply_vars(result), edge
