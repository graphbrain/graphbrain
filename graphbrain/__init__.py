from graphbrain.hyperedge import *
from graphbrain.memory.leveldb import LevelDB


def hgraph(locator_string):
    """Returns an instance of Hypergraph identified by the locator_string.
    The hypergraph will be created if it does not exist.

    Currently, the only type of hypergraph is based on LevelDB storage,
    and the location_string is the path to the LevelDB folder.
    """
    return LevelDB(locator_string)
