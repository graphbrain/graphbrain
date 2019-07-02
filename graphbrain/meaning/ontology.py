import progressbar
from graphbrain import *


def generate(hg, verbose=False):
    count = 0
    i = 0
    with progressbar.ProgressBar(max_value=hg.edge_count()) as bar:
        for edge in hg.all_edges():
            et = entity_type(edge)
            if et[0] == 'c':
                ct = connector_type(edge[0])
                parent = None
                if ct[0] == 'b':
                    mcs = main_concepts(edge)
                    if len(mcs) == 1:
                        parent = mcs[0]
                elif ct[0] == 'm' and len(edge) == 2:
                    parent = edge[1]
                if parent:
                    ont_edge = ('type_of/p/.', edge, parent)
                    # print(ent2str(ont_edge))
                    hg.add(ont_edge, primary=False)
                    count += 1
            i += 1
            bar.update(i)
    return count
