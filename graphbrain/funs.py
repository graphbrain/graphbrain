from graphbrain.memory.lmdb import LMDB


def hypergraph(locator_string):
    return LMDB(locator_string)
