from graphbrain import hedge


# TODO: make concepts decomposition configurable
def conjunctions_decomposition(edge, concepts=False):
    if edge.atom:
        return []

    # relationship conjunctions
    if edge[0].type() == 'J' and edge.type()[0] != 'C':
        cur_subj = None
        cur_role = None
        edges = []
        for subedge in edge[1:]:
            if subedge.type()[0] != 'R':
                continue

            subj = subedge.edges_with_argrole('s')
            passive = subedge.edges_with_argrole('p')
            newedge = subedge

            if len(subj) > 0 and subj[0] is not None:
                cur_subj = subj[0]
                cur_role = 's'
            elif len(passive) > 0 and passive[0] is not None:
                cur_subj = passive[0]
                cur_role = 'p'
            elif cur_subj is not None and subedge.type()[0] == 'R':
                newedge = hedge([subedge.predicate()]) + hedge(subedge[1:])
                newedge = newedge.insert_edge_with_argrole(
                    cur_subj, cur_role, 0)
            new_edges = conjunctions_decomposition(newedge, concepts=concepts)
            edges += new_edges
        return edges

    # concept conjunctions
    if concepts:
        for pos, subedge in enumerate(edge):
            if not subedge.atom:
                if subedge[0].type() == 'J' and subedge.type()[0] == 'C':
                    edges = []
                    for list_item in subedge[1:]:
                        subedges = conjunctions_decomposition(
                            hedge([list_item]), concepts=concepts)
                        for se in subedges:
                            newedge = hedge(edge[0:pos]) + se + hedge(
                                edge[pos + 1:])
                            edges += conjunctions_decomposition(
                                newedge, concepts=concepts)
                    return edges
                else:
                    subedges = conjunctions_decomposition(
                        subedge, concepts=concepts)
                    if len(subedges) > 1:
                        edges = []
                        for list_item in subedges:
                            newedge = (hedge(edge[0:pos]) +
                                       hedge([list_item]) +
                                       hedge(edge[pos + 1:]))
                            edges += conjunctions_decomposition(
                                newedge, concepts=concepts)
                        return edges

    # no decomposition neeeded
    return [edge]
