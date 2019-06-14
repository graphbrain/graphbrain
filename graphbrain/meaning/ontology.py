from graphbrain import *


if __name__ == '__main__':
    hg = hypergraph('reddit-worldnews-01012013-01082017.hg')
    for edge in hg.all_edges():
        et = entity_type(edge)
        if et[0] == 'c':
            ct = connector_type(edge[0])
            if ct[0] == 'b':
                print(ent2str(edge))
