from graphbrain import *


def run(args):
    hg = hgraph(args.hg)
    print('atoms: {}'.format(hg.atom_count()))
    print('edges: {}'.format(hg.edge_count()))
    print('primary atoms: {}'.format(hg.primary_atom_count()))
    print('primary edges: {}'.format(hg.primary_edge_count()))
