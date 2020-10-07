def create_op(edge, optype='add', primary=True, count=False,
              attributes={}, sequence=None, position=-1):
    return {'edge': edge,
            'optype': optype,
            'primary': primary,
            'count': count,
            'attributes': attributes,
            'sequence': sequence,
            'position': position}


def apply_op(hg, op):
    op_performed = False

    edge = op['edge']
    optype = op['optype']
    primary = op['primary']
    count = op['count']
    attributes = op['attributes']
    sequence = op['sequence']

    if optype == 'add':
        if sequence:
            position = op['position']
            # TODO: detect already existing edges in this case
            hg.add_to_sequence(sequence, position, edge)
            hg.set_primary(edge, True)
        else:
            if hg.exists(edge):
                if count:
                    op_performed = True
                    hg.add(edge, primary=primary, count=True)
            else:
                op_performed = True
                hg.add(edge, primary=primary, count=count)

        for attribute in attributes:
            hg.set_attribute(edge, attribute, attributes[attribute])
    elif optype == 'remove':
        if hg.exists(edge):
            op_performed = True
            hg.remove(edge)
    elif optype == 'set_attributes':
        first = True
        for attribute in attributes:
            existed = hg.set_attribute(edge, attribute, attributes[attribute])
            if first:
                op_performed = existed
                first = False
    else:
        raise RuntimeError('Unknown operation type: {}'.format(optype))

    return op_performed


def apply_ops(hg, ops):
    for op in ops:
        apply_op(hg, op)
