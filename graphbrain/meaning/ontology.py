import graphbrain.constants as const


def subtypes(hg, edge):
    ont_edges = hg.search((const.type_of_pred, '*', edge))
    return tuple([ont_edge[1] for ont_edge in ont_edges])


def supertypes(hg, edge):
    ont_edges = hg.serch((const.type_of_pred, edge, '*'))
    return tuple([ont_edge[2] for ont_edge in ont_edges])
