from contextlib import contextmanager

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
    if locator_string[-3:] == '.db':
        return graphbrain.memory.sqlite.SQLite(locator_string)
    elif locator_string[-3:] == '.hg':
        if LEVELDB:
            return graphbrain.memory.leveldb.LevelDB(locator_string)
        else:
            print('LevelDB not supported. Custom build required.')
    else:
        print('Hypergraph database could not be found.')


@contextmanager
def hopen(*args, **kwds):
    hg = hgraph(*args, **kwds)
    hg.begin_transaction()
    hg.batch_mode = True
    try:
        yield hg
    finally:
        hg.batch_mode = False
        hg.end_transaction()
