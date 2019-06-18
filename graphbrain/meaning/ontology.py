from graphbrain import *


def generate_ontology(hg):
    count = 0
    for edge in hg.all_edges():
        et = entity_type(edge)
        if et[0] == 'c':
            ct = connector_type(edge[0])
            parent = None
            if ct[0] == 'b':
                parent = main_concept(edge)
            elif ct[0] == 'm' and len(edge) == 2:
                parent = edge[1]
            if parent:
                syn_edge = ('type_of/p/.', edge, parent)
                # print(ent2str(syn_edge))
                count += 1
    return count


if __name__ == '__main__':
    hg = hypergraph('reddit-worldnews-01012013-01082017.hg')
    count = generate_ontology(hg)
    print(count)
