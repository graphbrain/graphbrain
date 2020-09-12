from graphbrain import hedge
from graphbrain.cognition.agent import Agent
from graphbrain.op import create_op


def conjunctions_resolution(edge):
    if edge.is_atom():
        return []

    if edge[0].type() == 'J' and edge.type()[0] == 'R':
        cur_subj = None
        cur_pred = None
        cur_role = None
        edges = []
        no_obj_edges = []
        for subedge in edge[1:]:
            subj = subedge.edges_with_argrole('s')
            passive = subedge.edges_with_argrole('p')
            newedge = subedge

            if (len(subj) > 0 or len(passive) > 0) and len(subedge) == 2:
                no_obj_edges.append(subedge)

            if len(subj) > 0 and subj[0] is not None:
                cur_subj = subj[0]
                cur_pred = subedge[0]
                cur_role = 's'
            elif len(passive) > 0 and passive[0] is not None:
                cur_subj = passive[0]
                cur_pred = subedge[0]
                cur_role = 'p'
            elif (cur_subj is not None and
                    subedge.type()[0] == 'R' and subedge[0].type() != 'J'):
                newedge = hedge([cur_pred.replace_atom(cur_pred.predicate(),
                                 subedge[0].predicate())]) + hedge(subedge[1:])
                newedge = newedge.insert_edge_with_argrole(
                    cur_subj, cur_role, 0)
                # old_pred = newedge.predicate()
                # new_pred = newedge.predicate()
                # if old_pred and new_pred:
                #     old_pred_u = UniqueAtom(old_pred)
                #     new_pred_u = UniqueAtom(new_pred)
                #     atom2word[new_pred_u] = atom2word[old_pred_u]
            new_edges = conjunctions_resolution(newedge)
            edges += new_edges
        return edges

    for pos, subedge in enumerate(edge):
        if not subedge.is_atom():
            if (subedge[0].type() == 'J' and
                    subedge[0].to_str()[0] != ':' and
                    subedge.type()[0] == 'C'):
                edges = []
                for list_item in subedge[1:]:
                    subedges = conjunctions_resolution(hedge([list_item]))
                    for se in subedges:
                        newedge = hedge(
                            edge[0:pos]) + se + hedge(edge[pos + 1:])
                        edges.append(newedge)
                return edges
            else:
                subedges = conjunctions_resolution(subedge)
                if len(subedges) > 1:
                    edges = []
                    for list_item in subedges:
                        newedge = (hedge(edge[0:pos]) +
                                   hedge([list_item]) +
                                   hedge(edge[pos + 1:]))
                        edges.append(newedge)
                    return edges
    return [edge]


class Conjunctions(Agent):
    def process_edge(self, edge, depth):
        for edge in conjunctions_resolution(edge):
            yield create_op(edge)
