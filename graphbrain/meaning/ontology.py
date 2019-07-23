import progressbar
from graphbrain import *
import graphbrain.constants as const


def subtypes(hg, edge):
    ont_edges = hg.pat2edges(hedge((const.type_of_pred, '*', edge)))
    return tuple([ont_edge[1] for ont_edge in ont_edges])


def supertypes(hg, edge):
    ont_edges = hg.pat2ents((const.type_of_pred, edge, '*'))
    return tuple([ont_edge[2] for ont_edge in ont_edges])


def generate(hg, verbose=False):
    count = 0
    i = 0
    total_non_atoms = hg.edge_count() - hg.atom_count()
    with progressbar.ProgressBar(max_value=total_non_atoms) as bar:
        for edge in hg.all_non_atoms():
            et = edge.type()
            if et[0] == 'c':
                ct = edge[0].connector_type()
                parent = None
                if ct[0] == 'b':
                    mcs = edge.main_concepts()
                    if len(mcs) == 1:
                        parent = mcs[0]
                elif ct[0] == 'm' and len(edge) == 2:
                    parent = edge[1]
                if parent:
                    ont_edge = (const.type_of_pred, edge, parent)
                    hg.add(ont_edge, primary=False)
                    count += 1
            i += 1
            bar.update(i)
    return count
