import os

from graphbrain.hyperedge import hedge
import graphbrain.memory.sqlite
try:
    import graphbrain.memory.leveldb
    LEVELDB = True
except ImportError:
    LEVELDB = False


def hgraph(locator_string):
    """Returns an instance of Hypergraph identified by the locator_string.
    The hypergraph will be created if it does not exist.

    Currently, the only type of hypergraph is based on LevelDB storage,
    and the location_string is the path to the LevelDB folder.
    """
    if os.path.isfile(locator_string): 
        return graphbrain.memory.sqlite.SQLite(locator_string)
    elif os.path.isdir(locator_string):
        if LEVELDB:
            return graphbrain.memory.leveldb.LevelDB(locator_string)
        else:
            print('LevelDB not supported. Custom build required.')
    else:
        print('Hypergraph database could not be found.')
