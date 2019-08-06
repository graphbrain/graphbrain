def find_concept(edge):
    if edge.type()[0] == 'c':
        return edge
    elif not edge.is_atom():
        return find_concept(edge[1])
    else:
        return None


def is_proper_concept(edge):
    if edge.is_atom():
        return edge.type()[:2] == 'cp'
    else:
        for subedge in edge[1:]:
            if is_proper_concept(subedge):
                return True
        return False


def all_concepts(edge):
    concepts = set()
    if edge.type()[0] == 'c':
        concepts.add(edge)
    if not edge.is_atom():
        for item in edge:
            concepts |= all_concepts(item)
    return concepts
