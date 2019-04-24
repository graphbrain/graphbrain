from graphbrain.hypergraphs.leveldb import LevelDB


def hypergraph(locator_string):
    return LevelDB(locator_string)
