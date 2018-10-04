from gb.reader.semantic_tree import Position


IGNORE, APPLY_HYPEREDGE, NEST_HYPEREDGE, APPLY_TOKEN, NEST_TOKEN, HEAD = range(6)


def apply(parent, root, child_id, pos, transf):
    if transf == IGNORE:
        pass
    elif transf == APPLY_HYPEREDGE:
        if pos == Position.LEFT:
            parent.apply_head(child_id)
        else:
            parent.apply_tail(child_id)
    elif transf == NEST_HYPEREDGE:
        parent.nest(child_id)
    elif transf == APPLY_TOKEN:
        root.apply_tail(child_id)
    elif transf == NEST_TOKEN:
        root.nest(child_id)
    elif transf == HEAD:
        parent.reverse_apply(child_id)


def to_string(transf):
    if transf == IGNORE:
        return 'ignore'
    elif transf == APPLY_HYPEREDGE:
        return 'apply hyperedge'
    elif transf == NEST_HYPEREDGE:
        return 'nest hyperedge'
    elif transf == APPLY_TOKEN:
        return 'apply token'
    elif transf == NEST_TOKEN:
        return 'nest token'
    elif transf == HEAD:
        return 'head'
    else:
        return '?'
